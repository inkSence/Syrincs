package syrincs.b_application.ports;

import syrincs.a_domain.rhythm.Pattern;
import syrincs.a_domain.rhythm.RhythmSpec;
import syrincs.a_domain.rhythm.VoiceSpec;

import java.util.List;

/**
 * Application port for playing a rhythm pattern using domain-only types.
 * Implementations belong to the outer adapter layer and may use javax.sound.midi internally.
 */
public interface RhythmPlaybackPort {
    void play(Pattern pattern, RhythmSpec spec, List<VoiceSpec> voices) throws Exception;
}
