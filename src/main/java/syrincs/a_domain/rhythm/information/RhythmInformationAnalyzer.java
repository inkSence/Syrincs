package syrincs.a_domain.rhythm.information;

import java.util.ArrayList;
import java.util.List;

/** Shared exact evaluation used by public analysis and every generated result. */
public final class RhythmInformationAnalyzer {
    public InformationAnalysis analyze(OnsetSequence sequence, AnalysisSettings settings) {
        if (sequence.slots() != settings.frame().slots())
            throw new IllegalArgumentException("Word/frame length mismatch");
        ExactDescriptionAnalyzer.Engine engine = new ExactDescriptionAnalyzer.Engine();
        List<Integer> boundaries =
                settings.segmentation() == null
                        ? List.of(0, sequence.slots())
                        : settings.segmentation().boundaries();
        List<InformationAnalysis.Window> windows = new ArrayList<>();
        String history = settings.initialHistory();
        DescriptionDecoder decoder = new DescriptionDecoder();
        for (int i = 1; i < boundaries.size(); i++) {
            int start = boundaries.get(i - 1), end = boundaries.get(i);
            OnsetSequence word =
                    new OnsetSequence(sequence.normalizedOnsets().substring(start, end));
            String historyBits = history.replace('x', '1').replace('o', '0');
            boolean causal = settings.scope() == Scope.CAUSAL_WINDOWS;
            OptimalCodes optimal =
                    causal
                            ? engine.conditional(word.bits(), historyBits)
                            : engine.base(word.bits());
            AnalysisFrame original = settings.frame();
            AnalysisFrame localFrame =
                    new AnalysisFrame(
                            word.slots(),
                            original.slotDurationMs(),
                            original.quarterBpm(),
                            original.ticksPerQuarter(),
                            original.numerator(),
                            original.denominator());
            AnalysisSettings local =
                    new AnalysisSettings(
                            localFrame,
                            causal ? Scope.CAUSAL_WINDOWS : Scope.GLOBAL_DESCRIPTION,
                            causal ? Segmentation.equal(word.slots(), word.slots(), null) : null,
                            causal ? history : "",
                            List.of(),
                            null);
            var decoded =
                    decoder.decode(
                            optimal.canonicalCode(),
                            word.slots(),
                            local.scope(),
                            local.segmentation(),
                            local.historyBits());
            if (!decoded.bits().equals(word.bits()))
                throw new IllegalStateException(
                        "Canonical witness failed independent reconstruction");
            windows.add(
                    new InformationAnalysis.Window(
                            i,
                            start,
                            end,
                            word,
                            MeasurementKey.of(local),
                            decoded.messages().getFirst(),
                            optimal,
                            causal ? ContextDictionary.fromHistory(historyBits) : List.of()));
            if (causal) history += word.normalizedOnsets();
        }
        List<Integer> values = windows.stream().map(InformationAnalysis.Window::totalBits).toList();
        List<Integer> bars = new ArrayList<>();
        if (settings.segmentation() != null && !settings.segmentation().barBoundaries().isEmpty()) {
            var barBounds = settings.segmentation().barBoundaries();
            for (int i = 1; i < barBounds.size(); i++) {
                int start = barBounds.get(i - 1), end = barBounds.get(i);
                bars.add(
                        windows.stream()
                                .filter(w -> w.start() >= start && w.end() <= end)
                                .mapToInt(InformationAnalysis.Window::totalBits)
                                .sum());
            }
        }
        List<ReferenceAssessment> references =
                settings.references().isEmpty()
                        ? List.of(
                                ReferenceAssessment.assess(
                                        null, settings.frame(), settings.referenceProfile()))
                        : settings.references().stream()
                                .map(
                                        r ->
                                                ReferenceAssessment.assess(
                                                        r,
                                                        settings.frame(),
                                                        settings.referenceProfile()))
                                .toList();
        String basis =
                settings.scope() == Scope.CAUSAL_WINDOWS ? "CAUSAL_WINDOWS" : "ISOLATED_WINDOWS";
        return new InformationAnalysis(
                sequence,
                MeasurementKey.of(settings),
                "BITS",
                values.stream().mapToInt(Integer::intValue).sum(),
                windows,
                bars,
                settings.scope() == Scope.GLOBAL_DESCRIPTION
                        ? null
                        : ProfileMetrics.of(basis, values),
                bars.isEmpty() ? null : ProfileMetrics.of(basis + "_BAR_CONTRIBUTIONS", bars),
                references);
    }
}
