package syrincs.b_application;

import syrincs.a_domain.Tone;
import syrincs.a_domain.chord.Chord;
import syrincs.b_application.ports.MidiOutputPort;

import javax.sound.midi.InvalidMidiDataException;
import javax.sound.midi.MidiUnavailableException;

public class SendToMidiUseCase {
    private final MidiOutputPort midiOutput;
    public SendToMidiUseCase(MidiOutputPort midiOutput) {
        this.midiOutput = midiOutput;
    }

    public void sendToneToDevice(Tone tone, String deviceNameSubstring) throws MidiUnavailableException, InvalidMidiDataException, InterruptedException {
        midiOutput.sendToneToDevice(tone, deviceNameSubstring);
    }

    public void sendChordToDevice(Chord chord, String deviceNameSubstring, long duration) {
        // Delegate to adapter; no application-layer printing or sleeping
        midiOutput.sendChordToDevice(chord, deviceNameSubstring, duration);
    }
}
