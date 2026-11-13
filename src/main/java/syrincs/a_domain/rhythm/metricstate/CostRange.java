package syrincs.a_domain.rhythm.metricstate;

/**
 * Cost share of one bar or beat ({@code [startPosition, endPosition)}) as a range over all
 * minimal paths. {@code minBits == maxBits} if all minimal paths agree.
 */
public record CostRange(int index, int startPosition, int endPosition, int minBits, int maxBits) {
    public boolean isUnambiguous() {
        return minBits == maxBits;
    }
}
