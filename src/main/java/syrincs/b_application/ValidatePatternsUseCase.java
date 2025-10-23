package syrincs.b_application;

import syrincs.a_domain.rhythm.Pattern;
import syrincs.a_domain.rhythm.RhythmSpec;
import syrincs.a_domain.rhythm.VoiceSpec;

import java.util.List;
import java.util.Map;

public class ValidatePatternsUseCase {

    public void validate(Pattern pattern, RhythmSpec spec, List<VoiceSpec> voices) throws ValidationException {
        // non null
        if (pattern == null) throw new ValidationException("Pattern is null");
        if (spec == null) throw new ValidationException("RhythmSpec is null");
        if (voices == null || voices.isEmpty()) throw new ValidationException("Missing voice declarations");
        VoiceSpec kick = find(voices, "kick");
        VoiceSpec snare = find(voices, "snare");
        if (kick == null) throw new ValidationException("Missing voice 'kick'");
        if (snare == null) throw new ValidationException("Missing voice 'snare'");

        Map<String, boolean[]> vmap = pattern.voices();
        if (!vmap.containsKey("kick")) throw new ValidationException("Missing voice: 'kick' (no pattern found)");
        if (!vmap.containsKey("snare")) throw new ValidationException("Missing voice: 'snare' (no pattern found)");
        if (vmap.size() != 2) throw new ValidationException("Unexpected voices in pattern: expected exactly 'kick' and 'snare'");
        validateStepLength(spec, vmap);
        validateRanges(spec, kick, snare);
    }

    private void validateStepLength(RhythmSpec spec, Map<String, boolean[]> vmap) throws ValidationException {
        int expected = spec.totalSteps();
        for (var e : vmap.entrySet()) {
            if (e.getValue() == null) throw new ValidationException("Pattern for voice '"+e.getKey()+"' is null");
            if (e.getValue().length != expected) {
                throw new ValidationException("Pattern length for voice '"+e.getKey()+"' is "+e.getValue().length+", expected "+expected);
            }
        }
    }

    private void validateRanges(RhythmSpec spec, VoiceSpec kick, VoiceSpec snare) throws ValidationException {
        // Ranges
        if (spec.ppq <= 0) throw new ValidationException("ppq must be > 0");
        if (spec.resPerBeat <= 0) throw new ValidationException("res-per-beat must be > 0");
        if (spec.beats() <= 0) throw new ValidationException("time numerator must be > 0");
        if (spec.bars <= 0) throw new ValidationException("bars must be > 0");
        if (kick.channel < 0 || kick.channel > 15) throw new ValidationException("kick channel must be 0..15");
        if (snare.channel < 0 || snare.channel > 15) throw new ValidationException("snare channel must be 0..15");
        if (kick.note < 0 || kick.note > 127) throw new ValidationException("kick note must be 0..127");
        if (snare.note < 0 || snare.note > 127) throw new ValidationException("snare note must be 0..127");
        if (kick.velocity < 0 || kick.velocity > 127) throw new ValidationException("kick vel must be 0..127");
        if (snare.velocity < 0 || snare.velocity > 127) throw new ValidationException("snare vel must be 0..127");
    }

    private VoiceSpec find(List<VoiceSpec> list, String name) {
        for (VoiceSpec v : list) if (v.name.equalsIgnoreCase(name)) return v;
        return null;
    }

    public static class ValidationException extends Exception {
        public ValidationException(String msg) { super(msg); }
    }
}
