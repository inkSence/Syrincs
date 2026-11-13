package syrincs.a_domain.rhythm.metricstate;

import java.math.BigInteger;
import java.util.Objects;

/**
 * Playing state. Spacing Δ, anchor A and expectation e are measured in grid positions. Δ is
 * unbounded upwards, hence {@link BigInteger}. The expectation exists only in {@link
 * PlayMode#NOTES} and is {@code null} in {@link PlayMode#RESTS}.
 */
public record MetricState(PlayMode mode, BigInteger delta, int anchor, BigInteger expectation) {
    public MetricState {
        Objects.requireNonNull(mode, "Mode");
        Objects.requireNonNull(delta, "Delta");
        if (delta.signum() <= 0) throw new IllegalArgumentException("Delta must be positive");
        if (anchor < 0) throw new IllegalArgumentException("Anchor must not be negative");
        if ((mode == PlayMode.NOTES) != (expectation != null))
            throw new IllegalArgumentException("Exactly the NOTES mode carries an expectation");
    }

    public static MetricState notes(BigInteger delta, int anchor, BigInteger expectation) {
        return new MetricState(PlayMode.NOTES, delta, anchor, expectation);
    }

    public static MetricState rests(BigInteger delta, int anchor) {
        return new MetricState(PlayMode.RESTS, delta, anchor, null);
    }

    @Override
    public String toString() {
        return mode == PlayMode.NOTES
                ? "N(delta=" + delta + ", anchor=" + anchor + ", expect=" + expectation + ")"
                : "P(delta=" + delta + ", anchor=" + anchor + ")";
    }
}
