package syrincs.a_domain.rhythm.information;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Objects;

/** Exact numbers for grid times and inclusive filter boundaries. */
public record Rational(BigInteger numerator, BigInteger denominator)
        implements Comparable<Rational> {
    public static final Rational ZERO = of(0);
    public static final Rational ONE = of(1);

    public Rational {
        Objects.requireNonNull(numerator);
        Objects.requireNonNull(denominator);
        if (denominator.signum() == 0) throw new IllegalArgumentException("Zero denominator");
        if (denominator.signum() < 0) {
            numerator = numerator.negate();
            denominator = denominator.negate();
        }
        BigInteger gcd = numerator.gcd(denominator);
        numerator = numerator.divide(gcd);
        denominator = denominator.divide(gcd);
    }

    public static Rational of(long n) {
        return new Rational(BigInteger.valueOf(n), BigInteger.ONE);
    }

    public static Rational of(long n, long d) {
        return new Rational(BigInteger.valueOf(n), BigInteger.valueOf(d));
    }

    public static Rational parse(String value) {
        Objects.requireNonNull(value);
        try {
            if (value.contains("/")) {
                String[] parts = value.split("/", -1);
                if (parts.length != 2)
                    throw new IllegalArgumentException("Invalid fraction: " + value);
                return new Rational(new BigInteger(parts[0]), new BigInteger(parts[1]));
            }
            BigDecimal decimal = new BigDecimal(value);
            return decimal.scale() < 0
                    ? new Rational(
                            decimal.unscaledValue().multiply(BigInteger.TEN.pow(-decimal.scale())),
                            BigInteger.ONE)
                    : new Rational(decimal.unscaledValue(), BigInteger.TEN.pow(decimal.scale()));
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Invalid finite rational number: " + value, e);
        }
    }

    public Rational add(Rational b) {
        return new Rational(
                numerator.multiply(b.denominator).add(b.numerator.multiply(denominator)),
                denominator.multiply(b.denominator));
    }

    public Rational subtract(Rational b) {
        return add(new Rational(b.numerator.negate(), b.denominator));
    }

    public Rational multiply(Rational b) {
        return new Rational(numerator.multiply(b.numerator), denominator.multiply(b.denominator));
    }

    public Rational multiply(long b) {
        return multiply(of(b));
    }

    public Rational divide(Rational b) {
        return new Rational(numerator.multiply(b.denominator), denominator.multiply(b.numerator));
    }

    public Rational square() {
        return multiply(this);
    }

    public BigInteger floor() {
        BigInteger[] qr = numerator.divideAndRemainder(denominator);
        return qr[1].signum() < 0 ? qr[0].subtract(BigInteger.ONE) : qr[0];
    }

    public BigInteger ceil() {
        return new Rational(numerator.negate(), denominator).floor().negate();
    }

    public boolean isInteger() {
        return denominator.equals(BigInteger.ONE);
    }

    public double doubleValue() {
        return numerator.doubleValue() / denominator.doubleValue();
    }

    @Override
    public int compareTo(Rational b) {
        return numerator.multiply(b.denominator).compareTo(b.numerator.multiply(denominator));
    }

    @Override
    public String toString() {
        return numerator + "/" + denominator;
    }
}
