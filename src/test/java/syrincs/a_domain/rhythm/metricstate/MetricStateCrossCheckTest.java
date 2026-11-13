package syrincs.a_domain.rhythm.metricstate;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

/**
 * Compares the merging analyzer with a naive, independently written enumeration of all
 * instruction choices (no state merging, plain ints). Differences would point at the merge,
 * the counting, the cost ranges or the prefix values.
 */
class MetricStateCrossCheckTest {
    private static final MetricStateAnalyzer ANALYZER = new MetricStateAnalyzer();

    /** Plain state: mode, delta, anchor, expectation (-1 in P). */
    private record S(boolean notes, int delta, int anchor, int exp) {}

    private record Path(List<String> codes, int[] costAtPosition) {}

    private static List<Path> enumerate(boolean sustained, int beat, int bar, String ev) {
        List<Path> out = new ArrayList<>();
        walk(sustained, beat, bar, ev, 0, null, new ArrayList<>(), new int[ev.length()], out);
        return out;
    }

    private static void walk(
            boolean sustained,
            int beat,
            int bar,
            String ev,
            int t,
            S s,
            List<String> codes,
            int[] costs,
            List<Path> out) {
        if (t == ev.length()) {
            out.add(new Path(List.copyOf(codes), costs.clone()));
            return;
        }
        if (t > 0 && t % bar == 0) s = s.notes ? new S(true, s.delta, t, t) : new S(false, s.delta, t, -1);
        char c = ev.charAt(t);
        boolean restOnset = sustained && c == 'o' && t > 0 && ev.charAt(t - 1) != 'o';
        List<List<String>> addedList = new ArrayList<>();
        List<S> nextList = new ArrayList<>();
        if (s == null) {
            addedList.add(List.of(c == 'x' ? "10" : "11"));
            nextList.add(c == 'x' ? new S(true, beat, 0, beat) : new S(false, beat, 0, -1));
        } else if (!s.notes) {
            if (c == 'x') {
                int d = s.delta;
                List<String> added = new ArrayList<>();
                while ((t - s.anchor) % d != 0) {
                    d /= 2;
                    added.add("00");
                }
                added.add("10");
                addedList.add(added);
                nextList.add(new S(true, d, t, t + d));
            } else {
                assertEquals('o', c);
                addedList.add(List.of());
                nextList.add(s);
            }
        } else if (c == 'x') {
            int d = s.delta;
            List<String> added = new ArrayList<>();
            if (t != s.exp) {
                assertTrue(t < s.exp);
                while ((t - s.anchor) % d != 0) {
                    d /= 2;
                    added.add("00");
                }
            }
            addedList.add(added);
            nextList.add(new S(true, d, t, t + d));
        } else if (c == '=') {
            if (t == s.exp) {
                addedList.add(List.of("01"));
                nextList.add(coarse(s, t));
            } else {
                addedList.add(List.of());
                nextList.add(s);
            }
        } else if (restOnset) {
            int d = s.delta;
            List<String> added = new ArrayList<>();
            if (t != s.exp) {
                assertTrue(t < s.exp);
                while ((t - s.anchor) % d != 0) {
                    d /= 2;
                    added.add("00");
                }
            }
            added.add("11");
            addedList.add(added);
            nextList.add(new S(false, d, t, -1));
        } else {
            assertFalse(sustained, "plain silence in N under SUSTAINED");
            if (t == s.exp) {
                addedList.add(List.of("11"));
                nextList.add(new S(false, s.delta, s.anchor, -1));
                addedList.add(List.of("01"));
                nextList.add(coarse(s, t));
            } else {
                addedList.add(List.of());
                nextList.add(s);
            }
        }
        for (int i = 0; i < nextList.size(); i++) {
            int before = codes.size();
            codes.addAll(addedList.get(i));
            costs[t] = 2 * addedList.get(i).size();
            walk(sustained, beat, bar, ev, t + 1, nextList.get(i), codes, costs, out);
            while (codes.size() > before) codes.remove(codes.size() - 1);
        }
        costs[t] = 0;
    }

    private static S coarse(S s, int t) {
        int d = s.delta * 2;
        int e = s.anchor + d;
        while (e <= t) e += d;
        return new S(true, d, s.anchor, e);
    }

    private static String randomEvents(Random random, boolean sustained, int length) {
        StringBuilder text = new StringBuilder();
        for (int i = 0; i < length; i++) {
            boolean sounding = i > 0 && text.charAt(i - 1) != 'o';
            double r = random.nextDouble();
            if (sustained && sounding) text.append(r < 0.4 ? '=' : r < 0.7 ? 'o' : 'x');
            else text.append(r < (sustained ? 0.35 : 0.3) ? 'x' : 'o');
        }
        return text.toString();
    }

    @Test
    void mergedSearchAgreesWithNaiveEnumerationOnRandomExamples() {
        Random random = new Random(20261113L);
        int[][] frames = {{4, 16}, {4, 8}, {2, 4}, {2, 8}, {1, 2}, {1, 4}, {8, 16}};
        int checked = 0;
        for (SoundCondition sound : SoundCondition.values())
            for (int[] f : frames)
                for (int round = 0; round < 60; round++) {
                    int bars = 1 + random.nextInt(Math.max(1, 28 / f[1]) + 1);
                    String events = randomEvents(random, sound == SoundCondition.SUSTAINED, bars * f[1]);
                    compare(sound, f[0], f[1], events);
                    checked++;
                }
        assertEquals(2 * frames.length * 60, checked);
    }

    private static void compare(SoundCondition sound, int beat, int bar, String events) {
        String label = sound + " " + beat + "/" + bar + " " + events;
        boolean sustained = sound == SoundCondition.SUSTAINED;
        List<Path> all = enumerate(sustained, beat, bar, events);
        int min = Integer.MAX_VALUE;
        for (Path p : all) min = Math.min(min, 2 * p.codes.size());
        List<Path> minimal = new ArrayList<>();
        for (Path p : all) if (2 * p.codes.size() == min) minimal.add(p);

        MetricStateAnalysis a =
                ANALYZER.analyze(MetricExample.parse(new MetricFrame(beat, bar, sound), events), 100_000);
        assertEquals(min, a.valueBits(), label);
        assertEquals(BigInteger.valueOf(minimal.size()), a.minimalPathCount(), label);

        Set<List<String>> sequences = new LinkedHashSet<>();
        for (Path p : minimal) sequences.add(p.codes);
        assertEquals(sequences, a.minimalSequenceCodes(), label);

        Map<List<String>, Integer> expectedMultiset = new HashMap<>();
        for (Path p : minimal) expectedMultiset.merge(p.codes, 1, Integer::sum);
        Map<List<String>, Integer> actualMultiset = new HashMap<>();
        for (MetricInterpretation p : a.minimalPaths()) actualMultiset.merge(p.codes(), 1, Integer::sum);
        assertEquals(expectedMultiset, actualMultiset, label);

        for (CostRange r : a.barCosts()) assertRange(r, minimal, label);
        for (CostRange r : a.beatCosts()) assertRange(r, minimal, label);

        for (int b = 1; b <= events.length() / bar; b++) {
            String prefix = events.substring(0, b * bar);
            int prefixMin = Integer.MAX_VALUE;
            for (Path p : enumerate(sustained, beat, bar, prefix))
                prefixMin = Math.min(prefixMin, 2 * p.codes.size());
            assertEquals(prefixMin, a.valueAfterBars().get(b - 1), label + " prefix " + b);
        }
        if (sustained) assertEquals(1, all.size(), label + " deterministic under SUSTAINED");
    }

    private static void assertRange(CostRange r, List<Path> minimal, String label) {
        int lo = Integer.MAX_VALUE, hi = Integer.MIN_VALUE;
        for (Path p : minimal) {
            int sum = 0;
            for (int t = r.startPosition(); t < r.endPosition(); t++) sum += p.costAtPosition[t];
            lo = Math.min(lo, sum);
            hi = Math.max(hi, sum);
        }
        assertEquals(lo, r.minBits(), label + " " + r);
        assertEquals(hi, r.maxBits(), label + " " + r);
    }
}
