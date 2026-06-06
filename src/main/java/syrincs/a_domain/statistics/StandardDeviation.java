package syrincs.a_domain.statistics;

import java.util.List;

/**
 * Utility for basic descriptive statistics on integer samples.
 */
public final class StandardDeviation {

    private StandardDeviation() { /* utility class */ }

    /**
     * Computes the arithmetic mean (average) of the given integer values.
     *
     * @param values list of integers (must be non-null and non-empty)
     * @return the mean as double
     * @throws IllegalArgumentException if the list is null or empty
     */
    public static double mean(List<Integer> values) {
        if (values == null || values.isEmpty()) {
            throw new IllegalArgumentException("values must not be null or empty");
        }
        long sum = 0L;
        for (Integer v : values) {
            if (v == null) {
                throw new IllegalArgumentException("values must not contain null elements");
            }
            sum += v;
        }
        return (double) sum / (double) values.size();
    }

    /**
     * Computes the population standard deviation (divide by N) of the given integer values.
     * This method uses {@link #mean(List)} internally.
     *
     * @param values list of integers (must be non-null and non-empty)
     * @return standard deviation as double (non-negative). For a single element list, returns 0.0.
     * @throws IllegalArgumentException if the list is null or empty
     */
    public static double calc(List<Integer> values) {
        if (values == null || values.isEmpty()) {
            throw new IllegalArgumentException("values must not be null or empty");
        }
        double avg = mean(values);
        double sumSquaredDiffs = 0.0;
        for (Integer v : values) {
            if (v == null) {
                throw new IllegalArgumentException("values must not contain null elements");
            }
            double diff = v - avg;
            sumSquaredDiffs += diff * diff;
        }
        double variance = sumSquaredDiffs / values.size(); // population variance (N)
        return Math.sqrt(variance);
    }
}
