package syrincs.c_adapters.midi;

import javax.sound.midi.MidiDevice;
import javax.sound.midi.Sequence;

/**
 * Adapter-layer SPI for playing a javax.sound.midi.Sequence on a selected MIDI output device.
 * This intentionally lives in the adapter layer to avoid leaking javax.sound.midi types into
 * the application boundary.
 */
public interface SequencePlayer {
    void play(Sequence sequence, MidiDevice device) throws Exception;

    default void play(Sequence sequence, MidiDevice device, String soundbankPath) throws Exception {
        if (soundbankPath != null) {
            throw new UnsupportedOperationException("This sequence player does not support --soundbank.");
        }
        play(sequence, device);
    }
}
