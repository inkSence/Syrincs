package syrincs.a_domain.hindemith;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ChordAnalysisTest {

    @Test
    @DisplayName("analyze: determines basic properties of chord [60, 64, 67, 71]")
    void analyze_determines_basic_properties_major7() {
        // C–E–G–B (Cmaj7 without the 7th spelled enharmonically as H in German notation)
        var analysis = new ChordAnalysis();
        var chord = List.of(60, 64, 67, 71);

        var res = analysis.analyze(chord);

        assertNotNull(res, "Result should not be null");
        // Column: tritone-free (A)
        assertEquals(ChordAnalysis.Column.A_TRITONE_FREE, res.column, "Expected tritone-free column (A)");
        // Root: by current project logic this chord resolves to root C (60)
        assertEquals(60, res.rootNote, "Expected root note C (60)");
        // Frame: highest - lowest = 71 - 60 = 11
        assertEquals(11, res.frameInterval, "Expected frame interval of 11 semitones");
        // Notes are returned sorted
        assertEquals(List.of(60, 64, 67, 71), res.notes, "Expected sorted note list in result");

        // Intentionally no assertion on group yet: the group mapping for this chord is under discussion
        // and may depend on the ordering/overlap of specifications.
    }

    @Test
    void chordSpecificationRepository_returnsImmutableSpecifications() {
        var specs = new ChordSpecificationRepository().getGroupSpecifications();

        assertEquals(18, specs.size());
        assertThrows(UnsupportedOperationException.class, () -> specs.clear());
    }
}
