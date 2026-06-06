package syrincs.a_domain.rhythm;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

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

}
