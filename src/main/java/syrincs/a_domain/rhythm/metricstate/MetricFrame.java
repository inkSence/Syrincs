package syrincs.a_domain.rhythm.metricstate;

import java.util.Objects;

/**
 * Given metric frame: grid positions per beat and per bar plus the sound condition. The initial
 * spacing Δ is one beat. Only binary division is supported, hence the power of two.
 */
public record MetricFrame(int beatSlots, int barSlots, SoundCondition sound) {
    public MetricFrame {
        Objects.requireNonNull(sound, "Sound condition is required; there is no default");
        if (beatSlots < 1 || Integer.bitCount(beatSlots) != 1)
            throw new IllegalArgumentException(
                    "beatSlots must be a power of two >= 1, got " + beatSlots);
        if (barSlots < 1 || barSlots % beatSlots != 0)
            throw new IllegalArgumentException(
                    "barSlots must be a positive multiple of beatSlots ("
                            + beatSlots
                            + "), got "
                            + barSlots);
    }
}
