package syrincs.b_application.ports.dto;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class BeatProfileCriteriaTest {
    @Test
    void peaksAreOneBasedAndIncludeTiesAndSilence() {
        for (int beat = 1; beat <= 4; beat++) {
            var criteria = new BeatProfileCriteria(null, beat);
            assertEquals(beat != 3, criteria.matches(List.of(1, 1, 0, 1)));
            assertEquals(beat == 1, criteria.matches(List.of(2, 0, 1, 0)));
            assertTrue(criteria.matches(List.of(0, 0, 0, 0)));
            assertFalse(criteria.matches(List.of(1, 1)));
            assertFalse(criteria.matches(List.of(1, 1, 0, 1, 1, 0, 0, 0)));
        }
    }

    @Test
    void exactProfileIsImmutableAndCombinesWithPeak() {
        var input = new ArrayList<>(List.of(1, 1, 0, 1));
        var criteria = new BeatProfileCriteria(input, 4);
        input.set(0, 9);
        assertEquals(List.of(1, 1, 0, 1), criteria.profile());
        assertThrows(UnsupportedOperationException.class, () -> criteria.profile().set(0, 9));
        assertTrue(criteria.active());
        assertTrue(criteria.matches(List.of(1, 1, 0, 1)));
        assertFalse(criteria.matches(List.of(2, 0, 1, 0)));
        assertDoesNotThrow(() -> criteria.validateInformation(3));
        assertThrows(IllegalArgumentException.class, () -> criteria.validateInformation(4));
        assertTrue(new BeatProfileCriteria(List.of(2, 0, 1, 0), null).matches(List.of(2, 0, 1, 0)));
    }

    @Test
    void absentCriteriaDoNotRestrictOtherMetersOrPhraseLengths() {
        var criteria = new BeatProfileCriteria(null, null);
        assertFalse(criteria.active());
        assertTrue(criteria.matches(List.of(1, 0)));
        assertDoesNotThrow(() -> criteria.validateInformation(3));
    }

    @Test
    void rejectsInvalidProfilesPeaksAndContradictions() {
        for (var profile : List.of(List.<Integer>of(), List.of(1, 1, 1), List.of(1, 1, 1, 1, 1),
                List.of(1, -1, 0, 1), Arrays.asList(1, null, 0, 1))) {
            assertThrows(IllegalArgumentException.class, () -> new BeatProfileCriteria(profile, null));
        }
        for (int beat : List.of(-1, 0, 5)) {
            assertThrows(IllegalArgumentException.class, () -> new BeatProfileCriteria(null, beat));
        }
        assertThrows(IllegalArgumentException.class, () -> new BeatProfileCriteria(List.of(1, 1, 0, 1), 3));
        assertThrows(IllegalArgumentException.class, () -> new BeatProfileCriteria(List.of(2, 0, 1, 0), 2));
    }

    @Test
    void profileSumCannotOverflowIntoAValidInformationGrade() {
        var criteria = new BeatProfileCriteria(List.of(Integer.MAX_VALUE, Integer.MAX_VALUE, 4, 1), null);
        assertThrows(IllegalArgumentException.class, () -> criteria.validateInformation(3));
    }
}
