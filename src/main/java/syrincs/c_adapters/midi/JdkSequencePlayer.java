package syrincs.c_adapters.midi;

import javax.sound.midi.*;

/**
 * Default adapter implementation for playing a MIDI Sequence using the JDK MIDI API.
 * Device resolution follows the same semantics as JdkMidiOutputAdapter: optional substring match
 * or fallback to default from env/config, otherwise first OUT device.
 */
public class JdkSequencePlayer implements SequencePlayer {

    @Override
    public void play(Sequence sequence, String deviceNameSubstring) throws Exception {
        if (sequence == null) throw new IllegalArgumentException("sequence must not be null");

        MidiDevice.Info target = DeviceResolver.resolveOutput(deviceNameSubstring);
        if (target == null) {
            if (deviceNameSubstring != null && !deviceNameSubstring.isBlank()) {
                throw new IllegalArgumentException("Unknown MIDI device: '" + deviceNameSubstring + "'");
            } else {
                throw new IllegalArgumentException("No suitable MIDI output device found. Set env SYRINCS_MIDI_DEVICE or pass --device.");
            }
        }

        Sequencer sequencer = MidiSystem.getSequencer(false); // not connected to default Synth
        boolean openedSeq = false;
        MidiDevice device = MidiSystem.getMidiDevice(target);
        boolean openedDev = false;
        try {
            if (!sequencer.isOpen()) { sequencer.open(); openedSeq = true; }
            if (!device.isOpen()) { device.open(); openedDev = true; }
            Transmitter seqTx = sequencer.getTransmitter();
            Receiver devRx = device.getReceiver();
            seqTx.setReceiver(devRx);

            sequencer.setSequence(sequence);
            sequencer.start();
            while (sequencer.isRunning()) {
                Thread.sleep(10);
            }
        } finally {
            try { if (openedSeq && sequencer.isOpen()) sequencer.close(); } catch (Exception ignored) {}
            try { if (openedDev && device.isOpen()) device.close(); } catch (Exception ignored) {}
        }
    }

    private MidiDevice.Info findOutputByName(String nameSubstring) {
        return DeviceResolver.findOutputInfoBySubstring(nameSubstring);
    }

    // Resolve default: env/config override → preferred brand hints → first available OUT
    private MidiDevice.Info autoSelectDefaultOutput() {
        return DeviceResolver.autoSelectDefaultOutput();
    }

    private MidiDevice.Info[] listMidiOutputs() {
        return DeviceResolver.listOutputInfos();
    }
}
