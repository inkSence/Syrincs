package syrincs.a_domain.rhythm;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;


public class RhythmTest {

    @Test
    public void testIsValidOnSetList(){
        assertDoesNotThrow( () -> new Rhythm(4,4,90, "Xooo Xooo Xooo Xooo"));
        assertDoesNotThrow( () -> new Rhythm(4,4,90, "Oooo oooo oooo oooo"));

        assertThrows(IllegalArgumentException.class, () -> new Rhythm(4,4,90, "xooo xooo xooo xoo"));
        assertThrows(IllegalArgumentException.class, () -> new Rhythm(4,4,90, "xooo xooo xooo xoooOo"));
        assertThrows(IllegalArgumentException.class, () -> new Rhythm(4,4,90, "Mooo xooo xooo xooo"));
    }

    @Test
    public void testIsValidOnSetListPerBeat(){
        var rhythm = new Rhythm(4,4,90, "xooo oxoo ooxo ooox");
        List<String> onsetList = rhythm.getOnsetListPerBeat();
        assertEquals("xooo", onsetList.getFirst());
        assertEquals("oxoo", onsetList.get(1));
        assertEquals("ooxo", onsetList.get(2));
        assertEquals("ooox", onsetList.get(3));

        assertNotEquals("oooo", onsetList.getFirst());
        assertNotEquals("oxoo", onsetList.getFirst());
        assertNotEquals("xxxx", onsetList.get(1));
        assertNotEquals("xooo", onsetList.get(1));
        assertNotEquals("xooo", onsetList.get(3));
        assertNotEquals("oxoo", onsetList.get(3));
    }

    @Test
    public void testOnSetListPerBeatIncludesAllBars(){
        var rhythm = new Rhythm(4,4,90, "oooo oooo oooo oooo xooo oxoo ooxo ooox");
        List<String> onsetList = rhythm.getOnsetListPerBeat();

        assertEquals(8, onsetList.size());
        assertEquals("oooo", onsetList.getFirst());
        assertEquals("oooo", onsetList.get(3));
        assertEquals("xooo", onsetList.get(4));
        assertEquals("ooox", onsetList.get(7));
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


}
