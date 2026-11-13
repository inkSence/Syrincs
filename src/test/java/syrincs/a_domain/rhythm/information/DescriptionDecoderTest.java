package syrincs.a_domain.rhythm.information;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

import java.util.List;

class DescriptionDecoderTest {
    private final DescriptionDecoder decoder = new DescriptionDecoder();

    @Test
    void independentExamplesAndCosts() {
        assertEquals("0".repeat(16), decoder.decode("000", 16).bits());
        assertEquals("1".repeat(16), decoder.decode("001", 16).bits());
        assertEquals("1000".repeat(4), decoder.decode("0110110101000", 16).bits());
        assertEquals("1010101", decoder.decode("1010110000", 7).bits());
        var witness = decoder.decode("010" + "1000".repeat(4), 16).messages().getFirst();
        assertEquals(19, witness.totalBits());
        assertEquals(3, witness.modelBits());
        assertEquals(16, witness.residualBits());
    }

    @Test
    void refusesInvalidAndPartialMessages() {
        for (String code : List.of("", "2", "00", "0000", "110", "111", "011", "100", "101"))
            assertThrows(IllegalArgumentException.class, () -> decoder.decode(code, 1), code);
        assertThrows(
                IllegalArgumentException.class,
                () -> decoder.decode("01111000", 6)); // unused period field
        assertThrows(
                IllegalArgumentException.class,
                () -> decoder.decode("101010101101", 4)); // duplicate/descending flips
    }

    @Test
    void causalDecoderRequiresActualContextAndConsumesAllBits() {
        var partition = Segmentation.equal(16, 4, 16);
        assertEquals(
                "1000".repeat(4),
                decoder.decode("0010100000101000101101", 16, Scope.CAUSAL_WINDOWS, partition, "")
                        .bits());
        assertThrows(
                IllegalArgumentException.class,
                () ->
                        decoder.decode(
                                "10", 4, Scope.CAUSAL_WINDOWS, Segmentation.equal(4, 4, null), ""));
    }
}
