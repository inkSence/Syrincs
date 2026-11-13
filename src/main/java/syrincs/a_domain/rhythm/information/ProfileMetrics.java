package syrincs.a_domain.rhythm.information;

import java.math.BigInteger;
import java.util.List;
import java.util.stream.IntStream;

public record ProfileMetrics(
        String basis,
        List<Integer> values,
        Rational mean,
        Rational variance,
        double deviation,
        List<Integer> peakPositions) {
    public ProfileMetrics {
        values = List.copyOf(values);
        peakPositions = List.copyOf(peakPositions);
    }

    public static ProfileMetrics of(String basis, List<Integer> values) {
        if (values.isEmpty())
            throw new IllegalArgumentException("Metrics require a nonempty profile");
        BigInteger sum = BigInteger.ZERO, squares = BigInteger.ZERO;
        for (int value : values) {
            BigInteger b = BigInteger.valueOf(value);
            sum = sum.add(b);
            squares = squares.add(b.multiply(b));
        }
        BigInteger k = BigInteger.valueOf(values.size());
        Rational mean = new Rational(sum, k);
        Rational variance =
                new Rational(k.multiply(squares).subtract(sum.multiply(sum)), k.multiply(k));
        int peak = values.stream().mapToInt(Integer::intValue).max().orElseThrow();
        return new ProfileMetrics(
                basis,
                values,
                mean,
                variance,
                Math.sqrt(variance.doubleValue()),
                IntStream.range(0, values.size())
                        .filter(i -> values.get(i) == peak)
                        .map(i -> i + 1)
                        .boxed()
                        .toList());
    }
}
