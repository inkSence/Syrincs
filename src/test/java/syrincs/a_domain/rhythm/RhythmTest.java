package syrincs.a_domain.rhythm;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;


public class RhythmTest {

    @Test
    public void testIsValidOnSetList(){
        var rhythm = new Rhythm(4,4,90, "Xooo Xooo Xooo Xooo");
        assertTrue(rhythm.hasValidLength(rhythm.onsetList));
        assertTrue(rhythm.hasValidCharacters(rhythm.onsetList));

        rhythm = new Rhythm(4,4,90, "Oooo oooo oooo oooo");
        assertTrue(rhythm.hasValidLength(rhythm.onsetList));
        assertTrue(rhythm.hasValidCharacters(rhythm.onsetList));

        rhythm = new Rhythm(4,4,90, "xooo xooo xooo xoo");
        assertFalse(rhythm.hasValidLength(rhythm.onsetList));
        assertTrue(rhythm.hasValidCharacters(rhythm.onsetList));

        rhythm = new Rhythm(4,4,90, "xooo xooo xooo xoooOo");
        assertFalse(rhythm.hasValidLength(rhythm.onsetList));
        assertTrue(rhythm.hasValidCharacters(rhythm.onsetList));

        rhythm = new Rhythm(4,4,90, "Mooo xooo xooo xooo");
        assertTrue(rhythm.hasValidLength(rhythm.onsetList));
        assertFalse(rhythm.hasValidCharacters(rhythm.onsetList));
    }

    @Test
    public void testIsOnBeat(){
        var rhythm = new Rhythm(4,4,90, "xooo xooo xooo xooo");
        assertTrue(rhythm.isOnBeat(0));
        assertTrue(rhythm.isOnBeat(4));
        assertTrue(rhythm.isOnBeat(12));
        assertFalse(rhythm.isOnBeat(1));
        assertFalse(rhythm.isOnBeat(2));
        assertFalse(rhythm.isOnBeat(7));
        assertFalse(rhythm.isOnBeat(9));
    }

    @Test void testCalculateInformation_OnlyQuarters(){
        var rhythm = new HuffmanRhythm(4,4,90, "xooo xooo xooo xooo");
        int info = rhythm.calculateInformation(rhythm.onsetList);
        assertEquals(1, info);

        rhythm = new HuffmanRhythm(4,4,90, "xooo oooo oooo oooo");
        info = rhythm.calculateInformation(rhythm.onsetList);
        assertEquals(2, info);

        rhythm = new HuffmanRhythm(4,4,90, "oooo oooo xooo oooo");
        info = rhythm.calculateInformation(rhythm.onsetList);
        assertEquals(2, info);

        rhythm = new HuffmanRhythm(4,4,90, "xooo xooo oooo oooo");
        info = rhythm.calculateInformation(rhythm.onsetList);
        assertEquals(2, info);

        rhythm = new HuffmanRhythm(4,4,90, "xooo oooo xooo xooo");
        info = rhythm.calculateInformation(rhythm.onsetList);
        assertEquals(3, info);

        rhythm = new HuffmanRhythm(4,4,90, "xooo xooo xooo oooo");
        info = rhythm.calculateInformation(rhythm.onsetList);
        assertEquals(2, info);
    }

    @Test void testCalculateInformation_WithSeparation() {
        var rhythm = new HuffmanRhythm(4, 4, 90, "xooo xoxo xooo xoxo");
        int info = rhythm.calculateInformation(rhythm.onsetList);
        assertEquals(3, info);

        rhythm = new HuffmanRhythm(4, 4, 90, "xoxo xooo xoxo xooo");
        info = rhythm.calculateInformation(rhythm.onsetList);
        assertEquals(3, info);

        rhythm = new HuffmanRhythm(4, 4, 90, "xoxo xoxo xoxo xoxo");
        info = rhythm.calculateInformation(rhythm.onsetList);
        assertEquals(5, info);

        rhythm = new HuffmanRhythm(4, 4, 90, "xooo xoxo xoxx xxxo");
        info = rhythm.calculateInformation(rhythm.onsetList);
        assertEquals(7, info);

        rhythm = new HuffmanRhythm(4, 4, 90, "xooo ooxo xoxx xxxo");
        info = rhythm.calculateInformation(rhythm.onsetList);
        assertEquals(9, info);

        rhythm = new HuffmanRhythm(4, 4, 90, "xxox xoxo ooxo xooo");
        info = rhythm.calculateInformation(rhythm.onsetList);
        assertEquals(9, info);

        rhythm = new HuffmanRhythm(4, 4, 90, "xxxx xxxx xxxx xxxx");
        info = rhythm.calculateInformation(rhythm.onsetList);
        assertEquals(9, info);

        rhythm = new HuffmanRhythm(4, 4, 90, "xxox xxox xxox xxox");
        info = rhythm.calculateInformation(rhythm.onsetList);
        assertEquals(17, info);

    }
}
