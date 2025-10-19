package syrincs.b_application.ports;

import syrincs.a_domain.chord.Chord;
import syrincs.a_domain.Tone;
import syrincs.b_application.errors.MidiPortException;

/**
 * Application port for sending tones and chords to a MIDI output.
 * Implementations belong to the outer adapter layer.
 *
 * Clean Architecture: This port does not expose framework types like javax.sound.midi.Sequence or MidiDevice.Info.
 * Device discovery belongs to MidiDeviceQueryPort; rhythm playback uses RhythmPlaybackPort.
 * No framework-specific checked exceptions are exposed; failures should throw MidiPortException.
 */
public interface MidiOutputPort {

    void sendToneToDevice(Tone tone, String deviceNameSubstring) throws MidiPortException;

    void sendChordToDevice(Chord chord, String deviceNameSubstring, long duration) throws MidiPortException;

    /**
     * Variant that allows specifying the target MIDI channel (0-15). For user-facing CLI, map 1-16 → 0-15 before calling.
     */
    void sendChordToDevice(Chord chord, String deviceNameSubstring, long duration, int channelZeroBased) throws MidiPortException;

}
