package syrincs.rhythm;

import syrincs.a_domain.Tone;
import syrincs.a_domain.chord.Chord;
import syrincs.b_application.ports.MidiOutputPort;

import javax.sound.midi.InvalidMidiDataException;
import javax.sound.midi.MidiUnavailableException;

/**
 * Minimal test fake for MidiOutputPort and MidiDeviceQueryPort.
 * This fake does not send real MIDI and returns an empty device list.
 */
public class FakeMidiOutputPort implements MidiOutputPort, syrincs.b_application.ports.MidiDeviceQueryPort {

    // MidiDeviceQueryPort (DTO-based) stub for tests
    @Override
    public java.util.List<syrincs.b_application.ports.dto.MidiEndpoint> listOutputs() {
        return java.util.List.of();
    }

    @Override
    public syrincs.b_application.ports.dto.MidiEndpoint findOutput(String nameSubstring) {
        return null;
    }

    @Override
    public void sendToneToDevice(Tone tone, String deviceNameSubstring) throws MidiUnavailableException, InvalidMidiDataException, InterruptedException {
        // no-op for tests
    }

    @Override
    public void sendChordToDevice(Chord chord, String deviceNameSubstring, long duration) {
        // no-op for tests
    }
}
