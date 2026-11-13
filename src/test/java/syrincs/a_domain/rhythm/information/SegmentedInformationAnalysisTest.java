package syrincs.a_domain.rhythm.information;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

import java.util.List;

class SegmentedInformationAnalysisTest {
    private final RhythmInformationAnalyzer analyzer = new RhythmInformationAnalyzer();

    @Test
    void globalIsolatedAndCausalAreDifferentMeasurementContracts() {
        var sequence = new OnsetSequence("xooo".repeat(8));
        assertEquals(14, analyzer.analyze(sequence, AnalysisSettings.global(32)).totalBits());
        var isolated =
                analyzer.analyze(
                        sequence,
                        new AnalysisSettings(
                                AnalysisFrame.unknown(32),
                                Scope.ISOLATED_WINDOWS,
                                Segmentation.equal(32, 16, 16),
                                "",
                                List.of(),
                                null));
        assertEquals(26, isolated.totalBits());
        assertEquals(List.of(13, 13), isolated.barContributions());
        var causal =
                analyzer.analyze(
                        sequence,
                        new AnalysisSettings(
                                AnalysisFrame.unknown(32),
                                Scope.CAUSAL_WINDOWS,
                                Segmentation.equal(32, 4, 16),
                                "",
                                List.of(),
                                null));
        assertEquals(34, causal.totalBits());
        assertEquals(List.of(22, 12), causal.barContributions());
        assertEquals(List.of(8, 8, 3, 3, 3, 3, 3, 3), causal.windowMetrics().values());
        assertEquals(List.of(1, 2), causal.windowMetrics().peakPositions());
        assertNotEquals(causal.measurementKey(), isolated.measurementKey());
        assertEquals(
                "xooo".repeat(7), causal.windows().getLast().measurementKey().initialHistory());
    }

    @Test
    void contextRemembersActualPastAndNeverFuture() {
        var settings =
                new AnalysisSettings(
                        AnalysisFrame.unknown(16),
                        Scope.CAUSAL_WINDOWS,
                        Segmentation.equal(16, 4, 16),
                        "",
                        List.of(),
                        null);
        var result = analyzer.analyze(new OnsetSequence("xooo".repeat(4)), settings);
        assertEquals(List.of(8, 8, 3, 3), result.windowMetrics().values());
        assertEquals(Rational.of(25, 4), result.windowMetrics().variance());
        assertEquals(List.of("0", "1000"), result.windows().getLast().contextDictionary());
        var changed = analyzer.analyze(new OnsetSequence("xoooxoooxoxoxoxo"), settings);
        assertEquals(
                result.windows().subList(0, 2).stream().map(w -> w.witness().code()).toList(),
                changed.windows().subList(0, 2).stream().map(w -> w.witness().code()).toList());
        var known =
                analyzer.analyze(
                        new OnsetSequence("xoxo".repeat(4)),
                        new AnalysisSettings(
                                settings.frame(),
                                settings.scope(),
                                settings.segmentation(),
                                "xoxoxoxo",
                                List.of(),
                                null));
        assertEquals(List.of(2, 2, 2, 2), known.windowMetrics().values());
    }

    @Test
    void primitiveDictionaryAndPhaseFollowSuffixExactly() {
        assertEquals(List.of("10"), ContextDictionary.fromHistory("10".repeat(20)));
        assertEquals(List.of("01"), ContextDictionary.fromHistory("01".repeat(20)));
        assertEquals(List.of(), ContextDictionary.fromHistory("1"));
    }
}
