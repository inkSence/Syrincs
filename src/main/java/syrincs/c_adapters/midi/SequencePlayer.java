package syrincs.c_adapters.midi;

import javax.sound.midi.Sequence;

/**
 * Adapter-layer SPI for playing a javax.sound.midi.Sequence on a selected MIDI output device.
 * This intentionally lives in the adapter layer to avoid leaking javax.sound.midi types into
 * the application boundary.
 */
public interface SequencePlayer {
    void play(Sequence sequence, String deviceNameSubstring) throws Exception;
}
