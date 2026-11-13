package syrincs.b_application.rhythm.information;

import syrincs.a_domain.rhythm.HuffmanRhythm;
import syrincs.a_domain.rhythm.information.*;
import syrincs.b_application.AppDefaults;
import syrincs.b_application.ports.RhythmInformationAnalyzerPort;

import java.util.List;
import java.util.Objects;

public final class AnalyzeRhythmInformationUseCase {
    private final RhythmInformationAnalyzerPort analyzer;

    public AnalyzeRhythmInformationUseCase(RhythmInformationAnalyzerPort analyzer) {
        this.analyzer = Objects.requireNonNull(analyzer);
    }

    public AnalysisResult execute(
            OnsetSequence sequence,
            AnalysisSettings settings,
            boolean includeGlobal,
            boolean compareLegacy) {
        InformationAnalysis primary = analyzer.analyze(sequence, settings);
        InformationAnalysis global =
                includeGlobal && settings.scope() != Scope.GLOBAL_DESCRIPTION
                        ? analyzer.analyze(sequence, settings.global())
                        : null;
        AnalysisResult.LegacyComparison legacy = null;
        if (compareLegacy) {
            var f = settings.frame();
            if (Objects.equals(f.numerator(), 4)
                    && Objects.equals(f.denominator(), 4)
                    && Objects.equals(f.ticksPerQuarter(), 4)
                    && f.slots() % 16 == 0) {
                var old =
                        new HuffmanRhythm(
                                4, 4, AppDefaults.DEFAULT_TEMPO_BPM, sequence.normalizedOnsets());
                legacy =
                        new AnalysisResult.LegacyComparison(
                                "AVAILABLE",
                                null,
                                "legacy-fsm-v1",
                                "CODE_SYMBOLS",
                                old.getInformation(),
                                old.getBeatInformation(),
                                old.getStandardDeviation(),
                                4,
                                4,
                                4,
                                new AnalysisResult.LegacyMeasurementKey(
                                        "legacy-fsm-v1",
                                        "LEGACY_CONTINUOUS_BEATS",
                                        sequence.slots(),
                                        4,
                                        4,
                                        4,
                                        "REST",
                                        "QUARTER_PER_BEAT"));
            } else
                legacy =
                        new AnalysisResult.LegacyComparison(
                                "UNAVAILABLE",
                                "Requires explicit 4/4 notation, four slots per quarter and whole"
                                    + " bars",
                                "legacy-fsm-v1",
                                "CODE_SYMBOLS",
                                null,
                                List.of(),
                                null,
                                null,
                                null,
                                null,
                                null);
        }
        return new AnalysisResult(primary, global, legacy);
    }
}
