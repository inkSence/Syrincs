package syrincs.c_adapters.midi;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import javax.sound.midi.*;
import java.lang.reflect.Proxy;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class MidiSoundbankTest {
    @TempDir Path dir;

    @Test
    void missingAndInvalidFilesFailBeforePlayback() throws Exception {
        assertThrows(IllegalArgumentException.class, () -> MidiSoundbank.read(dir.resolve("missing.sf2").toString()));
        Path invalid = dir.resolve("invalid.sf2");
        Files.writeString(invalid, "not a soundbank");
        assertThrows(IllegalArgumentException.class, () -> MidiSoundbank.read(invalid.toString()));
    }

    @Test
    void selectedBankReplacesFallbackInstrumentsIncludingPercussion() {
        var calls = new ArrayList<String>();
        MidiSoundbank.install(synth(calls, true, true), bank());
        assertEquals(List.of("supported", "loaded", "unload", "load"), calls);
    }

    @Test
    void unsupportedBankDoesNotUnloadExistingSounds() {
        var calls = new ArrayList<String>();
        assertThrows(IllegalArgumentException.class, () -> MidiSoundbank.install(synth(calls, false, true), bank()));
        assertEquals(List.of("supported"), calls);
    }

    @Test
    void unsuccessfulLoadIsAnErrorRatherThanSilentFallback() {
        var calls = new ArrayList<String>();
        assertThrows(IllegalStateException.class, () -> MidiSoundbank.install(synth(calls, true, false), bank()));
    }

    @Test
    void hardwareOutputRejectsSoundbankWithoutOpeningDevice() throws Exception {
        MidiDevice device = (MidiDevice) Proxy.newProxyInstance(getClass().getClassLoader(),
                new Class<?>[]{MidiDevice.class}, (proxy, method, args) -> {
                    fail("Hardware accessed: " + method.getName());
                    return null;
                });
        var error = assertThrows(IllegalArgumentException.class, () -> new JdkSequencePlayer()
                .play(new Sequence(Sequence.PPQ, 480), device, "bank.sf2"));
        assertTrue(error.getMessage().contains("--device Gervill"));
    }

    @Test
    void invalidFileDoesNotOpenSynthesizerOrSequencer() throws Exception {
        Synthesizer synth = (Synthesizer) Proxy.newProxyInstance(getClass().getClassLoader(),
                new Class<?>[]{Synthesizer.class}, (proxy, method, args) -> {
                    fail("Synthesizer accessed: " + method.getName());
                    return null;
                });
        assertThrows(IllegalArgumentException.class, () -> new JdkSequencePlayer()
                .play(new Sequence(Sequence.PPQ, 480), synth, dir.resolve("missing.sf2").toString()));
    }

    private Soundbank bank() {
        return (Soundbank) Proxy.newProxyInstance(getClass().getClassLoader(),
                new Class<?>[]{Soundbank.class}, (proxy, method, args) -> "Test bank");
    }

    private Synthesizer synth(List<String> calls, boolean supported, boolean loaded) {
        Instrument fallback = new Instrument(bank(), new Patch(0, 0), "Fallback", Object.class) {
            @Override public Object getData() { return null; }
        };
        return (Synthesizer) Proxy.newProxyInstance(getClass().getClassLoader(),
                new Class<?>[]{Synthesizer.class}, (proxy, method, args) -> switch (method.getName()) {
                    case "isSoundbankSupported" -> { calls.add("supported"); yield supported; }
                    case "getLoadedInstruments" -> { calls.add("loaded"); yield new Instrument[]{fallback}; }
                    case "unloadInstrument" -> { assertSame(fallback, args[0]); calls.add("unload"); yield null; }
                    case "loadAllInstruments" -> { calls.add("load"); yield loaded; }
                    default -> throw new AssertionError("Unexpected call: " + method.getName());
                });
    }
}
