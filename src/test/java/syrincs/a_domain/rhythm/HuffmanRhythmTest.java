package syrincs.a_domain.rhythm;

import org.junit.jupiter.api.Test;
import syrincs.a_domain.statistics.StandardDeviation;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class HuffmanRhythmTest {
    @Test
    void testCalculateInformation_OnlyQuarters(){
        var rhythm = new HuffmanRhythm(4,4,90, "xooo xooo xooo xooo");
        assertEquals(1, rhythm.getInformation());

        rhythm = new HuffmanRhythm(4,4,90, "xooo oooo oooo oooo");
        assertEquals(2, rhythm.getInformation());

        rhythm = new HuffmanRhythm(4,4,90, "oooo oooo xooo oooo");
        assertEquals(2, rhythm.getInformation());

        rhythm = new HuffmanRhythm(4,4,90, "xooo xooo oooo oooo");
        assertEquals(2, rhythm.getInformation());

        rhythm = new HuffmanRhythm(4,4,90, "xooo oooo xooo xooo");
        assertEquals(3, rhythm.getInformation());

        rhythm = new HuffmanRhythm(4,4,90, "xooo xooo xooo oooo");
        assertEquals(2, rhythm.getInformation());
    }

    @Test void testCalculateInformation_WithSeparation() {
        var rhythm = new HuffmanRhythm(4, 4, 90, "xooo xoxo xooo xoxo");
        assertEquals(3, rhythm.getInformation());

        rhythm = new HuffmanRhythm(4, 4, 90, "xoxo xooo xoxo xooo");
        assertEquals(3, rhythm.getInformation());

        rhythm = new HuffmanRhythm(4, 4, 90, "xoxo xoxo xoxo xoxo");
        assertEquals(5, rhythm.getInformation());

        rhythm = new HuffmanRhythm(4, 4, 90, "xooo xoxo xoxx xxxo");
        assertEquals(7, rhythm.getInformation());

        rhythm = new HuffmanRhythm(4, 4, 90, "xooo ooxo xoxx xxxo");
        assertEquals(9, rhythm.getInformation());

        rhythm = new HuffmanRhythm(4, 4, 90, "xxox xoxo ooxo xooo");
        assertEquals(9, rhythm.getInformation());

        rhythm = new HuffmanRhythm(4, 4, 90, "xxxx xxxx xxxx xxxx");
        assertEquals(9, rhythm.getInformation());

        rhythm = new HuffmanRhythm(4, 4, 90, "xxox xxox xxox xxox");
        assertEquals(17, rhythm.getInformation());

    }

    @Test
    void testCalculateInformation_IncludesAllBars() {
        var rhythm = new HuffmanRhythm(4, 4, 90, "oooo oooo oooo oooo xooo xooo xooo xooo");

        assertEquals(1, rhythm.getInformation());
    }

    @Test
    void testStandardDeviation_IsCalculatedFromBeatInformation() {
        var rhythm = new HuffmanRhythm(4, 4, 90, "xooo xoxo xooo xoxo");

        assertEquals(0.4330127018922193, rhythm.getStandardDeviation(), 0.0000000000000001);
    }

    @Test
    void beatInformation_exposesFirstProfileAndItsAggregates() {
        var rhythm = new HuffmanRhythm(4, 4, 90, "xooo xoxo xooo xoxo");

        assertEquals(List.of(1, 1, 0, 1), rhythm.getBeatInformation());
        assertEquals(3, rhythm.getInformation());
        assertEquals(0.75, rhythm.getMeanBeatInformation());
        assertEquals(0.4330127018922193, rhythm.getStandardDeviation(), 1e-15);
        assertAggregatesMatchBeatInformation(rhythm);
    }

    @Test
    void beatInformation_distinguishesDistributionsWithTheSameTotal() {
        var rhythm = new HuffmanRhythm(4, 4, 90, "xoxo xooo xoxo xooo");

        assertEquals(List.of(2, 0, 1, 0), rhythm.getBeatInformation());
        assertEquals(3, rhythm.getInformation());
        assertEquals(0.75, rhythm.getMeanBeatInformation());
        assertEquals(0.82915619758885, rhythm.getStandardDeviation(), 1e-15);
        assertAggregatesMatchBeatInformation(rhythm);
    }

    @Test
    void beatInformation_isUnmodifiable() {
        var rhythm = new HuffmanRhythm(4, 4, 90, "xooo xoxo xooo xoxo");
        var beats = rhythm.getBeatInformation();

        assertThrows(UnsupportedOperationException.class, () -> beats.add(9));
        assertThrows(UnsupportedOperationException.class, () -> beats.set(0, 9));
        assertThrows(UnsupportedOperationException.class, () -> beats.remove(0));
        assertThrows(UnsupportedOperationException.class, beats::clear);
        assertEquals(List.of(1, 1, 0, 1), rhythm.getBeatInformation());
        assertAggregatesMatchBeatInformation(rhythm);
    }

    @Test
    void beatInformation_isFlatAndCarriesPlayingStateAcrossBars() {
        var rhythm = new HuffmanRhythm(4, 4, 90, "xoxo ".repeat(8));

        assertEquals(List.of(2, 1, 1, 1, 1, 1, 1, 1), rhythm.getBeatInformation());
        assertEquals(9, rhythm.getInformation());
        assertEquals(1.125, rhythm.getMeanBeatInformation());
        assertEquals(0.33071891388307384, rhythm.getStandardDeviation(), 1e-15);
        assertAggregatesMatchBeatInformation(rhythm);
    }

    @Test
    void beatInformation_retainsSilentBeatsAndStartsFromRest() {
        var silence = new HuffmanRhythm(4, 4, 90, "oooo ".repeat(4));
        assertEquals(List.of(0, 0, 0, 0), silence.getBeatInformation());
        assertEquals(0, silence.getInformation());
        assertEquals(0.0, silence.getMeanBeatInformation());
        assertEquals(0.0, silence.getStandardDeviation());
        assertAggregatesMatchBeatInformation(silence);

        var laterOnset = new HuffmanRhythm(4, 4, 90, "oooo ".repeat(4) + "xooo ".repeat(4));
        assertEquals(List.of(0, 0, 0, 0, 1, 0, 0, 0), laterOnset.getBeatInformation());
        assertEquals(1, laterOnset.getInformation());
        assertEquals(0.125, laterOnset.getMeanBeatInformation());
        assertAggregatesMatchBeatInformation(laterOnset);
    }

    private static void assertAggregatesMatchBeatInformation(HuffmanRhythm rhythm) {
        var values = rhythm.getBeatInformation();
        assertEquals(rhythm.getOnsetListPerBeat().size(), values.size());
        assertEquals(values.stream().mapToInt(Integer::intValue).sum(), rhythm.getInformation());
        assertEquals(StandardDeviation.mean(values), rhythm.getMeanBeatInformation());
        assertEquals(StandardDeviation.calc(values), rhythm.getStandardDeviation());
    }

    @Test
    void constructorDoesNotWriteToStdout() {
        PrintStream originalOut = System.out;
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        try {
            System.setOut(new PrintStream(output));
            new HuffmanRhythm(4, 4, 90, "xooo xoxo xooo xoxo");
        } finally {
            System.setOut(originalOut);
        }

        assertTrue(output.toString(StandardCharsets.UTF_8).isBlank());
    }

}
