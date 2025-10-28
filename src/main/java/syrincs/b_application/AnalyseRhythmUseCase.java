package syrincs.b_application;

import syrincs.a_domain.rhythm.HuffmanRhythm;

/**
 * AnalyseRhythmUseCase
 * Builds a HuffmanRhythm from a given onset string using default meter/tempo values.
 */
public class AnalyseRhythmUseCase {

    /**
     * Create a HuffmanRhythm with defaults numerator=4, denominator=4, tempo=90.
     * The onset string may contain spaces and upper/lower case; the domain class normalizes it.
     */
    public HuffmanRhythm analyze(String onsetList) {
        if (onsetList == null) throw new IllegalArgumentException("onsetList must not be null");
        return new HuffmanRhythm(4, 4, 90, onsetList);
    }

    public int analyzeInformation(String onsetList) {
        if (onsetList == null) throw new IllegalArgumentException("onsetList must not be null");
        return new HuffmanRhythm(4, 4, 90, onsetList).getInformation();
    }
}
