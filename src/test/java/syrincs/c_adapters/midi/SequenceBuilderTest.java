package syrincs.c_adapters.midi;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import syrincs.b_application.ports.dto.RhythmPlaybackOptions;
import javax.sound.midi.MetaMessage;
import javax.sound.midi.Sequence;
import javax.sound.midi.ShortMessage;
import java.util.ArrayList;
import syrincs.a_domain.rhythm.Pattern;
import syrincs.a_domain.rhythm.RhythmSpec;
import syrincs.a_domain.rhythm.VoiceSpec;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

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

    @ParameterizedTest
    @ValueSource(ints = {4, 108, 120, 60_000_000})
    void tempoOverrideChangesMetadataAndRealDurationButPreservesGridAndGates(int tempo) throws Exception {
        var pattern = new Pattern();
        boolean[] kick = new boolean[32];
        kick[2] = true;
        kick[16] = true;
        pattern.put("kick", kick);
        var source = new RhythmSpec(4, 4, 120, 4, 2);
        var voices = List.of(new VoiceSpec("kick", 9, 36, 90, 50));
        var builder = new SequenceBuilder(0);
        var plain = builder.build(pattern, source, voices);
        var changed = builder.build(pattern, source, voices, new RhythmPlaybackOptions(tempo));
        int mpqn = tempoValue(changed);
        assertEquals(Math.round(60_000_000.0 / tempo), mpqn);
        assertEquals(120, source.tempoBpm);
        assertEquals(notes(plain), notes(changed));
        assertEquals(List.of("240:144:9:36:90", "300:128:9:36:0", "1920:144:9:36:90", "1980:128:9:36:0"), notes(changed));
        assertEquals(3840, changed.getTickLength());
        assertEquals(8L * mpqn, changed.getMicrosecondLength());
        var withTail = new SequenceBuilder().build(pattern, source, voices, new RhythmPlaybackOptions(tempo));
        assertEquals(3840 + Math.round(2000.0 * tempo / 60000.0 * 480), withTail.getTickLength());
        assertEquals(8L * mpqn + 2_000_000, withTail.getMicrosecondLength(), 10.0);
    }

    @Test
    void absentOverridePreservesAllSequenceEvents() throws Exception {
        var pattern = new Pattern();
        pattern.put("kick", new boolean[]{true, false, true, false});
        var spec = new RhythmSpec(4, 4, 96, 1, 1);
        var voices = List.of(new VoiceSpec("kick", 9, 36, 90, 50));
        var builder = new SequenceBuilder();
        var plain = builder.build(pattern, spec, voices);
        var explicitDefault = builder.build(pattern, spec, voices, RhythmPlaybackOptions.DEFAULT);
        assertEquals(625000, tempoValue(plain));
        var before = plain.getTracks()[0];
        var after = explicitDefault.getTracks()[0];
        assertEquals(before.size(), after.size());
        for (int i = 0; i < before.size(); i++) {
            assertEquals(before.get(i).getTick(), after.get(i).getTick());
            assertArrayEquals(before.get(i).getMessage().getMessage(), after.get(i).getMessage().getMessage());
        }
    }

    private static int tempoValue(Sequence sequence) {
        var track = sequence.getTracks()[0];
        for (int i = 0; i < track.size(); i++) {
            if (track.get(i).getMessage() instanceof MetaMessage message && message.getType() == 0x51) {
                byte[] data = message.getData();
                return ((data[0] & 255) << 16) | ((data[1] & 255) << 8) | (data[2] & 255);
            }
        }
        throw new AssertionError("Missing tempo event");
    }

    private static List<String> notes(Sequence sequence) {
        var result = new ArrayList<String>();
        var track = sequence.getTracks()[0];
        for (int i = 0; i < track.size(); i++) {
            var event = track.get(i);
            if (event.getMessage() instanceof ShortMessage message) {
                result.add(event.getTick() + ":" + message.getCommand() + ":" + message.getChannel() + ":" + message.getData1() + ":" + message.getData2());
            }
        }
        return result;
    }
}
