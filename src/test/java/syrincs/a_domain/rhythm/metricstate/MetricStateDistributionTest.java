package syrincs.a_domain.rhythm.metricstate;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

import java.util.Map;
import java.util.TreeMap;

/**
 * Key figures of the value distribution over all 65,536 one-bar patterns (4/4, 16 positions,
 * IMPULSE). The constants were produced by {@link MetricStateDistributionReport}; see
 * doc/rhythm-generator/specs-metrischer-spielzustand/05-verteilung.md.
 */
class MetricStateDistributionTest {
    /** Pairs of value (bits) and number of patterns. */
    private static final int[][] EXPECTED_COUNTS = {
        {2, 2}, {4, 12}, {6, 61}, {8, 353}, {10, 1459}, {12, 4493}, {14, 9055}, {16, 13932},
        {18, 14840}, {20, 11590}, {22, 6668}, {24, 2321}, {26, 677}, {28, 67}, {30, 6}
    };
    private static final int AMBIGUOUS_BY_PATHS = 49341;
    private static final int AMBIGUOUS_BY_SEQUENCES = 49341;

    @Test
    void distributionOverAllOneBarPatterns() {
        MetricFrame frame = new MetricFrame(4, 16, SoundCondition.IMPULSE);
        MetricStateAnalyzer analyzer = new MetricStateAnalyzer();
        Map<Integer, Integer> counts = new TreeMap<>();
        int ambiguousByPaths = 0;
        int ambiguousBySequences = 0;
        for (int mask = 0; mask < 65536; mask++) {
            StringBuilder events = new StringBuilder();
            for (int i = 0; i < 16; i++) events.append(((mask >> (15 - i)) & 1) == 1 ? 'x' : 'o');
            String pattern = events.toString();
            MetricStateAnalysis analysis;
            try {
                analysis = analyzer.analyze(frame, pattern, 1);
            } catch (RuntimeException e) {
                throw new AssertionError("Exception for pattern " + pattern, e);
            }
            counts.merge(analysis.valueBits(), 1, Integer::sum);
            if (analysis.isAmbiguous()) ambiguousByPaths++;
            if (analysis.minimalSequenceCodes().size() > 1) ambiguousBySequences++;
        }

        Map<Integer, Integer> expected = new TreeMap<>();
        for (int[] pair : EXPECTED_COUNTS) expected.put(pair[0], pair[1]);
        assertEquals(expected, counts);
        assertEquals(65536, counts.values().stream().mapToInt(Integer::intValue).sum());
        assertEquals(AMBIGUOUS_BY_PATHS, ambiguousByPaths);
        assertEquals(AMBIGUOUS_BY_SEQUENCES, ambiguousBySequences);
    }
}
