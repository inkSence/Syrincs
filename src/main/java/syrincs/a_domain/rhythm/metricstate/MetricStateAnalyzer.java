package syrincs.a_domain.rhythm.metricstate;

import java.math.BigInteger;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Deque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Measure {@code metric-state-v1}: exact minimum over all paths of the transition table of the
 * functional contract (rows 1-10 plus 5a-G, 5b-G and 4-G). Paths reaching the same state at the
 * same time are merged; there is no heuristic. A state without an applicable row is reported as
 * an {@link IllegalStateException}, never bridged by an invented rule.
 */
public final class MetricStateAnalyzer {
    public static final String VERSION = "metric-state-v1";
    public static final int DEFAULT_PATH_LIMIT = 16;

    /** Technical cap for the set of distinct minimal sequences; reported via a flag if hit. */
    public static final int MAX_DISTINCT_SEQUENCES = 10_000;

    private static final BigInteger TWO = BigInteger.TWO;

    public MetricStateAnalysis analyze(MetricExample example) {
        return analyze(example, DEFAULT_PATH_LIMIT);
    }

    public MetricStateAnalysis analyze(MetricFrame frame, String events, int pathLimit) {
        return analyze(MetricExample.parse(frame, events), pathLimit);
    }

    /**
     * @param pathLimit display limit: at most this many minimal paths are materialized with their
     *     protocol; count, sequences, ranges and values always cover all minimal paths
     */
    public MetricStateAnalysis analyze(MetricExample example, int pathLimit) {
        if (pathLimit < 0) throw new IllegalArgumentException("pathLimit must not be negative");
        int n = example.slots();
        MetricFrame frame = example.frame();

        // layers.get(t): states after the positions 0..t-1 have been evaluated.
        List<Map<MetricState, Node>> layers = new ArrayList<>(n + 1);
        Node root = new Node(null, 0);
        Map<MetricState, Node> current = new LinkedHashMap<>();
        layers.add(current);
        List<Node> sources = List.of(root);
        for (int t = 0; t < n; t++) {
            Map<MetricState, Node> next = new LinkedHashMap<>();
            EventKind event = example.eventAt(t);
            boolean barStart = t > 0 && example.isBarStart(t);
            for (Node source : sources) {
                MetricState before = barStart ? barChange(source.state, t) : source.state;
                for (Transition tr : transitions(frame, event, t, before)) {
                    int stepCost = tr.instructions.size() * Instruction.BITS;
                    int cost = source.cost + stepCost;
                    Node target = next.get(tr.after);
                    if (target == null) {
                        target = new Node(tr.after, cost);
                        next.put(tr.after, target);
                    } else if (cost < target.cost) {
                        target.cost = cost;
                        target.in.clear();
                    } else if (cost > target.cost) {
                        continue;
                    }
                    target.in.add(
                            new Edge(
                                    source,
                                    target,
                                    new ProtocolStep(
                                            t,
                                            event,
                                            barStart,
                                            before,
                                            tr.after,
                                            tr.rule,
                                            tr.instructions,
                                            stepCost)));
                }
            }
            layers.add(next);
            sources = new ArrayList<>(next.values());
        }

        int value = minCost(layers.get(n).values());
        List<Node> ends =
                layers.get(n).values().stream().filter(node -> node.cost == value).toList();

        // Nodes and edges lying on at least one minimal path, in layer order.
        Set<Node> relevant = new HashSet<>();
        Deque<Node> stack = new ArrayDeque<>(ends);
        relevant.addAll(ends);
        while (!stack.isEmpty())
            for (Edge edge : stack.pop().in) if (relevant.add(edge.from)) stack.push(edge.from);
        List<Node> ordered = new ArrayList<>();
        for (int t = 1; t <= n; t++)
            for (Node node : layers.get(t).values()) if (relevant.contains(node)) ordered.add(node);
        Map<Node, List<Edge>> out = new HashMap<>();
        for (Node node : ordered)
            for (Edge edge : node.in) out.computeIfAbsent(edge.from, k -> new ArrayList<>()).add(edge);

        BigInteger pathCount = countPaths(root, ordered, ends);
        boolean[] complete = {true};
        Set<List<Instruction>> sequences = distinctSequences(root, ordered, ends, complete);

        List<MetricInterpretation> paths = new ArrayList<>();
        collectPaths(root, out, n, new ArrayList<>(), paths, pathLimit);

        List<CostRange> barCosts = new ArrayList<>();
        for (int b = 0; b < example.bars(); b++)
            barCosts.add(range(b, b * frame.barSlots(), (b + 1) * frame.barSlots(), root, ordered, ends));
        List<CostRange> beatCosts = new ArrayList<>();
        for (int b = 0; b < example.beats(); b++)
            beatCosts.add(
                    range(b, b * frame.beatSlots(), (b + 1) * frame.beatSlots(), root, ordered, ends));

        List<Integer> valueAfterBars = new ArrayList<>();
        for (int b = 1; b <= example.bars(); b++)
            valueAfterBars.add(minCost(layers.get(b * frame.barSlots()).values()));

        return new MetricStateAnalysis(
                MetricStateMeasurementKey.of(frame),
                example,
                value,
                value / Instruction.BITS,
                pathCount,
                paths,
                sequences,
                complete[0],
                barCosts,
                beatCosts,
                valueAfterBars);
    }

    // ---- transition table -------------------------------------------------------------------

    /** Row 9: the anchor returns to the bar start; in N the downbeat itself is expected. */
    private static MetricState barChange(MetricState state, int t) {
        return state.mode() == PlayMode.NOTES
                ? MetricState.notes(state.delta(), t, BigInteger.valueOf(t))
                : MetricState.rests(state.delta(), t);
    }

    private static List<Transition> transitions(
            MetricFrame frame, EventKind event, int t, MetricState s) {
        boolean sustained = frame.sound() == SoundCondition.SUSTAINED;
        if (s == null) { // beginning of the example, rows 1 and 2
            BigInteger beat = BigInteger.valueOf(frame.beatSlots());
            if (event == EventKind.ONSET)
                return List.of(
                        new Transition(
                                TransitionRule.START_WITH_ONSET,
                                List.of(Instruction.PLAY_NOTES),
                                MetricState.notes(beat, 0, beat)));
            if (event == EventKind.SILENCE)
                return List.of(
                        new Transition(
                                TransitionRule.START_WITHOUT_ONSET,
                                List.of(Instruction.PLAY_RESTS),
                                MetricState.rests(beat, 0)));
            throw noRule(t, event, null);
        }
        BigInteger time = BigInteger.valueOf(t);
        if (s.mode() == PlayMode.RESTS) {
            if (event == EventKind.SILENCE)
                return List.of(new Transition(TransitionRule.SILENCE_IN_RESTS, List.of(), s));
            if (event != EventKind.ONSET) throw noRule(t, event, s);
            List<Instruction> instructions = new ArrayList<>();
            BigInteger delta = refineUntilOnGrid(s, t, event, instructions);
            TransitionRule rule =
                    instructions.isEmpty()
                            ? TransitionRule.ONSET_ON_SILENT_GRID
                            : TransitionRule.ONSET_OFF_SILENT_GRID;
            instructions.add(Instruction.PLAY_NOTES);
            return List.of(
                    new Transition(rule, instructions, MetricState.notes(delta, t, time.add(delta))));
        }

        // NOTES
        int position = time.compareTo(s.expectation());
        if (position > 0) throw noRule(t, event, s); // an expectation was never evaluated
        boolean atExpectation = position == 0;
        switch (event) {
            case ONSET -> {
                if (atExpectation)
                    return List.of(
                            new Transition(
                                    TransitionRule.ONSET_AT_EXPECTATION,
                                    List.of(),
                                    MetricState.notes(s.delta(), t, time.add(s.delta()))));
                List<Instruction> instructions = new ArrayList<>();
                BigInteger delta = refineUntilOnGrid(s, t, event, instructions);
                return List.of(
                        new Transition(
                                TransitionRule.ONSET_BEFORE_EXPECTATION,
                                instructions,
                                MetricState.notes(delta, t, time.add(delta))));
            }
            case REST_ONSET -> {
                if (atExpectation)
                    return List.of(
                            new Transition(
                                    TransitionRule.REST_ONSET_AT_EXPECTATION,
                                    List.of(Instruction.PLAY_RESTS),
                                    MetricState.rests(s.delta(), t)));
                List<Instruction> instructions = new ArrayList<>();
                BigInteger delta = refineUntilOnGrid(s, t, event, instructions);
                instructions.add(Instruction.PLAY_RESTS);
                return List.of(
                        new Transition(
                                TransitionRule.REST_ONSET_BEFORE_EXPECTATION,
                                instructions,
                                MetricState.rests(delta, t)));
            }
            case HOLD -> {
                if (!atExpectation)
                    return List.of(new Transition(TransitionRule.NO_CHANGE_IN_NOTES, List.of(), s));
                return List.of(coarsen(TransitionRule.HELD_TONE_AT_EXPECTATION, s, time));
            }
            case SILENCE -> {
                // Under SUSTAINED, N always sounds; plain silence in N has no row.
                if (sustained) throw noRule(t, event, s);
                if (!atExpectation)
                    return List.of(new Transition(TransitionRule.NO_CHANGE_IN_NOTES, List.of(), s));
                return List.of(
                        new Transition(
                                TransitionRule.MISSING_ONSET_PLAY_RESTS,
                                List.of(Instruction.PLAY_RESTS),
                                MetricState.rests(s.delta(), s.anchor())),
                        coarsen(TransitionRule.MISSING_ONSET_COARSEN, s, time));
            }
        }
        throw noRule(t, event, s);
    }

    /** Rows 5b and 5b-G: one step only; next expectation is A + Δ with the new Δ. */
    private static Transition coarsen(TransitionRule rule, MetricState s, BigInteger time) {
        BigInteger delta = s.delta().multiply(TWO);
        BigInteger expectation = BigInteger.valueOf(s.anchor()).add(delta);
        // By construction A + Δ(new) lies after t. Guard only: smallest A + k·Δ after t.
        while (expectation.compareTo(time) <= 0) expectation = expectation.add(delta);
        return new Transition(
                rule,
                List.of(Instruction.COARSEN),
                MetricState.notes(delta, s.anchor(), expectation));
    }

    /** {@code 00} as often as needed until (t − A) is a multiple of Δ; returns the new Δ. */
    private static BigInteger refineUntilOnGrid(
            MetricState s, int t, EventKind event, List<Instruction> instructions) {
        BigInteger distance = BigInteger.valueOf(t - s.anchor());
        if (distance.signum() < 0) throw noRule(t, event, s);
        BigInteger delta = s.delta();
        while (distance.mod(delta).signum() != 0) {
            if (delta.testBit(0)) throw noRule(t, event, s); // Δ would fall below the grid
            delta = delta.shiftRight(1);
            instructions.add(Instruction.REFINE);
        }
        return delta;
    }

    private static IllegalStateException noRule(int t, EventKind event, MetricState state) {
        return new IllegalStateException(
                "No transition rule applies at position "
                        + t
                        + " for event "
                        + event
                        + " in state "
                        + (state == null ? "<beginning>" : state.toString()));
    }

    // ---- evaluation of the merged path graph -------------------------------------------------

    private static int minCost(Iterable<Node> nodes) {
        int min = Integer.MAX_VALUE;
        for (Node node : nodes) min = Math.min(min, node.cost);
        return min;
    }

    private static BigInteger countPaths(Node root, List<Node> ordered, List<Node> ends) {
        Map<Node, BigInteger> count = new HashMap<>();
        count.put(root, BigInteger.ONE);
        for (Node node : ordered) {
            BigInteger sum = BigInteger.ZERO;
            for (Edge edge : node.in) sum = sum.add(count.get(edge.from));
            count.put(node, sum);
        }
        BigInteger total = BigInteger.ZERO;
        for (Node end : ends) total = total.add(count.get(end));
        return total;
    }

    private static Set<List<Instruction>> distinctSequences(
            Node root, List<Node> ordered, List<Node> ends, boolean[] complete) {
        Map<Node, Set<List<Instruction>>> sets = new HashMap<>();
        sets.put(root, Set.of(List.of()));
        for (Node node : ordered) {
            Set<List<Instruction>> set = new LinkedHashSet<>();
            for (Edge edge : node.in)
                for (List<Instruction> prefix : sets.get(edge.from)) {
                    if (set.size() >= MAX_DISTINCT_SEQUENCES) {
                        complete[0] = false;
                        break;
                    }
                    if (edge.step.instructions().isEmpty()) {
                        set.add(prefix);
                    } else {
                        List<Instruction> extended = new ArrayList<>(prefix);
                        extended.addAll(edge.step.instructions());
                        set.add(List.copyOf(extended));
                    }
                }
            sets.put(node, set);
        }
        List<List<Instruction>> all = new ArrayList<>();
        Set<List<Instruction>> seen = new HashSet<>();
        for (Node end : ends) for (List<Instruction> s : sets.get(end)) if (seen.add(s)) all.add(s);
        all.sort(Comparator.comparing(MetricStateAnalyzer::codeString));
        if (all.size() > MAX_DISTINCT_SEQUENCES) {
            complete[0] = false;
            all = all.subList(0, MAX_DISTINCT_SEQUENCES);
        }
        return new LinkedHashSet<>(all);
    }

    private static String codeString(List<Instruction> sequence) {
        StringBuilder text = new StringBuilder();
        for (Instruction instruction : sequence) text.append(instruction.code());
        return text.toString();
    }

    private static void collectPaths(
            Node node,
            Map<Node, List<Edge>> out,
            int length,
            List<ProtocolStep> steps,
            List<MetricInterpretation> paths,
            int limit) {
        if (paths.size() >= limit) return;
        if (steps.size() == length) {
            List<Instruction> instructions = new ArrayList<>();
            int cost = 0;
            for (ProtocolStep step : steps) {
                instructions.addAll(step.instructions());
                cost += step.costBits();
            }
            paths.add(new MetricInterpretation(instructions, steps, cost));
            return;
        }
        for (Edge edge : out.getOrDefault(node, List.of())) {
            steps.add(edge.step);
            collectPaths(edge.to, out, length, steps, paths, limit);
            steps.remove(steps.size() - 1);
            if (paths.size() >= limit) return;
        }
    }

    /** Smallest and largest cost share of [start, end) over all minimal paths. */
    private static CostRange range(
            int index, int start, int end, Node root, List<Node> ordered, List<Node> ends) {
        Map<Node, int[]> share = new HashMap<>();
        share.put(root, new int[] {0, 0});
        for (Node node : ordered) {
            int min = Integer.MAX_VALUE, max = Integer.MIN_VALUE;
            for (Edge edge : node.in) {
                int[] from = share.get(edge.from);
                int p = edge.step.position();
                int w = p >= start && p < end ? edge.step.costBits() : 0;
                min = Math.min(min, from[0] + w);
                max = Math.max(max, from[1] + w);
            }
            share.put(node, new int[] {min, max});
        }
        int min = Integer.MAX_VALUE, max = Integer.MIN_VALUE;
        for (Node node : ends) {
            min = Math.min(min, share.get(node)[0]);
            max = Math.max(max, share.get(node)[1]);
        }
        return new CostRange(index, start, end, min, max);
    }

    private record Transition(TransitionRule rule, List<Instruction> instructions, MetricState after) {}

    private record Edge(Node from, Node to, ProtocolStep step) {}

    /** Merged state at one time with minimal cost and all predecessors of that cost. */
    private static final class Node {
        final MetricState state;
        int cost;
        final List<Edge> in = new ArrayList<>();

        Node(MetricState state, int cost) {
            this.state = state;
            this.cost = cost;
        }
    }
}
