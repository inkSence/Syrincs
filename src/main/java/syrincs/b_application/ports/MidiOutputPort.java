package syrincs.b_application.ports;

import syrincs.a_domain.chord.Chord;
import syrincs.a_domain.hindemith.HindemithChord;
import syrincs.a_domain.Tone;

import javax.sound.midi.MidiDevice;
import javax.sound.midi.InvalidMidiDataException;
import javax.sound.midi.MidiUnavailableException;
import javax.sound.midi.Sequence;

/**
 * Application port for sending tones to a MIDI output and discovering outputs.
 * Implementations belong to the d_frameworksAndDrivers layer.
 */
public interface MidiOutputPort {
    MidiDevice.Info[] listMidiOutputs();

    MidiDevice.Info findOutputByName(String nameSubstring);

    void sendToneToDevice(Tone tone, String deviceNameSubstring)
            throws MidiUnavailableException, InvalidMidiDataException, InterruptedException;

    void sendChordToDevice(Chord chord, String deviceNameSubstring, long duration);

    void playSequence(Sequence sequence, String deviceNameSubstring) throws Exception;
}
