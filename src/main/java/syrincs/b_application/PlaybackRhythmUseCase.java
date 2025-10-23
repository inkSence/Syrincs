package syrincs.b_application;

import syrincs.a_domain.rhythm.Pattern;
import syrincs.a_domain.rhythm.RhythmSpec;
import syrincs.a_domain.rhythm.VoiceSpec;
import syrincs.b_application.ports.RhythmPlaybackPort;

import java.util.List;
import java.util.Objects;

public class PlaybackRhythmUseCase {

    private final RhythmPlaybackPort playbackPort;

    public PlaybackRhythmUseCase(RhythmPlaybackPort playbackPort) {
        this.playbackPort = Objects.requireNonNull(playbackPort, "playbackPort");
    }
    
    public void playRhythm(Pattern pattern, RhythmSpec spec, List<VoiceSpec> voices) throws Exception {
        playbackPort.play(pattern, spec, voices);
    }
}
