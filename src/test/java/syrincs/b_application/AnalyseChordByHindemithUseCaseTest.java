package syrincs.b_application;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import syrincs.a_domain.hindemith.ChordAnalysis;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class AnalyseChordByHindemithUseCaseTest {

    @Test
    @DisplayName("analyze: [0,5,10] yields group 15 (A V layering M3/P4)")
    void analyze_layers_group15() {
        var res = analyseChordByHindemithUseCase.analyze(List.of(0, 5, 10));
        assertEquals(ChordAnalysis.Column.A_TRITONE_FREE, res.column);
        assertEquals(17, res.group);
    }

    AnalyseChordByHindemithUseCase analyseChordByHindemithUseCase = new AnalyseChordByHindemithUseCase();

    @Test
    @DisplayName("analyzeChordByHindemith: C-E-G yields column A, root C")
    void analyzeMajorTriad() {
        var res = analyseChordByHindemithUseCase.analyze(List.of(60, 64, 67));
        assertEquals(ChordAnalysis.Column.A_TRITONE_FREE, res.column);
        assertEquals(60, res.rootNote);
        assertEquals(1, res.group);
        assertEquals(List.of(60, 64, 67), res.notes);
    }

    @Test
    @DisplayName("analyzeChordByHindemith: tritone chord [60,66,69] yields column B, root 66")
    void analyzeTritoneChord() {
        var res = analyseChordByHindemithUseCase.analyze(List.of(60, 66, 69));
        assertEquals(ChordAnalysis.Column.B_WITH_TRITONE, res.column);
        assertEquals(66, res.rootNote);
        assertEquals(18, res.group);
        assertEquals(List.of(60, 66, 69), res.notes);
    }



    @Test
    @DisplayName("analyze: C-E-G is group A, root=C, degree=0, frame interval = 7")
    void analyze_majorTriad_basic() {
        var res = analyseChordByHindemithUseCase.analyze(List.of(60, 64, 67));
        assertEquals(ChordAnalysis.Column.A_TRITONE_FREE, res.column);
        assertEquals(60, res.rootNote);
        assertEquals(1, res.group);
        assertEquals(7, res.frameInterval);
        assertEquals(List.of(60, 64, 67), res.notes);
    }

    @Test
    @DisplayName("analyze: chord with tritone goes to group B; verifies root and degree")
    void analyze_tritoneChord_groupB_root_and_degree() {
        // Contains a tritone 60-66; best interval is minor third 66-69, hence root 66 per project logic
        var res = analyseChordByHindemithUseCase.analyze(List.of(60, 66, 69));
        assertEquals(ChordAnalysis.Column.B_WITH_TRITONE, res.column);
        assertEquals(66, res.rootNote);
        assertEquals(18, res.group);
        assertEquals(9, res.frameInterval);
        assertEquals(List.of(60, 66, 69), res.notes);
    }



    @Test
    @DisplayName("validate Group // A) I. 1")
    void validateGroup_A_I_1(){
        var res =  analyseChordByHindemithUseCase.analyze(List.of(60, 64, 67));
        assertEquals(ChordAnalysis.Column.A_TRITONE_FREE, res.column);
        assertEquals(60, res.rootNote);
        assertEquals(1, res.group);

        var res2 =  analyseChordByHindemithUseCase.analyze(List.of(60, 63, 67));
        assertEquals(ChordAnalysis.Column.A_TRITONE_FREE, res2.column);
        assertEquals(60, res2.rootNote);
        assertEquals(1, res2.group);
    }


    @Test
    @DisplayName("validate Group // A) I. 2")
    void validateGroup_A_I_2(){
        var res =  analyseChordByHindemithUseCase.analyze(List.of(60, 63, 68));
        assertEquals(ChordAnalysis.Column.A_TRITONE_FREE, res.column);
        assertEquals(68, res.rootNote);
        assertEquals(2, res.group);

        res =  analyseChordByHindemithUseCase.analyze(List.of(60, 64, 69));
        assertEquals(ChordAnalysis.Column.A_TRITONE_FREE, res.column);
        assertEquals(69, res.rootNote);
        assertEquals(2, res.group);

        res =  analyseChordByHindemithUseCase.analyze(List.of(60, 65, 69));
        assertEquals(ChordAnalysis.Column.A_TRITONE_FREE, res.column);
        assertEquals(65, res.rootNote);
        assertEquals(2, res.group);

        res =  analyseChordByHindemithUseCase.analyze(List.of(60, 65, 68));
        assertEquals(ChordAnalysis.Column.A_TRITONE_FREE, res.column);
        assertEquals(65, res.rootNote);
        assertEquals(2, res.group);
    }

    @Test
    @DisplayName("validate Group // B) II. a")
    void validateGroup_B_II_a(){
        var res = analyseChordByHindemithUseCase.analyze(List.of(60, 64, 70));
        assertEquals(ChordAnalysis.Column.B_WITH_TRITONE, res.column);
        assertEquals(60, res.rootNote);
        assertEquals(3, res.group);

        res = analyseChordByHindemithUseCase.analyze(List.of(60, 64, 67, 70));
        assertEquals(ChordAnalysis.Column.B_WITH_TRITONE, res.column);
        assertEquals(60, res.rootNote);
        assertEquals(3, res.group);
    }

    @Test
    @DisplayName("validate Group // B) II. b1")
    void validateGroup_B_II_b1(){
        var res = analyseChordByHindemithUseCase.analyze(List.of(60, 64, 67, 70, 74));
        assertEquals(ChordAnalysis.Column.B_WITH_TRITONE, res.column);
        assertEquals(60, res.rootNote);
        assertEquals(4, res.group);

        res = analyseChordByHindemithUseCase.analyze(List.of(60, 64, 70, 74));
        assertEquals(ChordAnalysis.Column.B_WITH_TRITONE, res.column);
        assertEquals(60, res.rootNote);
        assertEquals(4, res.group);

        res = analyseChordByHindemithUseCase.analyze(List.of(60, 63, 67, 69));
        assertEquals(ChordAnalysis.Column.B_WITH_TRITONE, res.column);
        assertEquals(60, res.rootNote);
        assertEquals(4, res.group);

        res = analyseChordByHindemithUseCase.analyze(List.of(60, 64, 66));
        assertEquals(ChordAnalysis.Column.B_WITH_TRITONE, res.column);
        assertEquals(60, res.rootNote);
        assertEquals(4, res.group);
    }

    @Test
    @DisplayName("validate Group // B) II. b2")
    void validateGroup_B_II_b2(){
        var res = analyseChordByHindemithUseCase.analyze(List.of(60, 66, 70));
        assertEquals(ChordAnalysis.Column.B_WITH_TRITONE, res.column);
        assertEquals(66, res.rootNote);
        assertEquals(5, res.group);

        res = analyseChordByHindemithUseCase.analyze(List.of(60, 63, 66, 68));
        assertEquals(ChordAnalysis.Column.B_WITH_TRITONE, res.column);
        assertEquals(68, res.rootNote);
        assertEquals(5, res.group);

        res = analyseChordByHindemithUseCase.analyze(List.of(60, 63, 65, 69));
        assertEquals(ChordAnalysis.Column.B_WITH_TRITONE, res.column);
        assertEquals(65, res.rootNote);
        assertEquals(5, res.group);

        res = analyseChordByHindemithUseCase.analyze(List.of(60, 62, 66));
        assertEquals(ChordAnalysis.Column.B_WITH_TRITONE, res.column);
        assertEquals(62, res.rootNote);
        assertEquals(5, res.group);
    }

    @Test
    @DisplayName("validate Group // B) II. b3")
    void validateGroup_B_II_b3(){
        var res = analyseChordByHindemithUseCase.analyze(List.of(60, 64, 66, 70));
        assertEquals(ChordAnalysis.Column.B_WITH_TRITONE, res.column);
        assertEquals(60, res.rootNote);
        assertEquals(6, res.group);

        res = analyseChordByHindemithUseCase.analyze(List.of(60, 62, 64, 68, 70));
        assertEquals(ChordAnalysis.Column.B_WITH_TRITONE, res.column);
        assertEquals(60, res.rootNote);
        assertEquals(6, res.group);

        res = analyseChordByHindemithUseCase.analyze(List.of(60, 62, 66, 68));
        assertEquals(ChordAnalysis.Column.B_WITH_TRITONE, res.column);
        assertEquals(62, res.rootNote);
        assertEquals(6, res.group);

        res = analyseChordByHindemithUseCase.analyze(List.of(60, 62, 64, 66, 68, 70));
        assertEquals(ChordAnalysis.Column.B_WITH_TRITONE, res.column);
        assertEquals(60, res.rootNote);
        assertEquals(6, res.group);
    }


    @Test
    @DisplayName("validate Group // A) III. 1 – Zeile 1")
    void validateGroup_A_III_1_Line1() {
        // group 7 8 9
        var res = analyseChordByHindemithUseCase.analyze(List.of(60, 62, 67));
        assertEquals(ChordAnalysis.Column.A_TRITONE_FREE, res.column);
        assertEquals(60, res.rootNote);
        assertEquals(7, res.group);

        res = analyseChordByHindemithUseCase.analyze(List.of(60, 65, 67));
        assertEquals(ChordAnalysis.Column.A_TRITONE_FREE, res.column);
        assertEquals(60, res.rootNote);
        assertEquals(7, res.group);

        res = analyseChordByHindemithUseCase.analyze(List.of(60, 67, 69));
        assertEquals(ChordAnalysis.Column.A_TRITONE_FREE, res.column);
        assertEquals(60, res.rootNote);
        assertEquals(7, res.group);

        res = analyseChordByHindemithUseCase.analyze(List.of(60, 67, 70));
        assertEquals(ChordAnalysis.Column.A_TRITONE_FREE, res.column);
        assertEquals(60, res.rootNote);
        assertEquals(7, res.group);

        res = analyseChordByHindemithUseCase.analyze(List.of(60, 67, 71));
        assertEquals(ChordAnalysis.Column.A_TRITONE_FREE, res.column);
        assertEquals(60, res.rootNote);
        assertEquals(8, res.group);

        res = analyseChordByHindemithUseCase.analyze(List.of(60, 62, 64, 67));
        assertEquals(ChordAnalysis.Column.A_TRITONE_FREE, res.column);
        assertEquals(60, res.rootNote);
        assertEquals(7, res.group);

        res = analyseChordByHindemithUseCase.analyze(List.of(60, 64, 65, 67));
        assertEquals(ChordAnalysis.Column.A_TRITONE_FREE, res.column);
        assertEquals(60, res.rootNote);
        assertEquals(9, res.group);

        res = analyseChordByHindemithUseCase.analyze(List.of(60, 64, 67, 69));
        assertEquals(ChordAnalysis.Column.A_TRITONE_FREE, res.column);
        assertEquals(60, res.rootNote);
        assertEquals(7, res.group);

        res = analyseChordByHindemithUseCase.analyze(List.of(60, 64, 67, 68));
        assertEquals(ChordAnalysis.Column.A_TRITONE_FREE, res.column);
        assertEquals(60, res.rootNote);
        assertEquals(9, res.group);

        res = analyseChordByHindemithUseCase.analyze(List.of(60, 63, 67, 68));
        assertEquals(ChordAnalysis.Column.A_TRITONE_FREE, res.column);
        assertEquals(60, res.rootNote);
        assertEquals(9, res.group);

        res = analyseChordByHindemithUseCase.analyze(List.of(60, 62, 63, 67));
        assertEquals(ChordAnalysis.Column.A_TRITONE_FREE, res.column);
        assertEquals(60, res.rootNote);
        assertEquals(9, res.group);

        res = analyseChordByHindemithUseCase.analyze(List.of(60, 62, 65, 67));
        assertEquals(ChordAnalysis.Column.A_TRITONE_FREE, res.column);
        assertEquals(60, res.rootNote);
        assertEquals(7, res.group);
    }


    @Test
    @DisplayName("validate Group // A) III. 1 – Zeile 2")
    void validateGroup_A_III_1_Line2() {
        //groups 7 8 9
        var res = analyseChordByHindemithUseCase.analyze(List.of(60, 63, 67, 70));
        assertEquals(ChordAnalysis.Column.A_TRITONE_FREE, res.column);
        assertEquals(60, res.rootNote);
        assertEquals(7, res.group);

        res = analyseChordByHindemithUseCase.analyze(List.of(60, 63, 67, 71));
        assertEquals(ChordAnalysis.Column.A_TRITONE_FREE, res.column);
        assertEquals(60, res.rootNote);
        assertEquals(8, res.group);

        res = analyseChordByHindemithUseCase.analyze(List.of(60, 64, 67, 71));
        assertEquals(ChordAnalysis.Column.A_TRITONE_FREE, res.column);
        assertEquals(60, res.rootNote);
        assertEquals(8, res.group);

        res = analyseChordByHindemithUseCase.analyze(List.of(60, 64, 67, 71, 74));
        assertEquals(ChordAnalysis.Column.A_TRITONE_FREE, res.column);
        assertEquals(60, res.rootNote);
        assertEquals(8, res.group);

        res = analyseChordByHindemithUseCase.analyze(List.of(60, 63, 67, 70, 74));
        assertEquals(ChordAnalysis.Column.A_TRITONE_FREE, res.column);
        assertEquals(60, res.rootNote);
        assertEquals(8, res.group);

        res = analyseChordByHindemithUseCase.analyze(List.of(60, 67, 70, 77));
        assertEquals(ChordAnalysis.Column.A_TRITONE_FREE, res.column);
        assertEquals(60, res.rootNote);
        assertEquals(7, res.group);

        res = analyseChordByHindemithUseCase.analyze(List.of(60, 63, 67, 70, 74, 77));
        assertEquals(ChordAnalysis.Column.A_TRITONE_FREE, res.column);
        assertEquals(60, res.rootNote);
        assertEquals(8, res.group);

        res = analyseChordByHindemithUseCase.analyze(List.of(60, 67, 69, 71, 74));
        assertEquals(ChordAnalysis.Column.A_TRITONE_FREE, res.column);
        assertEquals(60, res.rootNote);
        assertEquals(8, res.group);

        res = analyseChordByHindemithUseCase.analyze(List.of(60, 64, 75));
        assertEquals(ChordAnalysis.Column.A_TRITONE_FREE, res.column);
        assertEquals(60, res.rootNote);
        assertEquals(8, res.group);

        res = analyseChordByHindemithUseCase.analyze(List.of(60, 67, 74));
        assertEquals(ChordAnalysis.Column.A_TRITONE_FREE, res.column);
        assertEquals(60, res.rootNote);
        assertEquals(7, res.group);

        res = analyseChordByHindemithUseCase.analyze(List.of(60, 67, 69, 76));
        assertEquals(ChordAnalysis.Column.A_TRITONE_FREE, res.column);
        assertEquals(60, res.rootNote);
        assertEquals(7, res.group);

        res = analyseChordByHindemithUseCase.analyze(List.of(60, 67, 68, 75));
        assertEquals(ChordAnalysis.Column.A_TRITONE_FREE, res.column);
        assertEquals(60, res.rootNote);
        assertEquals(9, res.group);
    }


    // ------------------------------------------------------------
    // Redundante/erweiterbare Beispiele: Hier kannst du eigene Akkorde eintragen
    // ------------------------------------------------------------

    @Test
    @DisplayName("validate Group // A) III. 2")
    void validateGroup_A_III_2() {
        var res = analyseChordByHindemithUseCase.analyze(List.of(60, 62, 65));
        assertEquals(ChordAnalysis.Column.A_TRITONE_FREE, res.column);
        assertEquals(65, res.rootNote);
        assertEquals(10, res.group);

        res = analyseChordByHindemithUseCase.analyze(List.of(60, 62, 69));
        assertEquals(ChordAnalysis.Column.A_TRITONE_FREE, res.column);
        assertEquals(62, res.rootNote);
        assertEquals(10, res.group);

        res = analyseChordByHindemithUseCase.analyze(List.of(60, 62, 65, 69));
        assertEquals(ChordAnalysis.Column.A_TRITONE_FREE, res.column);
        assertEquals(62, res.rootNote);
        assertEquals(10, res.group);

        res = analyseChordByHindemithUseCase.analyze(List.of(60, 63, 70));
        assertEquals(ChordAnalysis.Column.A_TRITONE_FREE, res.column);
        assertEquals(63, res.rootNote);
        assertEquals(10, res.group);

        res = analyseChordByHindemithUseCase.analyze(List.of(60, 61, 65, 68));
        assertEquals(ChordAnalysis.Column.A_TRITONE_FREE, res.column);
        assertEquals(61, res.rootNote);
        assertEquals(12, res.group);

        res = analyseChordByHindemithUseCase.analyze(List.of(60, 65, 69, 70));
        assertEquals(ChordAnalysis.Column.A_TRITONE_FREE, res.column);
        assertEquals(65, res.rootNote);
        assertEquals(12, res.group);

        res = analyseChordByHindemithUseCase.analyze(List.of(60, 62, 69, 70));
        assertEquals(ChordAnalysis.Column.A_TRITONE_FREE, res.column);
        assertEquals(62, res.rootNote);
        assertEquals(12, res.group);

        res = analyseChordByHindemithUseCase.analyze(List.of(60, 62, 69, 71));
        assertEquals(ChordAnalysis.Column.A_TRITONE_FREE, res.column);
        assertEquals(62, res.rootNote);
        assertEquals(11, res.group);

        res = analyseChordByHindemithUseCase.analyze(List.of(60, 65, 74));
        assertEquals(ChordAnalysis.Column.A_TRITONE_FREE, res.column);
        assertEquals(65, res.rootNote);
        assertEquals(10, res.group);

        res = analyseChordByHindemithUseCase.analyze(List.of(60, 65, 68, 75));
        assertEquals(ChordAnalysis.Column.A_TRITONE_FREE, res.column);
        assertEquals(68, res.rootNote);
        assertEquals(10, res.group);

        res = analyseChordByHindemithUseCase.analyze(List.of(60, 65, 69, 76));
        assertEquals(ChordAnalysis.Column.A_TRITONE_FREE, res.column);
        assertEquals(69, res.rootNote);
        assertEquals(11, res.group);

        res = analyseChordByHindemithUseCase.analyze(List.of(60, 65, 69, 70, 74));
        assertEquals(ChordAnalysis.Column.A_TRITONE_FREE, res.column);
        assertEquals(65, res.rootNote);
        assertEquals(12, res.group);
    }

    @Test
    @DisplayName("validate Group // B) IV. 1")
    void validateGroup_B_IV_1() {
        var res = analyseChordByHindemithUseCase.analyze(List.of(60, 64, 67, 70, 73));
        assertEquals(ChordAnalysis.Column.B_WITH_TRITONE, res.column);
        assertEquals(60, res.rootNote);
        assertEquals(14, res.group);

        res = analyseChordByHindemithUseCase.analyze(List.of(60, 64, 70, 73));
        assertEquals(ChordAnalysis.Column.B_WITH_TRITONE, res.column);
        assertEquals(60, res.rootNote);
        assertEquals(13, res.group);

        res = analyseChordByHindemithUseCase.analyze(List.of(60, 66, 67, 73));
        assertEquals(ChordAnalysis.Column.B_WITH_TRITONE, res.column);
        assertEquals(60, res.rootNote);
        assertEquals(14, res.group);

        res = analyseChordByHindemithUseCase.analyze(List.of(60, 67, 68, 74));
        assertEquals(ChordAnalysis.Column.B_WITH_TRITONE, res.column);
        assertEquals(60, res.rootNote);
        assertEquals(13, res.group);

        res = analyseChordByHindemithUseCase.analyze(List.of(60, 64, 66, 67, 74));
        assertEquals(ChordAnalysis.Column.B_WITH_TRITONE, res.column);
        assertEquals(60, res.rootNote);
        assertEquals(13, res.group);

        res = analyseChordByHindemithUseCase.analyze(List.of(60, 66, 67, 73, 74, 80));
        assertEquals(ChordAnalysis.Column.B_WITH_TRITONE, res.column);
        assertEquals(60, res.rootNote);
        assertEquals(14, res.group);
    }

    @Test
    @DisplayName("validate Group // B) IV. 2")
    void validateGroup_B_IV_2() {
        var res = analyseChordByHindemithUseCase.analyze(List.of(60, 65, 66, 69, 75));
        assertEquals(ChordAnalysis.Column.B_WITH_TRITONE, res.column);
        assertEquals(65, res.rootNote);
        assertEquals(16, res.group);

        res = analyseChordByHindemithUseCase.analyze(List.of(60, 64, 65, 70, 73));
        assertEquals(ChordAnalysis.Column.B_WITH_TRITONE, res.column);
        assertEquals(65, res.rootNote);
        assertEquals(16, res.group);

        res = analyseChordByHindemithUseCase.analyze(List.of(60, 62, 66, 69, 73, 76));
        assertEquals(ChordAnalysis.Column.B_WITH_TRITONE, res.column);
        assertEquals(62, res.rootNote);
        assertEquals(15, res.group);

        res = analyseChordByHindemithUseCase.analyze(List.of(60, 61, 66, 69, 77));
        assertEquals(ChordAnalysis.Column.B_WITH_TRITONE, res.column);
        assertEquals(66, res.rootNote);
        assertEquals(15, res.group);

        res = analyseChordByHindemithUseCase.analyze(List.of(60, 64, 71, 77));
        assertEquals(ChordAnalysis.Column.B_WITH_TRITONE, res.column);
        assertEquals(64, res.rootNote);
        assertEquals(15, res.group);

        res = analyseChordByHindemithUseCase.analyze(List.of(60, 64, 66, 71));
        assertEquals(ChordAnalysis.Column.B_WITH_TRITONE, res.column);
        assertEquals(64, res.rootNote);
        assertEquals(15, res.group);

        res = analyseChordByHindemithUseCase.analyze(List.of(60, 64, 70, 75));
        assertEquals(ChordAnalysis.Column.B_WITH_TRITONE, res.column);
        assertEquals(75, res.rootNote);
        assertEquals(15, res.group);
    }

    @Test
    @DisplayName("validate Group // A) V.")
    void validateGroup_A_V() {
        var res = analyseChordByHindemithUseCase.analyze(List.of(60, 64, 68));
        assertEquals(ChordAnalysis.Column.A_TRITONE_FREE, res.column);
        assertEquals(60, res.rootNote);
        assertEquals(17, res.group);

        res = analyseChordByHindemithUseCase.analyze(List.of(60, 65, 70));
        assertEquals(ChordAnalysis.Column.A_TRITONE_FREE, res.column);
        assertEquals(65, res.rootNote);
        assertEquals(17, res.group);
    }

    //"validate Group // B) VI."
    @Test
    @DisplayName("validate Group // B) VI.")
    void validateGroup_B_VI() {
        var res = analyseChordByHindemithUseCase.analyze(List.of(60, 63, 66));
        assertEquals(ChordAnalysis.Column.B_WITH_TRITONE, res.column);
        assertEquals(60, res.rootNote);
        assertEquals(18, res.group);

        res = analyseChordByHindemithUseCase.analyze(List.of(61, 64, 67));
        assertEquals(ChordAnalysis.Column.B_WITH_TRITONE, res.column);
        assertEquals(61, res.rootNote);
        assertEquals(18, res.group);

        res = analyseChordByHindemithUseCase.analyze(List.of(60, 63, 69));
        assertEquals(ChordAnalysis.Column.B_WITH_TRITONE, res.column);
        assertEquals(60, res.rootNote);
        assertEquals(18, res.group);

        res = analyseChordByHindemithUseCase.analyze(List.of(60, 66, 69));
        assertEquals(ChordAnalysis.Column.B_WITH_TRITONE, res.column);
        assertEquals(66, res.rootNote);
        assertEquals(18, res.group);

        res = analyseChordByHindemithUseCase.analyze(List.of(60, 63, 66, 69));
        assertEquals(ChordAnalysis.Column.B_WITH_TRITONE, res.column);
        assertEquals(60, res.rootNote);
        assertEquals(18, res.group);
    }

}

