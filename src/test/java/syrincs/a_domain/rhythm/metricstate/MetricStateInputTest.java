package syrincs.a_domain.rhythm.metricstate;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

class MetricStateInputTest {
    private static final MetricFrame IMPULSE = new MetricFrame(4, 16, SoundCondition.IMPULSE);
    private static final MetricFrame SUSTAINED = new MetricFrame(4, 16, SoundCondition.SUSTAINED);

    private static void rejected(MetricFrame frame, String events, String messagePart) {
        IllegalArgumentException e =
                assertThrows(
                        IllegalArgumentException.class, () -> MetricExample.parse(frame, events));
        assertTrue(e.getMessage().contains(messagePart), e.getMessage());
    }

    @Test
    void frameRequiresPowerOfTwoBeatAndBarAsMultipleOfBeat() {
        assertThrows(IllegalArgumentException.class, () -> new MetricFrame(0, 4, SoundCondition.IMPULSE));
        assertThrows(IllegalArgumentException.class, () -> new MetricFrame(3, 12, SoundCondition.IMPULSE));
        assertThrows(IllegalArgumentException.class, () -> new MetricFrame(-4, 16, SoundCondition.IMPULSE));
        assertThrows(IllegalArgumentException.class, () -> new MetricFrame(4, 0, SoundCondition.IMPULSE));
        assertThrows(IllegalArgumentException.class, () -> new MetricFrame(4, 6, SoundCondition.IMPULSE));
        assertThrows(NullPointerException.class, () -> new MetricFrame(4, 16, null));
        assertDoesNotThrow(() -> new MetricFrame(1, 1, SoundCondition.IMPULSE));
        assertDoesNotThrow(() -> new MetricFrame(4, 12, SoundCondition.SUSTAINED));
    }

    @Test
    void normalizesWhitespaceAndCase() {
        MetricExample example = MetricExample.parse(SUSTAINED, " X=== ==\t== oOoo\noooo ");
        assertEquals("x=======oooooooo", example.normalizedEvents());
        assertEquals(16, example.slots());
        assertEquals(1, example.bars());
        assertEquals(4, example.beats());
    }

    @Test
    void rejectsEmptyInvalidCharactersAndUnfittingLengths() {
        rejected(IMPULSE, "", "empty");
        rejected(IMPULSE, "   ", "empty");
        rejected(IMPULSE, "xooo xo-o xooo xooo", "'-' at position 6");
        rejected(IMPULSE, "xooo xooo xooo xoo", "multiple of barSlots");
        rejected(IMPULSE, "xooo xooo xooo xooo xooo", "multiple of barSlots");
        rejected(new MetricFrame(4, 4, SoundCondition.IMPULSE), "xooo".repeat(33), "At most 128");
        assertEquals(
                128, MetricExample.parse(new MetricFrame(4, 4, SoundCondition.IMPULSE), "xooo".repeat(32)).slots());
        assertThrows(NullPointerException.class, () -> MetricExample.parse(IMPULSE, null));
        assertThrows(NullPointerException.class, () -> MetricExample.parse(null, "xooo"));
    }

    @Test
    void holdIsAnInputErrorUnderImpulse() {
        rejected(IMPULSE, "x=== oooo oooo oooo", "not allowed under IMPULSE");
    }

    @Test
    void holdMustDirectlyFollowOnsetOrHold() {
        rejected(SUSTAINED, "==== oooo oooo oooo", "position 0 must directly follow");
        rejected(SUSTAINED, "xo=o oooo oooo oooo", "position 2 must directly follow");
        assertDoesNotThrow(() -> MetricExample.parse(SUSTAINED, "x=x= xx== oooo ooox"));
        // A tone may be held across the bar line.
        assertDoesNotThrow(
                () -> MetricExample.parse(new MetricFrame(4, 4, SoundCondition.SUSTAINED), "ooox ===="));
    }

    @Test
    void derivesRestOnsetsOnlyUnderSustainedAndAcrossBarLines() {
        MetricExample impulse = MetricExample.parse(new MetricFrame(2, 4, SoundCondition.IMPULSE), "xoox oooo");
        assertEquals(EventKind.ONSET, impulse.eventAt(0));
        assertEquals(EventKind.SILENCE, impulse.eventAt(1));
        assertEquals(EventKind.SILENCE, impulse.eventAt(4));

        MetricExample sustained =
                MetricExample.parse(new MetricFrame(2, 4, SoundCondition.SUSTAINED), "ox=o x==x oooo");
        assertEquals(EventKind.SILENCE, sustained.eventAt(0));
        assertEquals(EventKind.ONSET, sustained.eventAt(1));
        assertEquals(EventKind.HOLD, sustained.eventAt(2));
        assertEquals(EventKind.REST_ONSET, sustained.eventAt(3));
        assertEquals(EventKind.ONSET, sustained.eventAt(7));
        // x without a following = sounds exactly one position; its end falls on the bar line.
        assertEquals(EventKind.REST_ONSET, sustained.eventAt(8));
        assertEquals(EventKind.SILENCE, sustained.eventAt(9));
    }

    @Test
    void analyzerRejectsNegativePathLimit() {
        assertThrows(
                IllegalArgumentException.class,
                () -> new MetricStateAnalyzer().analyze(MetricExample.parse(IMPULSE, "xooo xooo xooo xooo"), -1));
    }

    @Test
    void instructionCodesAreTheFourTwoBitWords() {
        assertEquals("00", Instruction.REFINE.code());
        assertEquals("01", Instruction.COARSEN.code());
        assertEquals("10", Instruction.PLAY_NOTES.code());
        assertEquals("11", Instruction.PLAY_RESTS.code());
        assertEquals(Instruction.COARSEN, Instruction.fromCode("01"));
        assertThrows(IllegalArgumentException.class, () -> Instruction.fromCode("2"));
    }
}
