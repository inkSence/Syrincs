package syrincs.c_adapters.midi;

import javax.sound.midi.MidiSystem;
import javax.sound.midi.Soundbank;
import javax.sound.midi.Synthesizer;
import java.nio.file.Files;
import java.nio.file.Path;

final class MidiSoundbank {
    private MidiSoundbank() {}

    static Soundbank read(String file) throws Exception {
        Path path = Path.of(file);
        if (!Files.isRegularFile(path) || !Files.isReadable(path)) {
            throw new IllegalArgumentException("Cannot read --soundbank file: " + file);
        }
        Soundbank bank;
        try {
            bank = MidiSystem.getSoundbank(path.toFile());
        } catch (Exception e) {
            throw new IllegalArgumentException("Invalid --soundbank file: " + file, e);
        }
        if (bank == null) throw new IllegalArgumentException("Invalid --soundbank file: " + file);
        return bank;
    }

    static void install(Synthesizer synthesizer, Soundbank bank) {
        if (!synthesizer.isSoundbankSupported(bank)) {
            throw new IllegalArgumentException("MIDI synthesizer does not support --soundbank: " + bank.getName());
        }
        // Remove fallback patches, including percussion, so only the selected bank supplies sounds.
        for (var instrument : synthesizer.getLoadedInstruments()) {
            synthesizer.unloadInstrument(instrument);
        }
        if (!synthesizer.loadAllInstruments(bank)) {
            throw new IllegalStateException("Cannot load --soundbank: " + bank.getName());
        }
    }
}
