package syrincs.a_domain.rhythm.metricstate;

import java.math.BigInteger;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * Result for one finite example as a whole.
 *
 * @param valueBits minimum cost over all paths of the transition table
 * @param instructionCount number of instructions of every minimal path
 * @param minimalPathCount exact number of minimal paths (distinct state sequences)
 * @param minimalPaths minimal paths up to the requested display limit, in deterministic order
 * @param minimalSequences distinct instruction sequences of the minimal paths, sorted by code
 * @param minimalSequencesComplete {@code false} only if more than {@link
 *     MetricStateAnalyzer#MAX_DISTINCT_SEQUENCES} distinct sequences exist
 * @param barCosts cost share per bar as a range over all minimal paths
 * @param beatCosts cost share per beat as a range over all minimal paths
 * @param valueAfterBars value of the prefix example after each full bar
 */
public record MetricStateAnalysis(
        MetricStateMeasurementKey key,
        MetricExample example,
        int valueBits,
        int instructionCount,
        BigInteger minimalPathCount,
        List<MetricInterpretation> minimalPaths,
        Set<List<Instruction>> minimalSequences,
        boolean minimalSequencesComplete,
        List<CostRange> barCosts,
        List<CostRange> beatCosts,
        List<Integer> valueAfterBars) {
    public MetricStateAnalysis {
        minimalPaths = List.copyOf(minimalPaths);
        Set<List<Instruction>> copy = new LinkedHashSet<>();
        for (List<Instruction> sequence : minimalSequences) copy.add(List.copyOf(sequence));
        minimalSequences = Collections.unmodifiableSet(copy);
        barCosts = List.copyOf(barCosts);
        beatCosts = List.copyOf(beatCosts);
        valueAfterBars = List.copyOf(valueAfterBars);
    }

    /** {@code true} if the display limit hid some minimal paths. */
    public boolean minimalPathsTruncated() {
        return minimalPathCount.compareTo(BigInteger.valueOf(minimalPaths.size())) > 0;
    }

    /** More than one minimal path: the open interpretation of guiding principle 3. */
    public boolean isAmbiguous() {
        return minimalPathCount.compareTo(BigInteger.ONE) > 0;
    }

    /** Distinct minimal sequences as lists of 2-bit codes, e.g. {@code [10, 01]}. */
    public Set<List<String>> minimalSequenceCodes() {
        Set<List<String>> codes = new LinkedHashSet<>();
        for (List<Instruction> sequence : minimalSequences)
            codes.add(sequence.stream().map(Instruction::code).toList());
        return Collections.unmodifiableSet(codes);
    }
}
