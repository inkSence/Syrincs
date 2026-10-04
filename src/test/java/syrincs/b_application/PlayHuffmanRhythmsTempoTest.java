package syrincs.b_application;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import syrincs.a_domain.rhythm.HuffmanRhythm;
import syrincs.a_domain.rhythm.Pattern;
import syrincs.a_domain.rhythm.RhythmSpec;
import syrincs.a_domain.rhythm.VoiceSpec;
import syrincs.b_application.ports.RhythmPlaybackPort;
import syrincs.b_application.ports.dto.RhythmPlaybackOptions;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class PlayHuffmanRhythmsTempoTest {
    @Test
    void overrideAppliesToEntireSequenceWithoutMutatingSelectedRhythms() throws Exception {
        var port = new CapturingPort();
        var useCase = new PlayHuffmanRhythmsUseCase(new PlaybackRhythmUseCase(port), new ValidatePatternsUseCase());
        var first = new HuffmanRhythm(4, 4, 120, "xoooxoxoxoooxoxo");
        var second = new HuffmanRhythm(4, 4, 120, "xoxoxoooxoxoxooo");
        var rhythms = List.of(first, second, first);
        var profile = first.getBeatInformation();
        int information = first.getInformation();
        double deviation = first.getStandardDeviation();
        useCase.playRhythms(rhythms, "Virtual Out");
        Pattern plain = port.pattern;
        assertEquals(120, port.spec.tempoBpm);
        assertEquals(RhythmPlaybackOptions.DEFAULT, port.options);
        useCase.playRhythms(rhythms, "Virtual Out", new RhythmPlaybackOptions(108));
        assertEquals(2, port.calls);
        assertEquals(108, port.spec.tempoBpm);
        assertEquals(3, port.spec.bars);
        assertEquals(4, port.spec.resPerBeat);
        assertEquals("Virtual Out", port.device);
        assertEquals(new RhythmPlaybackOptions(108), port.options);
        for (var voice : plain.voices().keySet()) {
            assertArrayEquals(plain.voices().get(voice), port.pattern.voices().get(voice));
        }
        assertEquals(120, first.getTempo());
        assertEquals(120, second.getTempo());
        assertEquals(profile, first.getBeatInformation());
        assertEquals(information, first.getInformation());
        assertEquals(deviation, first.getStandardDeviation());
        assertEquals("xoooxoxoxoooxoxo", first.getOnsetList());
    }

    @Test
    void overrideUnifiesDifferentSourceTemposButDefaultStillRejectsThem() throws Exception {
        var port = new CapturingPort();
        var useCase = new PlayHuffmanRhythmsUseCase(new PlaybackRhythmUseCase(port), new ValidatePatternsUseCase());
        var rhythms = List.of(new HuffmanRhythm(4, 4, 96, "xoooxoxoxoooxoxo"),
                new HuffmanRhythm(4, 4, 120, "xoxoxoooxoxoxooo"));
        assertThrows(IllegalArgumentException.class, () -> useCase.playRhythms(rhythms));
        assertEquals(0, port.calls);
        useCase.playRhythms(rhythms, null, new RhythmPlaybackOptions(108));
        assertEquals(1, port.calls);
        assertEquals(108, port.spec.tempoBpm);
        assertEquals(2, port.spec.bars);
        assertEquals(96, rhythms.getFirst().getTempo());
    }

    @Test
    void rdlOverridePreservesSourceSpecAndDefaultKeepsItsTempo() throws Exception {
        var port = new CapturingPort();
        var playback = new PlaybackRhythmUseCase(port);
        var source = new RhythmSpec(3, 4, 96, 8, 2);
        var pattern = new Pattern();
        playback.playRhythm(pattern, source, List.of(), "Virtual Out", new RhythmPlaybackOptions(108));
        assertEquals(96, source.tempoBpm);
        assertNotSame(source, port.spec);
        assertEquals(108, port.spec.tempoBpm);
        assertEquals(3, port.spec.meterNumerator);
        assertEquals(4, port.spec.meterDenominator);
        assertEquals(8, port.spec.resPerBeat);
        assertEquals(2, port.spec.bars);
        assertSame(pattern, port.pattern);
        playback.playRhythm(pattern, source, List.of(), null, RhythmPlaybackOptions.DEFAULT);
        assertSame(source, port.spec);
    }

    @ParameterizedTest
    @ValueSource(ints = {-1, 0, 1, 3, 60_000_001, Integer.MAX_VALUE})
    void invalidTempoCannotEnterApplication(int tempo) {
        assertThrows(IllegalArgumentException.class, () -> new RhythmPlaybackOptions(tempo));
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
