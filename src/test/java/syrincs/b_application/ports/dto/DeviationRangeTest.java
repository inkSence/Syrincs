package syrincs.b_application.ports.dto;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

class DeviationRangeTest {
    @Test
    void absentBoundsAndZeroAreAllowed() {
        assertEquals(new DeviationRange(null, null), new DeviationRange(null, null));
        assertNull(new DeviationRange(null, 0.0).min());
        assertNull(new DeviationRange(0.0, null).max());
        assertEquals(0.0, new DeviationRange(0.0, 0.0).min());
        assertEquals(0.8, new DeviationRange(0.2, 0.8).max());
    }

    @ParameterizedTest
    @MethodSource("invalidRanges")
    void invalidBoundsHaveReadableErrors(Double min, Double max, String expectedMessage) {
        var error = assertThrows(IllegalArgumentException.class, () -> new DeviationRange(min, max));
        assertTrue(error.getMessage().contains(expectedMessage));
    }

    static Stream<Arguments> invalidRanges() {
        return Stream.of(
                Arguments.of(-0.1, null, "--deviation-min"),
                Arguments.of(null, -0.1, "--deviation-max"),
                Arguments.of(Double.NaN, null, "finite and non-negative"),
                Arguments.of(null, Double.NaN, "finite and non-negative"),
                Arguments.of(Double.POSITIVE_INFINITY, null, "--deviation-min"),
                Arguments.of(null, Double.POSITIVE_INFINITY, "--deviation-max"),
                Arguments.of(Double.NEGATIVE_INFINITY, null, "--deviation-min"),
                Arguments.of(null, Double.NEGATIVE_INFINITY, "--deviation-max"),
                Arguments.of(0.8, 0.2, "must not exceed"));
    }
}
