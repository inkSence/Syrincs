package syrincs.b_application.rhythm.metricstate;

import syrincs.a_domain.rhythm.metricstate.MetricExample;
import syrincs.a_domain.rhythm.metricstate.MetricFrame;
import syrincs.b_application.ports.MetricStateAnalyzerPort;

import java.util.Objects;

/** Validates the order, calls the analyzer and passes the display limit through. */
public final class AnalyzeMetricStateUseCase {
    private final MetricStateAnalyzerPort analyzer;

    public AnalyzeMetricStateUseCase(MetricStateAnalyzerPort analyzer) {
        this.analyzer = Objects.requireNonNull(analyzer);
    }

    /**
     * @throws IllegalArgumentException for an invalid frame, invalid events or a non-positive
     *     display limit
     */
    public MetricStateResult execute(MetricStateRequest request) {
        Objects.requireNonNull(request, "Request");
        if (request.displayLimit() <= 0)
            throw new IllegalArgumentException("Alternatives limit must be positive");
        MetricFrame frame =
                new MetricFrame(request.beatSlots(), request.barSlots(), request.sound());
        MetricExample example = MetricExample.parse(frame, request.events());
        return new MetricStateResult(
                analyzer.analyze(example, request.displayLimit()), request.displayLimit());
    }
}
