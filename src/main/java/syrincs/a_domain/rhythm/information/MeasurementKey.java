package syrincs.a_domain.rhythm.information;

import java.util.List;

/** The full actual history identifies the measurement even when its last 32 slots suffice for K. */
public record MeasurementKey(
        String measureId,
        String grammarVersion,
        Scope scope,
        String contextPolicy,
        AnalysisFrame frame,
        Segmentation segmentation,
        String initialHistory,
        List<MetricReference> metricReferences,
        String referenceProfile) {
    public MeasurementKey {
        metricReferences = List.copyOf(metricReferences);
    }

    public static MeasurementKey of(AnalysisSettings settings) {
        return new MeasurementKey(
                ExactDescriptionAnalyzer.VERSION,
                ExactDescriptionAnalyzer.VERSION,
                settings.scope(),
                settings.scope() == Scope.CAUSAL_WINDOWS ? ContextDictionary.VERSION : "NONE",
                settings.frame(),
                settings.segmentation(),
                settings.initialHistory(),
                settings.references(),
                settings.referenceProfile());
    }
}
