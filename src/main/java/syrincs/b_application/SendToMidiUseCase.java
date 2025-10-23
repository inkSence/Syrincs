package syrincs.b_application;

import syrincs.a_domain.Tone;
import syrincs.a_domain.chord.Chord;
import syrincs.b_application.ports.MidiOutputPort;

public class SendToMidiUseCase {
    private final MidiOutputPort midiOutput;
    public SendToMidiUseCase(MidiOutputPort midiOutput) {
        this.midiOutput = midiOutput;
    }

    public void sendToneToDevice(Tone tone, String deviceNameSubstring) {
        midiOutput.sendToneToDevice(tone, deviceNameSubstring);
    }

    public void sendChordToDevice(Chord chord, long duration) {
        // Delegate to adapter; no application-layer printing or sleeping
        midiOutput.sendChordToDevice(chord, duration);
    }

    public void sendChordToDevice(Chord chord, long duration, Integer channelZeroBased) {
        if (channelZeroBased == null) {
            midiOutput.sendChordToDevice(chord, duration);
        } else {
            midiOutput.sendChordToDevice(chord, duration, channelZeroBased);
        }
    }
}
