package syrincs.a_domain.rhythm.information;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

import java.math.BigInteger;
import java.util.List;

class ExactDescriptionAnalyzerTest {
    private final ExactDescriptionAnalyzer analyzer = new ExactDescriptionAnalyzer();
    private final DescriptionDecoder decoder = new DescriptionDecoder();

    @Test
    void numericalContractsAndCanonicalTies() {
        assertGlobal("o".repeat(16), 3, "000");
        assertGlobal("x".repeat(16), 3, "001");
        assertGlobal("xooo".repeat(4), 13, "0110110101000");
        assertGlobal("xoxo".repeat(4), 11, "01100101010");
        assertGlobal("xooo".repeat(8), 14, "01100110101000");
        assertGlobal("xooo".repeat(32), 16, "0110000110101000");
        var ties = analyzer.analyze(new OnsetSequence("xoxoxox"));
        assertEquals(10, ties.totalBits());
        assertEquals(BigInteger.valueOf(3), ties.count());
        assertEquals(List.of("0101010101", "0110101010", "1010110000"), ties.alternatives(10));
        assertEquals(List.of("0101010101"), ties.alternatives(1));
        assertEquals(BigInteger.valueOf(3), ties.count());
    }

    @Test
    void largerPrefixCanHaveSmallerGlobalDescription() {
        assertEquals(18, analyzer.analyze(new OnsetSequence("xoooxoxoxoooxox")).totalBits());
        assertEquals(17, analyzer.analyze(new OnsetSequence("xoooxoxoxoooxoxo")).totalBits());
    }

    @Test
    void roundTripsAllShortWordsAndAllTheirOptimalWitnesses() {
        for (int n = 1; n <= 8; n++)
            for (int mask = 0; mask < (1 << n); mask++) {
                String bits =
                        "0".repeat(n - Integer.toBinaryString(mask).length())
                                + Integer.toBinaryString(mask);
                var result = analyzer.analyze(OnsetSequence.fromBits(bits));
                assertTrue(result.totalBits() >= 3 && result.totalBits() <= n + 3);
                for (String code : result.alternatives(100))
                    assertEquals(bits, decoder.decode(code, n).bits());
                assertEquals(
                        result.count().min(BigInteger.valueOf(100)).intValueExact(),
                        result.alternatives(100).size());
            }
    }

    private void assertGlobal(String onsets, int cost, String code) {
        var result = analyzer.analyze(new OnsetSequence(onsets));
        assertEquals(cost, result.totalBits());
        assertEquals(code, result.canonicalCode());
        assertEquals(
                new OnsetSequence(onsets).bits(), decoder.decode(code, onsets.length()).bits());
    }
}
