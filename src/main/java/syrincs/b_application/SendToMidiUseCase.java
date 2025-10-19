package syrincs.b_application;

import syrincs.a_domain.Tone;
import syrincs.a_domain.chord.Chord;
import syrincs.a_domain.hindemith.HindemithChord;
import syrincs.b_application.ports.MidiOutputPort;

import javax.sound.midi.InvalidMidiDataException;
import javax.sound.midi.MidiDevice;
import javax.sound.midi.MidiUnavailableException;
import javax.sound.midi.Sequence;

public class SendToMidiUseCase {
    private final MidiOutputPort midiOutput;
    public SendToMidiUseCase(MidiOutputPort midiOutput) {
        this.midiOutput = midiOutput;
    }

    public MidiDevice.Info[] listMidiOutputs() {
        return midiOutput.listMidiOutputs();
    }

    public MidiDevice.Info findOutputByName(String nameSubstring) {
        return midiOutput.findOutputByName(nameSubstring);
    }

    public void sendToneToDevice(Tone tone, String deviceNameSubstring) throws MidiUnavailableException, InvalidMidiDataException, InterruptedException {
        midiOutput.sendToneToDevice(tone, deviceNameSubstring);
    }

    public void sendChordToDevice(Chord chord, String deviceNameSubstring, long duration) throws MidiUnavailableException, InvalidMidiDataException, InterruptedException {
        midiOutput.sendChordToDevice(chord, deviceNameSubstring, duration);
        long start = System.currentTimeMillis();
        System.out.println("[MIDI] Playing chord: " + chord);
        long elapsed = System.currentTimeMillis() - start;
        long sleep = duration - elapsed;
        if (sleep > 0) Thread.sleep(sleep);
    }

    public void playSequence(Sequence sequence, String deviceNameSubstring) throws Exception {
        midiOutput.playSequence(sequence, deviceNameSubstring);
    }
}
