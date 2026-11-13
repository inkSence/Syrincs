package syrincs.c_adapters.cli;

import syrincs.a_domain.rhythm.metricstate.CostRange;
import syrincs.a_domain.rhythm.metricstate.MetricInterpretation;
import syrincs.a_domain.rhythm.metricstate.MetricStateAnalysis;
import syrincs.a_domain.rhythm.metricstate.ProtocolStep;
import syrincs.b_application.rhythm.metricstate.MetricStateResult;

import java.io.PrintWriter;
import java.util.List;
import java.util.stream.Collectors;

/** Text rendering of the {@code metric-state-v1} analysis; same bracket style as rhythm-info. */
final class MetricStateText {
    private MetricStateText() {}

    static void print(MetricStateResult result, PrintWriter out) {
        MetricStateAnalysis a = result.analysis();
        var key = a.key();
        out.printf(
                "[METRIC-STATE] Measure=%s | Sound=%s | Unit=BITS | Optimality=EXACT%n",
                key.measureVersion(), key.sound());
        out.printf(
                "[FRAME] BeatSlots=%d | BarSlots=%d | Slots=%d | Bars=%d | Events=%s%n",
                key.beatSlots(),
                key.barSlots(),
                a.example().slots(),
                a.example().bars(),
                grouped(a.example().normalizedEvents(), key.beatSlots()));
        int distinct = a.minimalSequenceCodes().size();
        out.printf(
                "[VALUE] Bits=%d | Instructions=%d | MinimalPaths=%s | DistinctSequences=%s%s%n",
                a.valueBits(),
                a.instructionCount(),
                a.minimalPathCount(),
                a.minimalSequencesComplete() ? distinct : "at least " + distinct,
                a.isAmbiguous() ? " | Interpretation=OPEN" : " | Interpretation=UNIQUE");
        if (a.isAmbiguous())
            out.printf(
                    "[NOTE] Interpretation is open: %s minimal paths are equally cheap; no single"
                            + " reading is preferred.%n",
                    a.minimalPathCount());
        if (a.minimalPathsTruncated())
            out.printf(
                    "[NOTE] Display truncated: showing %d of %s minimal paths (--alternatives-limit"
                            + " %d); counts, value and cost ranges cover all minimal paths.%n",
                    a.minimalPaths().size(), a.minimalPathCount(), result.displayLimit());
        int number = 0;
        for (MetricInterpretation path : a.minimalPaths()) {
            number++;
            out.printf(
                    "[PATH %d/%d] Instructions=%s | Bits=%d%n",
                    number, a.minimalPaths().size(), codes(path.codes()), path.costBits());
            for (ProtocolStep step : path.protocol()) {
                if (step.instructions().isEmpty() && !step.barStart()) continue;
                out.printf(
                        "[STEP] Position=%d | Event=%s | Row=%s | Instructions=%s | Bits=%d |"
                                + " After=%s%s%n",
                        step.position(),
                        step.event(),
                        step.rule().row(),
                        codes(step.instructions().stream().map(i -> i.code()).toList()),
                        step.costBits(),
                        step.stateAfter(),
                        step.barStart() ? " | BarStart=yes" : "");
            }
        }
        out.printf("[BAR-COSTS] %s%n", ranges(a.barCosts()));
        out.printf("[BEAT-COSTS] %s%n", ranges(a.beatCosts()));
        out.printf(
                "[VALUE-AFTER-BARS] %s%n",
                a.valueAfterBars().stream().map(String::valueOf).collect(Collectors.joining(" ")));
    }

    private static String codes(List<String> codes) {
        return codes.isEmpty() ? "(none)" : String.join(" ", codes);
    }

    private static String ranges(List<CostRange> ranges) {
        return ranges.stream()
                .map(
                        r ->
                                (r.index() + 1)
                                        + " ["
                                        + r.startPosition()
                                        + ","
                                        + r.endPosition()
                                        + ")="
                                        + (r.minBits() == r.maxBits()
                                                ? String.valueOf(r.minBits())
                                                : r.minBits() + "–" + r.maxBits()))
                .collect(Collectors.joining(" | "));
    }

    private static String grouped(String events, int group) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < events.length(); i += group) {
            if (i > 0) sb.append(' ');
            sb.append(events, i, Math.min(events.length(), i + group));
        }
        return sb.toString();
    }
}
