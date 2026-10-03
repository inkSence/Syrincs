package syrincs.b_application.ports.dto;

/**
 * Optional inclusive bounds on stored beat-information deviation.
 * A null bound is absent; two absent bounds mean no deviation predicate.
 * This does not represent the legacy strict-minimum playback default.
 */
public record DeviationRange(Double min, Double max) {
    public DeviationRange {
        validateBound(min, "--deviation-min");
        validateBound(max, "--deviation-max");
        if (min != null && max != null && min > max) {
            throw new IllegalArgumentException("--deviation-min must not exceed --deviation-max");
        }
    }

    private static void validateBound(Double bound, String option) {
        if (bound != null && (!Double.isFinite(bound) || bound < 0)) {
            throw new IllegalArgumentException(option + " must be finite and non-negative");
        }
    }
}
