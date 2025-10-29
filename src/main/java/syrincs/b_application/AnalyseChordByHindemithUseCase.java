package syrincs.b_application;

import syrincs.a_domain.hindemith.ChordAnalysis;

import java.util.List;

/**
 * AnalyseChordByHindemithUseCase
 * Application-layer use case that delegates chord analysis to the
 * domain service ChordAnalysisHindemith.
 */
public class AnalyseChordByHindemithUseCase {

    private final ChordAnalysis analyzer = new ChordAnalysis();

    /**
     * Analyze a chord given as a list of MIDI notes (can be unsorted).
     * Returns the domain result (column A/B, optional root, optional group 1..14, frame interval, sorted notes).
     */
    public ChordAnalysis.Result analyze(List<Integer> midiNotes) {
        return analyzer.analyze(midiNotes);
    }


}
