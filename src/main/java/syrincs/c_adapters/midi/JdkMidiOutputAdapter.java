package syrincs.c_adapters.midi;

import syrincs.a_domain.chord.Chord;
import syrincs.a_domain.Tone;
import syrincs.b_application.ports.MidiOutputPort;

import javax.sound.midi.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Frameworks & Drivers implementation of MidiOutputPort using the JDK javax.sound.midi API.
 */
public class JdkMidiOutputAdapter implements MidiOutputPort {

    @Override
    public MidiDevice.Info[] listMidiOutputs() {
        MidiDevice.Info[] all = MidiSystem.getMidiDeviceInfo();
        List<MidiDevice.Info> outs = new ArrayList<>();
        for (MidiDevice.Info info : all) {
            try {
                MidiDevice dev = MidiSystem.getMidiDevice(info);
                int maxReceivers = dev.getMaxReceivers();
                if (maxReceivers != 0) { // -1 unlimited or >0
                    outs.add(info);
                }
            } catch (MidiUnavailableException ignored) {
            }
        }
        return outs.toArray(new MidiDevice.Info[0]);
    }

    @Override
    public MidiDevice.Info findOutputByName(String nameSubstring) {
        if (nameSubstring == null) return null;
        String needle = nameSubstring.toLowerCase();
        for (MidiDevice.Info info : listMidiOutputs()) {
            String hay = (info.getName() + " " + info.getDescription() + " " + info.getVendor()).toLowerCase();
            if (hay.contains(needle)) {
                return info;
            }
        }
        return null;
    }

    @Override
    public void sendToneToDevice(Tone tone, String deviceNameSubstring) throws MidiUnavailableException, InvalidMidiDataException, InterruptedException {
        MidiDevice.Info info = (deviceNameSubstring != null && !deviceNameSubstring.isEmpty())
                ? findOutputByName(deviceNameSubstring)
                : null;
        if (info == null) {
            // Centralized auto-selection previously in Main
            info = autoSelectDefaultOutput();
        }
        if (info == null) {
            throw new MidiUnavailableException("No suitable MIDI output device found" +
                    (deviceNameSubstring != null ? " for substring '" + deviceNameSubstring + "'" : ""));
        }
        send(tone, info, 0);
    }

    @Override
    public void sendChordToDevice(Chord chord, String deviceNameSubstring, long duration) {
        if (chord == null) return;
        try {
            MidiDevice.Info info = (deviceNameSubstring != null && !deviceNameSubstring.isEmpty())
                    ? findOutputByName(deviceNameSubstring)
                    : null;
            if (info == null) {
                info = autoSelectDefaultOutput();
            }
            if (info == null) {
                System.out.println("[MIDI] No suitable output device found" +
                        (deviceNameSubstring != null ? " for substring '" + deviceNameSubstring + "'" : ""));
                return;
            }

            MidiDevice device = MidiSystem.getMidiDevice(info);
            boolean openedHere = false;
            try {
                if (!device.isOpen()) { device.open(); openedHere = true; }
                Receiver receiver = device.getReceiver();
                try {
                    sendChordViaReceiver(receiver, chord, 0, duration);
                } finally {
                    try { receiver.close(); } catch (Exception ignored) {}
                }
            } finally {
                if (openedHere && device.isOpen()) device.close();
            }
        } catch (Exception e) {
            // Do not propagate checked exceptions: the port method does not declare throws.
            // Log to console to aid debugging in a console tool.
            System.out.println("[MIDI] Failed to send chord: " + e.getMessage());
        }
    }

    @Override
    public void playSequence(Sequence sequence, String deviceNameSubstring) throws Exception {
        if (sequence == null) throw new IllegalArgumentException("sequence must not be null");

        MidiDevice.Info target = null;
        if (deviceNameSubstring != null && !deviceNameSubstring.isBlank()) {
            target = findOutputByName(deviceNameSubstring);
            if (target == null) {
                throw new IllegalArgumentException("Unknown MIDI device: '" + deviceNameSubstring + "'");
            }
        } else {
            target = autoSelectDefaultOutput();
            if (target == null) {
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

    private void sendChordViaReceiver(Receiver receiver, Chord chord, int channel, long duration) throws InvalidMidiDataException, InterruptedException {
        if (receiver == null) throw new IllegalArgumentException("receiver must not be null");
        if (chord == null) throw new IllegalArgumentException("chord must not be null");
        if (channel < 0 || channel > 15) throw new IllegalArgumentException("channel must be between 0 and 15");

        List<Integer> notes = chord.getNotes();
        if (notes == null || notes.isEmpty()) return;

        int velocity = 32; // default soft
        long durationMs = 100; // default short duration for a chord
        long now = -1; // immediate

        // Note ON for all notes
        for (Integer n : notes) {
            if (n == null) continue;
            int pitch = n;
            if (pitch < 0) pitch = 0; if (pitch > 127) pitch = 127;
            ShortMessage on = new ShortMessage();
            on.setMessage(ShortMessage.NOTE_ON, channel, pitch, velocity);
            receiver.send(on, now);
        }

        // Hold duration
        if (durationMs > 0) Thread.sleep(durationMs);

        // Note OFF for all notes
        for (Integer n : notes) {
            if (n == null) continue;
            int pitch = n;
            if (pitch < 0) pitch = 0; if (pitch > 127) pitch = 127;
            ShortMessage off = new ShortMessage();
            off.setMessage(ShortMessage.NOTE_OFF, channel, pitch, 0);
            receiver.send(off, now);
        }
    }

    // Resolve default: env/config override → preferred brand hints → first available OUT
    private MidiDevice.Info autoSelectDefaultOutput() {
        // 1) Env/config
        try {
            String preferred = syrincs.d_frameworksAndDrivers.AppConfig.loadDefaultMidiOutputName();
            if (preferred != null && !preferred.isBlank()) {
                MidiDevice.Info byCfg = findOutputByName(preferred);
                if (byCfg != null) return byCfg;
            }
        } catch (Throwable ignored) {}
        // 2) Preferred brand hints
        String[] needles = {"Roland Digital Piano", "DP603"};
        for (String n : needles) {
            MidiDevice.Info info = findOutputByName(n);
            if (info != null) return info;
        }
        // 3) Fallback: first OUT
        MidiDevice.Info[] outs = listMidiOutputs();
        return outs.length > 0 ? outs[0] : null;
    }

    private void send(Tone tone, MidiDevice.Info info, int channel) throws MidiUnavailableException, InvalidMidiDataException, InterruptedException {
        MidiDevice device = MidiSystem.getMidiDevice(info);
        boolean openedHere = false;
        try {
            if (!device.isOpen()) { device.open(); openedHere = true; }
            Receiver receiver = device.getReceiver();
            try {
                sendViaReceiver(receiver, tone, channel);
            } finally {
                try { receiver.close(); } catch (Exception ignored) {}
            }
        } finally {
            if (openedHere && device.isOpen()) device.close();
        }
    }

    private void sendViaReceiver(Receiver receiver, Tone tone, int channel) throws InvalidMidiDataException, InterruptedException {
        if (receiver == null) throw new IllegalArgumentException("receiver must not be null");
        if (tone == null) throw new IllegalArgumentException("tone must not be null");
        if (channel < 0 || channel > 15) throw new IllegalArgumentException("channel must be between 0 and 15");

        int pitch = (int) Math.round(tone.getMidiPitch());
        if (pitch < 0) pitch = 0; if (pitch > 127) pitch = 127;
        int velocity = (int) Math.round(Math.max(0, Math.min(1, tone.getLoudness())) * 127);
        if (velocity < 1) velocity = 1;

        long now = -1; // immediate
        ShortMessage noteOn = new ShortMessage();
        noteOn.setMessage(ShortMessage.NOTE_ON, channel, pitch, velocity);
        receiver.send(noteOn, now);

        long durationMs = tone.getDurationInMilliseconds();
        if (durationMs < 0) durationMs = 0;
        Thread.sleep(durationMs);

        ShortMessage noteOff = new ShortMessage();
        noteOff.setMessage(ShortMessage.NOTE_OFF, channel, pitch, 0);
        receiver.send(noteOff, now);
    }
}
