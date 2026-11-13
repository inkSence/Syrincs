package syrincs.b_application.rhythm.metricstate;

import syrincs.a_domain.rhythm.metricstate.MetricStateAnalysis;

import java.util.Objects;

/** Result of {@link AnalyzeMetricStateUseCase}: the analysis and the display limit it used. */
public record MetricStateResult(MetricStateAnalysis analysis, int displayLimit) {
    public MetricStateResult {
        Objects.requireNonNull(analysis, "Analysis");
    }
}
