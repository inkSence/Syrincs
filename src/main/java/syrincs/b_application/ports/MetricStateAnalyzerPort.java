package syrincs.b_application.ports;

import syrincs.a_domain.rhythm.metricstate.MetricExample;
import syrincs.a_domain.rhythm.metricstate.MetricStateAnalysis;

/** Port for the {@code metric-state-v1} analysis of one finite example. */
@FunctionalInterface
public interface MetricStateAnalyzerPort {
    /**
     * @param pathLimit display limit for materialized minimal paths; count and values always cover
     *     all minimal paths
     */
    MetricStateAnalysis analyze(MetricExample example, int pathLimit);
}
