package syrincs.b_application.rhythm.information;

import syrincs.a_domain.rhythm.information.InformationAnalysis;

import java.util.List;

public record AnalysisResult(
        InformationAnalysis primary,
        InformationAnalysis additionalGlobal,
        LegacyComparison legacyComparison) {
    public record LegacyComparison(
            String status,
            String reason,
            String measureId,
            String unit,
            Integer total,
            List<Integer> beatInformation,
            Double deviation,
            Integer numerator,
            Integer denominator,
            Integer slotsPerBeat,
            LegacyMeasurementKey measurementKey) {
        public LegacyComparison {
            beatInformation = List.copyOf(beatInformation);
        }
    }

    public record LegacyMeasurementKey(
            String measureId,
            String scope,
            int slots,
            int numerator,
            int denominator,
            int slotsPerBeat,
            String initialPlayingState,
            String subdivisionReset) {}
}
