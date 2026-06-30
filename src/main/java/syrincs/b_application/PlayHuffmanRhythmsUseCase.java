package syrincs.b_application;

import syrincs.a_domain.rhythm.HuffmanRhythm;
import syrincs.a_domain.rhythm.Pattern;
import syrincs.a_domain.rhythm.RhythmMapperFromOnsetStringToKickAndSnare;
import syrincs.a_domain.rhythm.RhythmSpec;
import syrincs.a_domain.rhythm.VoiceSpec;

import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Use case to play a list of HuffmanRhythm objects as one continuous pattern.
 *
 * Responsibilities:
 * - Map each Huffman rhythm's onset string to kick/snare Pattern using the domain mapper
 * - Derive a RhythmSpec from HuffmanRhythm (meter, tempo; resPerBeat=4, bars inferred)
 * - Validate and delegate playback to the existing PlaybackRhythmUseCase
 */
public class PlayHuffmanRhythmsUseCase {
    private final PlaybackRhythmUseCase playback;
    private final ValidatePatternsUseCase validate;

    public PlayHuffmanRhythmsUseCase(PlaybackRhythmUseCase playback, ValidatePatternsUseCase validate) {
        this.playback = Objects.requireNonNull(playback, "playback");
        this.validate = Objects.requireNonNull(validate, "validate");
    }

    /**
     * Plays the given rhythms as one concatenated pattern. No-op for null/empty lists.
     */
    public void playRhythms(List<HuffmanRhythm> rhythms) throws Exception {
        playRhythms(rhythms, null);
    }

    public void playRhythms(List<HuffmanRhythm> rhythms, String deviceNameSubstring) throws Exception {
        if (rhythms == null || rhythms.isEmpty()) {
            return;
        }
        List<VoiceSpec> voices = RhythmMapperFromOnsetStringToKickAndSnare.defaultVoiceSpecs();
        Pattern combinedPattern = combinePatterns(rhythms, voices);
        RhythmSpec combinedSpec = combinedSpec(rhythms);

        validate.validate(combinedPattern, combinedSpec, voices);
        playback.playRhythm(combinedPattern, combinedSpec, voices, deviceNameSubstring);
    }

    private Pattern combinePatterns(List<HuffmanRhythm> rhythms, List<VoiceSpec> voices) throws Exception {
        RhythmSpec firstSpec = buildSpec(Objects.requireNonNull(rhythms.getFirst(), "rhythm"));
        int totalSteps = 0;
        for (HuffmanRhythm hr : rhythms) {
            RhythmSpec spec = buildSpec(Objects.requireNonNull(hr, "rhythm"));
            requireCompatible(firstSpec, spec);
            totalSteps += spec.totalSteps();
        }

        boolean[] kick = new boolean[totalSteps];
        boolean[] snare = new boolean[totalSteps];
        int offset = 0;
        for (HuffmanRhythm hr : rhythms) {
            Pattern pattern = buildPattern(hr);
            RhythmSpec spec = buildSpec(hr);
            validate.validate(pattern, spec, voices);
            Map<String, boolean[]> voicePatterns = pattern.voices();
            boolean[] kickPart = voicePatterns.get("kick");
            boolean[] snarePart = voicePatterns.get("snare");
            System.arraycopy(kickPart, 0, kick, offset, kickPart.length);
            System.arraycopy(snarePart, 0, snare, offset, snarePart.length);
            offset += spec.totalSteps();
        }

        Pattern combined = new Pattern();
        combined.put("kick", kick);
        combined.put("snare", snare);
        return combined;
    }

    private RhythmSpec combinedSpec(List<HuffmanRhythm> rhythms) throws Exception {
        RhythmSpec first = buildSpec(Objects.requireNonNull(rhythms.getFirst(), "rhythm"));
        int bars = 0;
        for (HuffmanRhythm hr : rhythms) {
            RhythmSpec spec = buildSpec(Objects.requireNonNull(hr, "rhythm"));
            requireCompatible(first, spec);
            bars += spec.bars;
        }
        return new RhythmSpec(first.meterNumerator, first.meterDenominator, first.tempoBpm, first.resPerBeat, bars);
    }

    private void requireCompatible(RhythmSpec first, RhythmSpec next) {
        if (first.meterNumerator != next.meterNumerator
                || first.meterDenominator != next.meterDenominator
                || first.tempoBpm != next.tempoBpm
                || first.resPerBeat != next.resPerBeat) {
            throw new IllegalArgumentException("Cannot concatenate Huffman rhythms with different meter, tempo, or grid.");
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
