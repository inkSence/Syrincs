package syrincs.c_adapters.midi;

import javax.sound.midi.*;

/**
 * Default adapter implementation for playing a MIDI Sequence using the JDK MIDI API.
 * Device resolution follows the same semantics as JdkMidiOutputAdapter: optional substring match
 * or fallback to default from env/config, otherwise first OUT device.
 */
public class JdkSequencePlayer implements SequencePlayer {

    @Override
    public void play(Sequence sequence, MidiDevice device) throws Exception {
        if (sequence == null) throw new IllegalArgumentException("sequence must not be null");
        Sequencer sequencer = MidiSystem.getSequencer(false); // not connected to default Synth
        boolean openedDev = false;
        boolean openedSeq = false;

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

}
