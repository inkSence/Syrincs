package syrincs.b_application.ports;

import syrincs.a_domain.chord.Chord;
import syrincs.a_domain.Tone;

import javax.sound.midi.InvalidMidiDataException;
import javax.sound.midi.MidiUnavailableException;


/**
 * Application port for sending tones and chords to a MIDI output.
 * Implementations belong to the outer adapter layer.
 *
 * Clean Architecture: This port does not expose framework types like javax.sound.midi.Sequence or MidiDevice.Info.
 * Device discovery belongs to MidiDeviceQueryPort; rhythm playback uses RhythmPlaybackPort.
 */
public interface MidiOutputPort {

    void sendToneToDevice(Tone tone, String deviceNameSubstring)
            throws MidiUnavailableException, InvalidMidiDataException, InterruptedException;

    void sendChordToDevice(Chord chord, String deviceNameSubstring, long duration);


}
