package syrincs.b_application;

import org.junit.jupiter.api.Test;
import syrincs.a_domain.rhythm.HuffmanRhythm;
import syrincs.a_domain.rhythm.Pattern;
import syrincs.a_domain.rhythm.RhythmSpec;
import syrincs.a_domain.rhythm.VoiceSpec;
import syrincs.b_application.ports.RhythmPlaybackPort;
import syrincs.b_application.ports.dto.RhythmPlaybackOptions;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class PlayHuffmanRhythmsUseCaseTest {
    @Test
    void countInIsForwardedOnceForCompleteSelectionWithoutChangingMusicalContent() throws Exception {
        var port = new CapturingPort();
        var useCase = new PlayHuffmanRhythmsUseCase(new PlaybackRhythmUseCase(port), new ValidatePatternsUseCase());
        var first = new HuffmanRhythm(4, 4, 108, "xoooxoxoxoooxoxo");
        var second = new HuffmanRhythm(4, 4, 108, "xoxoxoooxoxoxooo");
        var rhythms = List.of(first, second, first);
        var profile = first.getBeatInformation();
        var information = first.getInformation();
        var deviation = first.getStandardDeviation();
        useCase.playRhythms(rhythms, "Virtual Out");
        Pattern plain = port.pattern;
        assertEquals(RhythmPlaybackOptions.DEFAULT, port.options);

        useCase.playRhythms(rhythms, "Virtual Out", new RhythmPlaybackOptions(true));
        assertEquals(2, port.calls);
        assertTrue(port.options.countIn());
        assertEquals("Virtual Out", port.device);
        assertEquals(3, port.spec.bars);
        assertEquals(108, port.spec.tempoBpm);
        assertEquals(2, port.pattern.voices().size());
        for (var voice : plain.voices().keySet()) {
            assertArrayEquals(plain.voices().get(voice), port.pattern.voices().get(voice));
        }
        assertEquals(profile, first.getBeatInformation());
        assertEquals(information, first.getInformation());
        assertEquals(deviation, first.getStandardDeviation());
        assertEquals("xoooxoxoxoooxoxo", first.getOnsetList());
    }

    @Test
    void unsupportedMeterIsRejectedBeforeCallingPort() {
        var port = new CapturingPort();
        var useCase = new PlayHuffmanRhythmsUseCase(new PlaybackRhythmUseCase(port), new ValidatePatternsUseCase());
        assertThrows(IllegalArgumentException.class, () -> useCase.playRhythms(
                List.of(new HuffmanRhythm(4, 8, 120, "xoxoxoooxoxoxooo")), null, new RhythmPlaybackOptions(true)));
        assertEquals(0, port.calls);
    }

    private static class CapturingPort implements RhythmPlaybackPort {
        int calls;
        Pattern pattern;
        RhythmSpec spec;
        String device;
        RhythmPlaybackOptions options;

        @Override
        public void play(Pattern pattern, RhythmSpec spec, List<VoiceSpec> voices) {
            fail("Expected explicit playback options");
        }

        @Override
        public void play(Pattern pattern, RhythmSpec spec, List<VoiceSpec> voices, String device,
                         RhythmPlaybackOptions options) {
            calls++;
            this.pattern = pattern;
            this.spec = spec;
            this.device = device;
            this.options = options;
        }
    }
}
