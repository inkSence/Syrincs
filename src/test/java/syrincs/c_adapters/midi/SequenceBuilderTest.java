package syrincs.c_adapters.midi;

import org.junit.jupiter.api.Test;
import syrincs.a_domain.rhythm.Pattern;
import syrincs.a_domain.rhythm.RhythmSpec;
import syrincs.a_domain.rhythm.VoiceSpec;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class SequenceBuilderTest {

    @Test
    void build_keepsSequenceOpenForConfiguredTailAfterPatternEnd() throws Exception {
        Pattern pattern = new Pattern();
        pattern.put("kick", new boolean[16]);
        RhythmSpec spec = new RhythmSpec(4, 4, 120, 4, 1);
        VoiceSpec kick = new VoiceSpec("kick", 9, 36, 90, 50);

        var sequence = new SequenceBuilder(2_000).build(pattern, spec, List.of(kick));

        long patternTicks = 16L * (MidiConfig.defaults().ppq() / spec.resPerBeat);
        long tailTicks = 4L * MidiConfig.defaults().ppq();
        assertEquals(patternTicks + tailTicks, sequence.getTickLength());
    }

    @Test
    void build_canDisableTrailingSilenceForPreciseSequenceTests() throws Exception {
        Pattern pattern = new Pattern();
        pattern.put("kick", new boolean[16]);
        RhythmSpec spec = new RhythmSpec(4, 4, 120, 4, 1);
        VoiceSpec kick = new VoiceSpec("kick", 9, 36, 90, 50);

        var sequence = new SequenceBuilder(0).build(pattern, spec, List.of(kick));

        long patternTicks = 16L * (MidiConfig.defaults().ppq() / spec.resPerBeat);
        assertEquals(patternTicks, sequence.getTickLength());
    }
}
