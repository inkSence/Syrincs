package syrincs.c_adapters.osc;

import syrincs.a_domain.Tone;
import syrincs.a_domain.chord.Chord;
import syrincs.b_application.errors.MidiPortException;
import syrincs.b_application.ports.MidiOutputPort;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

public class SuperColliderOscOutputAdapter implements MidiOutputPort {
    public static final String DEFAULT_HOST = "127.0.0.1";
    public static final int DEFAULT_PORT = 57120;
    public static final String DEFAULT_SYNTH = "basic.sine";
    public static final double DEFAULT_PAN = 0.0;

    private final String host;
    private final int port;
    private final String defaultSynth;
    private final double defaultPan;

    public SuperColliderOscOutputAdapter() {
        this(DEFAULT_HOST, DEFAULT_PORT, DEFAULT_SYNTH, DEFAULT_PAN);
    }

    public SuperColliderOscOutputAdapter(String host, int port, String defaultSynth, double defaultPan) {
        this.host = requireText(host, "host");
        if (port < 1 || port > 65_535) {
            throw new IllegalArgumentException("port must be between 1 and 65535");
        }
        this.port = port;
        this.defaultSynth = requireText(defaultSynth, "defaultSynth");
        this.defaultPan = clampFinite(defaultPan, -1.0, 1.0, "defaultPan");
    }

    @Override
    public void sendToneToDevice(Tone tone, String deviceNameSubstring) throws MidiPortException {
        if (tone == null) {
            return;
        }

        try {
            sendNote(
                    defaultSynth,
                    (int) Math.round(tone.getMidiPitch()),
                    tone.getLoudness(),
                    tone.getDurationInMilliseconds() / 1000.0,
                    defaultPan
            );
        } catch (IOException e) {
            throw new MidiPortException("Failed to send OSC note to SuperCollider", e);
        }
    }

    @Override
    public void sendChordToDevice(Chord chord, long duration) throws MidiPortException {
        sendChordToDevice(chord, duration, 0);
    }

    @Override
    public void sendChordToDevice(Chord chord, long duration, int channelZeroBased) throws MidiPortException {
        if (chord == null || chord.getNotes() == null) {
            return;
        }

        for (Integer note : chord.getNotes()) {
            if (note == null) {
                continue;
            }
            try {
                sendNote(defaultSynth, note, 0.7, duration / 1000.0, defaultPan);
            } catch (IOException e) {
                throw new MidiPortException("Failed to send OSC chord note to SuperCollider", e);
            }
        }
    }

    public void sendNote(String synth, int midiNote, double velocity, double durationSeconds, double pan) throws IOException {
        byte[] packet = OscMessageBuilder.build(
                "/note",
                requireText(synth, "synth"),
                clampMidiNote(midiNote),
                (float) clampFinite(velocity, 0.0, 1.0, "velocity"),
                (float) Math.max(0.01, requireFinite(durationSeconds, "durationSeconds")),
                (float) clampFinite(pan, -1.0, 1.0, "pan")
        );
        send(packet);
    }

    private void send(byte[] packet) throws IOException {
        InetAddress address = InetAddress.getByName(host);
        DatagramPacket datagram = new DatagramPacket(packet, packet.length, address, port);
        try (DatagramSocket socket = new DatagramSocket()) {
            socket.send(datagram);
        }
    }

    private static int clampMidiNote(int midiNote) {
        if (midiNote < 0) {
            return 0;
        }
        if (midiNote > 127) {
            return 127;
        }
        return midiNote;
    }

    private static double clampFinite(double value, double min, double max, String name) {
        double finite = requireFinite(value, name);
        if (finite < min) {
            return min;
        }
        if (finite > max) {
            return max;
        }
        return finite;
    }

    private static double requireFinite(double value, String name) {
        if (!Double.isFinite(value)) {
            throw new IllegalArgumentException(name + " must be finite");
        }
        return value;
    }

    private static String requireText(String value, String name) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(name + " must not be blank");
        }
        return value;
    }
}
