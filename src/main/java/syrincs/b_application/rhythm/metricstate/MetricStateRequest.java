package syrincs.b_application.rhythm.metricstate;

import syrincs.a_domain.rhythm.metricstate.SoundCondition;

import java.util.Objects;

/**
 * Framework-free order for one {@code metric-state-v1} analysis. The sound condition and the
 * frame have no defaults; {@code displayLimit} only limits the displayed minimal paths.
 */
public record MetricStateRequest(
        SoundCondition sound, int beatSlots, int barSlots, String events, int displayLimit) {
    public MetricStateRequest {
        Objects.requireNonNull(sound, "Sound condition is required; there is no default");
        Objects.requireNonNull(events, "Events");
    }
}
