package syrincs.a_domain.rhythm.metricstate;

/** Identifies the measurement; values of different keys are not comparable. */
public record MetricStateMeasurementKey(
        String measureVersion, SoundCondition sound, int beatSlots, int barSlots) {
    public static MetricStateMeasurementKey of(MetricFrame frame) {
        return new MetricStateMeasurementKey(
                MetricStateAnalyzer.VERSION, frame.sound(), frame.beatSlots(), frame.barSlots());
    }
}
