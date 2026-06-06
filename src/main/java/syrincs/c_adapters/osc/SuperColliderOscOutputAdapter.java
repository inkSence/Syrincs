package syrincs.c_adapters.osc;

import syrincs.a_domain.Tone;
import syrincs.a_domain.chord.Chord;
import syrincs.b_application.ports.MidiOutputPort;

import javax.sound.midi.InvalidMidiDataException;
import javax.sound.midi.MidiDevice;
import javax.sound.midi.MidiUnavailableException;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

public class SuperColliderOscOutputAdapter implements MidiOutputPort {
    public static final String DEFAULT_HOST = "127.0.0.1";
    public static final int DEFAULT_PORT = 57120;
    public static final String DEFAULT_PRESET = "test.sine";
    public static final double DEFAULT_PAN = 0.0;

    private final String host;
    private final int port;
    private final String defaultPreset;
    private final double defaultPan;

    public SuperColliderOscOutputAdapter() {
        this(DEFAULT_HOST, DEFAULT_PORT, DEFAULT_PRESET, DEFAULT_PAN);
    }

    public SuperColliderOscOutputAdapter(String host, int port, String defaultPreset, double defaultPan) {
        this.host = requireText(host, "host");
        if (port < 1 || port > 65_535) {
            throw new IllegalArgumentException("port must be between 1 and 65535");
        }
        this.port = port;
        this.defaultPreset = requireText(defaultPreset, "defaultPreset");
        this.defaultPan = clampFinite(defaultPan, -1.0, 1.0, "defaultPan");
    }

    @Override
    public MidiDevice.Info[] listMidiOutputs() {
        return new MidiDevice.Info[0];
    }

    @Override
    public MidiDevice.Info findOutputByName(String nameSubstring) {
        return null;
    }

    @Override
    public void sendToneToDevice(Tone tone, String deviceNameSubstring)
            throws MidiUnavailableException, InvalidMidiDataException, InterruptedException {
        if (tone == null) {
            return;
        }

        try {
            sendNote(
                    defaultPreset,
                    (int) Math.round(tone.getMidiPitch()),
                    tone.getLoudness(),
                    tone.getDurationInMilliseconds() / 1000.0,
                    defaultPan
            );
        } catch (IOException e) {
            throw new UncheckedIOException("Failed to send OSC note to SuperCollider", e);
        }
    }

    @Override
    public void sendChordToDevice(Chord chord, String deviceNameSubstring, long duration) {
        if (chord == null || chord.getNotes() == null) {
            return;
        }

        int[] midiNotes = chord.getNotes().stream()
                .filter(note -> note != null)
                .mapToInt(Integer::intValue)
                .toArray();
        if (midiNotes.length == 0) {
            return;
        }

        try {
            sendChord(defaultPreset, midiNotes, 0.7, duration / 1000.0, defaultPan);
        } catch (IOException e) {
            System.out.println("[OSC] Failed to send chord to SuperCollider: " + e.getMessage());
        }
    }

    public void sendNote(String preset, int midiNote, double velocity, double durationSeconds, double pan) throws IOException {
        byte[] packet = OscMessageBuilder.build(
                "/note",
                requireText(preset, "preset"),
                clampMidiNote(midiNote),
                (float) clampFinite(velocity, 0.0, 1.0, "velocity"),
                (float) Math.max(0.01, requireFinite(durationSeconds, "durationSeconds")),
                (float) clampFinite(pan, -1.0, 1.0, "pan")
        );
        send(packet);
    }

    public void sendChord(String preset, int[] midiNotes, double velocity, double durationSeconds, double pan) throws IOException {
        if (midiNotes == null || midiNotes.length == 0) {
            throw new IllegalArgumentException("midiNotes must contain at least one note");
        }

        Object[] arguments = new Object[1 + midiNotes.length + 3];
        arguments[0] = requireText(preset, "preset");
        for (int i = 0; i < midiNotes.length; i++) {
            arguments[i + 1] = clampMidiNote(midiNotes[i]);
        }
        arguments[arguments.length - 3] = (float) clampFinite(velocity, 0.0, 1.0, "velocity");
        arguments[arguments.length - 2] = (float) Math.max(0.01, requireFinite(durationSeconds, "durationSeconds"));
        arguments[arguments.length - 1] = (float) clampFinite(pan, -1.0, 1.0, "pan");

        send(OscMessageBuilder.build("/chord", arguments));
    }

    public void sendDrum(String preset, double velocity, double pan) throws IOException {
        byte[] packet = OscMessageBuilder.build(
                "/drum",
                requireText(preset, "preset"),
                (float) clampFinite(velocity, 0.0, 1.0, "velocity"),
                (float) clampFinite(pan, -1.0, 1.0, "pan")
        );
        send(packet);
    }

    public void sendFx(String effectName, boolean enabled, String paramName, double value) throws IOException {
        byte[] packet = OscMessageBuilder.build(
                "/fx",
                requireText(effectName, "effectName"),
                enabled ? 1 : 0,
                requireText(paramName, "paramName"),
                (float) requireFinite(value, "value")
        );
        send(packet);
    }

    public void sendSet(String target, String paramName, double value) throws IOException {
        byte[] packet = OscMessageBuilder.build(
                "/set",
                requireText(target, "target"),
                requireText(paramName, "paramName"),
                (float) requireFinite(value, "value")
        );
        send(packet);
    }

    public void sendRamp(String target, String paramName, double value, double seconds) throws IOException {
        byte[] packet = OscMessageBuilder.build(
                "/ramp",
                requireText(target, "target"),
                requireText(paramName, "paramName"),
                (float) requireFinite(value, "value"),
                (float) Math.max(0.0, requireFinite(seconds, "seconds"))
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
