package syrincs.b_application.ports.dto;

import syrincs.a_domain.rhythm.RhythmSpec;

/** Playback-only overrides; never part of the rhythm's analysis or identity. */
public record RhythmPlaybackOptions(Integer tempoBpm, boolean countIn, String soundbankPath) {
    public static final RhythmPlaybackOptions DEFAULT = new RhythmPlaybackOptions(null, false);
    // The playback transport represents a quarter note in 1..16,777,215 microseconds.
    public static final int MIN_TEMPO_BPM = 4;
    public static final int MAX_TEMPO_BPM = 60_000_000;

    /** Compatibility constructor for tempo and count-in overrides. */
    public RhythmPlaybackOptions(Integer tempoBpm, boolean countIn) {
        this(tempoBpm, countIn, null);
    }

    /** Compatibility constructor for a tempo-only override. */
    public RhythmPlaybackOptions(Integer tempoBpm) {
        this(tempoBpm, false);
    }

    /** Compatibility constructor for a count-in-only override. */
    public RhythmPlaybackOptions(boolean countIn) {
        this(null, countIn);
    }

    public RhythmPlaybackOptions {
        if (soundbankPath != null && soundbankPath.isBlank()) {
            throw new IllegalArgumentException("--soundbank must name a soundbank file.");
        }
        if (tempoBpm != null && (tempoBpm < MIN_TEMPO_BPM || tempoBpm > MAX_TEMPO_BPM)) {
            throw new IllegalArgumentException("--tempo must be a whole BPM value between "
                    + MIN_TEMPO_BPM + " and " + MAX_TEMPO_BPM + " (playback transport limits).");
        }
    }

    public int effectiveTempo(int sourceTempo) {
        return tempoBpm == null ? sourceTempo : tempoBpm;
    }

    public RhythmSpec applyTo(RhythmSpec source) {
        int tempo = effectiveTempo(source.tempoBpm);
        return tempo == source.tempoBpm ? source : new RhythmSpec(source.meterNumerator,
                source.meterDenominator, tempo, source.resPerBeat, source.bars);
    }

    public void validateMeter(int numerator, int denominator) {
        if (countIn && (numerator != 4 || denominator != 4)) {
            throw new IllegalArgumentException("Count-in currently requires 4/4 meter; got "
                    + numerator + "/" + denominator + ".");
        }
    }
}
