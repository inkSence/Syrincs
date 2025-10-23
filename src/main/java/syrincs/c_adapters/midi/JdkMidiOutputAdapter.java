package syrincs.c_adapters.midi;

import syrincs.a_domain.chord.Chord;
import syrincs.a_domain.Tone;
import syrincs.b_application.errors.MidiPortException;
import syrincs.b_application.ports.MidiOutputPort;

import javax.sound.midi.*;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Frameworks & Drivers implementation of MidiOutputPort using the JDK javax.sound.midi API.
 */
public class JdkMidiOutputAdapter implements MidiOutputPort, syrincs.b_application.ports.MidiDeviceQueryPort  {

    private static final Logger LOG = Logger.getLogger(JdkMidiOutputAdapter.class.getName());

    private MidiDevice.Info findOutputInfoBySubstring(String nameSubstring) {
        return DeviceService.findOutputInfoBySubstring(nameSubstring);
    }

    @Override
    public void sendToneToDevice(Tone tone, String deviceNameSubstring) {
        try {
            MidiDevice.Info info = (deviceNameSubstring != null && !deviceNameSubstring.isEmpty())
                    ? findOutputInfoBySubstring(deviceNameSubstring)
                    : null;
            if (info == null) {
                info = DeviceService.autoSelectDefaultOutput();
            }
            if (info == null) {
                String msg = "No suitable MIDI output device found" +
                        (deviceNameSubstring != null ? " for substring '" + deviceNameSubstring + "'" : "");
                LOG.warning(msg);
                throw new MidiPortException(msg);
            }
            send(tone, info, 0);
        } catch (MidiUnavailableException | InvalidMidiDataException | InterruptedException e) {
            LOG.log(Level.WARNING, "Failed to send tone: " + e.getMessage(), e);
            throw new MidiPortException("Failed to send tone: " + e.getMessage(), e);
        }
    }

    @Override
    public void sendChordToDevice(Chord chord, long duration) throws MidiPortException {
        sendChordToDevice(chord, duration, 0);
    }

    @Override
    public void sendChordToDevice(Chord chord, long duration, int channelZeroBased) throws MidiPortException {
        if (chord == null) return;
        try {

            MidiDevice device = DeviceService.getMidiDevice();
            boolean openedHere = false;
            try {
                if (!device.isOpen()) { device.open(); openedHere = true; }
                Receiver receiver = device.getReceiver();
                try {
                    sendChordViaReceiver(receiver, chord, channelZeroBased, duration);
                } finally {
                    try { receiver.close(); } catch (Exception ignored) {}
                }
            } finally {
                if (openedHere && device.isOpen()) device.close();
            }
        } catch (MidiUnavailableException | InvalidMidiDataException e) {
            LOG.log(Level.WARNING, "Failed to send chord: " + e.getMessage(), e);
            throw new MidiPortException("Failed to send chord: " + e.getMessage(), e);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            LOG.log(Level.WARNING, "Sending chord interrupted", e);
            throw new MidiPortException("Sending chord interrupted", e);
        }
    }


    private void sendChordViaReceiver(Receiver receiver, Chord chord, int channel, long duration) throws InvalidMidiDataException, InterruptedException {
        if (receiver == null) throw new IllegalArgumentException("receiver must not be null");
        if (chord == null) throw new IllegalArgumentException("chord must not be null");
        if (channel < 0 || channel > 15) throw new IllegalArgumentException("channel must be between 0 and 15");

        List<Integer> notes = chord.getNotes();
        if (notes == null || notes.isEmpty()) return;

        int velocity = 32; // default soft
        long durationMs = (duration > 0 ? duration : 200); // use provided duration or default 200 ms
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

    // MidiDeviceQueryPort implementation (framework-agnostic DTOs)
    @Override
    public java.util.List<syrincs.b_application.ports.dto.MidiEndpoint> listOutputs() {
        MidiDevice.Info[] all = MidiSystem.getMidiDeviceInfo();
        java.util.List<syrincs.b_application.ports.dto.MidiEndpoint> outs = new java.util.ArrayList<>();
        for (MidiDevice.Info info : all) {
            try {
                MidiDevice dev = MidiSystem.getMidiDevice(info);
                boolean out = dev.getMaxReceivers() != 0;      // -1 unlimited or >0
                boolean in  = dev.getMaxTransmitters() != 0;   // -1 unlimited or >0
                if (out) {
                    outs.add(new syrincs.b_application.ports.dto.MidiEndpoint(
                            info.getName(), in, true, info.getVendor(), info.getDescription()
                    ));
                }
            } catch (MidiUnavailableException ignored) {
            }
        }
        return outs;
    }

    @Override
    public syrincs.b_application.ports.dto.MidiEndpoint findOutput(String nameSubstring) {
        if (nameSubstring == null || nameSubstring.isBlank()) return null;
        String needle = nameSubstring.toLowerCase();
        for (var ep : listOutputs()) {
            String hay = (ep.name() + " " + ep.description() + " " + ep.vendor()).toLowerCase();
            if (hay.contains(needle)) return ep;
        }
        return null;
    }
}
