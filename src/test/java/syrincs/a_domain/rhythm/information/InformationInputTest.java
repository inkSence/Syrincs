package syrincs.a_domain.rhythm.information;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

import java.util.List;

class InformationInputTest {
    @Test
    void exactTimeAndUnknownDefaults() {
        var frame = new AnalysisFrame(16, null, Rational.of(108), 4, 4, 4);
        assertEquals(Rational.of(1250, 9), frame.slotDurationMs());
        assertEquals(16, frame.notatedBarSlots());
        assertNull(AnalysisFrame.unknown(16).slotDurationMs());
        assertEquals(Rational.of(1, 4), Rational.parse("0.25"));
        assertEquals(Rational.of(1000), Rational.parse("1e3"));
        assertThrows(IllegalArgumentException.class, () -> Rational.parse("NaN"));
        assertThrows(IllegalArgumentException.class, () -> Rational.parse("1/0"));
    }

    @Test
    void normalizationAndLengthLimits() {
        assertEquals("xo", new OnsetSequence("X O\n").normalizedOnsets());
        assertEquals(128, new OnsetSequence("o".repeat(128)).slots());
        for (String s : List.of("", "x-", "o".repeat(129)))
            assertThrows(IllegalArgumentException.class, () -> new OnsetSequence(s));
    }

    @Test
    void partitionMustBeCompleteAndAligned() {
        var s = Segmentation.equal(32, 4, 16);
        s.validate(AnalysisFrame.unknown(32));
        assertThrows(UnsupportedOperationException.class, () -> s.boundaries().add(40));
        assertThrows(IllegalArgumentException.class, () -> Segmentation.equal(17, 4, null));
        assertThrows(
                IllegalArgumentException.class,
                () -> new Segmentation(List.of(0, 4, 8), List.of(0, 3, 8)));
        assertThrows(IllegalArgumentException.class, () -> s.validate(AnalysisFrame.unknown(16)));
    }
}
