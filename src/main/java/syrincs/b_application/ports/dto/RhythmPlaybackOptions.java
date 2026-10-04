package syrincs.b_application.ports.dto;

import syrincs.a_domain.rhythm.RhythmSpec;

/** Playback-only overrides; the source rhythm and its analysis remain unchanged. */
public record RhythmPlaybackOptions(Integer tempoBpm) {
    public static final RhythmPlaybackOptions DEFAULT = new RhythmPlaybackOptions(null);
    // The playback transport represents a quarter note in 1..16,777,215 microseconds.
    public static final int MIN_TEMPO_BPM = 4;
    public static final int MAX_TEMPO_BPM = 60_000_000;

    public RhythmPlaybackOptions {
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
}
