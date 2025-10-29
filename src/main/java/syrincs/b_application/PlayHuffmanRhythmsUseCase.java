package syrincs.b_application;

import syrincs.a_domain.rhythm.HuffmanRhythm;
import syrincs.a_domain.rhythm.Pattern;
import syrincs.a_domain.rhythm.RhythmMapperFromOnsetStringToKickAndSnare;
import syrincs.a_domain.rhythm.RhythmSpec;
import syrincs.a_domain.rhythm.VoiceSpec;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * Use case to sequentially play a list of HuffmanRhythm objects.
 *
 * Responsibilities:
 * - Map each Huffman rhythm's onset string to kick/snare Pattern using the domain mapper
 * - Derive a RhythmSpec from HuffmanRhythm (meter, tempo; resPerBeat=4, bars inferred)
 * - Validate and delegate playback to the existing PlaybackRhythmUseCase
 */
public class PlayHuffmanRhythmsUseCase {
    // Todo: Diese Klasse sollte aufgelöst werden. Irgendwie kanns nicht sein, dass hier andere UseCases mit drin sind. Dafür ist der Interactor da. Man sollte zuerst bspw. die Teile wie Spec erstellen, dann validieren und dann abspielen.

    private final PlaybackRhythmUseCase playback;
    private final ValidatePatternsUseCase validate;

    public PlayHuffmanRhythmsUseCase(PlaybackRhythmUseCase playback, ValidatePatternsUseCase validate) {
        this.playback = Objects.requireNonNull(playback, "playback");
        this.validate = Objects.requireNonNull(validate, "validate");
    }

    /**
     * Plays the given rhythms one after another. No-op for null/empty lists.
     */
    public void playRhythms(List<HuffmanRhythm> rhythms) throws Exception {
        for (HuffmanRhythm hr : rhythms) {
            Pattern pattern = buildPattern(hr);
            List<VoiceSpec> voices = RhythmMapperFromOnsetStringToKickAndSnare.defaultVoiceSpecs();
            RhythmSpec spec = buildSpec(hr);

            // Validate & play
            validate.validate(pattern, spec, voices);
            playback.playRhythm(pattern, spec, voices);
        }
    }

    private Pattern buildPattern(HuffmanRhythm hr) {
        var mapper = RhythmMapperFromOnsetStringToKickAndSnare.defaultMapper(
                RhythmMapperFromOnsetStringToKickAndSnare.Style.DEFAULT);
        var result = mapper.map(hr.getOnsetList());
        return RhythmMapperFromOnsetStringToKickAndSnare.toPattern(result);
    }

    private RhythmSpec buildSpec(HuffmanRhythm hr) throws Exception {
        // Derive spec from HuffmanRhythm (assume 16th grid -> resPerBeat=4)
        int num = hr.getNumerator();
        int den = hr.getDenominator();
        int resPerBeat = 4;
        int bars = Math.max(1, hr.getOnsetList().length() / (num * resPerBeat));
        return new RhythmSpec(num, den, hr.getTempo(), resPerBeat, bars);
    }
}
