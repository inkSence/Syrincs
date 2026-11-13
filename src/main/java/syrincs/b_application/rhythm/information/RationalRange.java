package syrincs.b_application.rhythm.information;

import syrincs.a_domain.rhythm.information.Rational;

public record RationalRange(Rational min, Rational max) {
    public RationalRange {
        if (min != null && min.compareTo(Rational.ZERO) < 0
                || max != null && max.compareTo(Rational.ZERO) < 0
                || min != null && max != null && min.compareTo(max) > 0)
            throw new IllegalArgumentException("Invalid nonnegative inclusive rational range");
    }

    public boolean contains(Rational value) {
        return (min == null || value.compareTo(min) >= 0)
                && (max == null || value.compareTo(max) <= 0);
    }

    public boolean containsDeviationVariance(Rational variance) {
        return (min == null || variance.compareTo(min.square()) >= 0)
                && (max == null || variance.compareTo(max.square()) <= 0);
    }
}
