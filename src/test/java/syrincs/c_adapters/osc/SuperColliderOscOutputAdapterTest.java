package syrincs.c_adapters.osc;

import org.junit.jupiter.api.Test;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assumptions.abort;

class SuperColliderOscOutputAdapterTest {

    @Test
    void sendsNoteAsOscUdpPacket() throws Exception {
        assertOscPacket(
                adapter -> adapter.sendNote("test.sine", 60, 0.7, 0.5, 0.0),
                "/note",
                ",sifff",
                "test.sine"
        );
    }

    @Test
    void sendsChordAsOscUdpPacket() throws Exception {
        assertOscPacket(
                adapter -> adapter.sendChord("organ.full", new int[]{60, 64, 67}, 0.6, 1.0, 0.0),
                "/chord",
                ",siiifff",
                "organ.full"
        );
    }

    @Test
    void sendsDrumAsOscUdpPacket() throws Exception {
        assertOscPacket(
                adapter -> adapter.sendDrum("drum.kick", 0.9, 0.0),
                "/drum",
                ",sff",
                "drum.kick"
        );
    }

    @Test
    void sendsClapDrumPresetAsOscUdpPacket() throws Exception {
        assertOscPacket(
                adapter -> adapter.sendDrum("drum.clap", 0.7, 0.0),
                "/drum",
                ",sff",
                "drum.clap"
        );
    }

    @Test
    void sendsSampleDrumPresetAsOscUdpPacket() throws Exception {
        assertOscPacket(
                adapter -> adapter.sendDrum("drum.kick.sample", 0.9, 0.0),
                "/drum",
                ",sff",
                "drum.kick.sample"
        );
    }

    @Test
    void sendsSampleTonalPresetAsOscUdpPacket() throws Exception {
        assertOscPacket(
                adapter -> adapter.sendNote("keys.piano.sample", 60, 0.7, 1.0, 0.0),
                "/note",
                ",sifff",
                "keys.piano.sample"
        );
    }

    @Test
    void sendsFxAsOscUdpPacket() throws Exception {
        assertOscPacket(
                adapter -> adapter.sendFx("reverb", true, "mix", 0.25),
                "/fx",
                ",sisf",
                "reverb",
                "mix"
        );
    }

    @Test
    void sendsSetAsOscUdpPacket() throws Exception {
        assertOscPacket(
                adapter -> adapter.sendSet("master", "volume", 0.8),
                "/set",
                ",ssf",
                "master",
                "volume"
        );
    }

    @Test
    void sendsRampAsOscUdpPacket() throws Exception {
        assertOscPacket(
                adapter -> adapter.sendRamp("reverb", "mix", 0.4, 2.0),
                "/ramp",
                ",ssff",
                "reverb",
                "mix"
        );
    }

    @Test
    void sendsSceneAsOscUdpPacket() throws Exception {
        assertOscPacket(
                adapter -> adapter.sendScene("scene.chorale"),
                "/scene",
                ",s",
                "scene.chorale"
        );
    }

    @Test
    void sendsRoleAsOscUdpPacket() throws Exception {
        assertOscPacket(
                adapter -> adapter.sendRole("harmony", "pad.warm"),
                "/role",
                ",ss",
                "harmony",
                "pad.warm"
        );
    }

    private static void assertOscPacket(OscSend send, String... expectedText) throws Exception {
        InetAddress loopback = InetAddress.getLoopbackAddress();

        try (DatagramSocket receiver = new DatagramSocket(0, loopback)) {
            receiver.setSoTimeout(1_000);
            int port = receiver.getLocalPort();
            SuperColliderOscOutputAdapter adapter =
                    new SuperColliderOscOutputAdapter("127.0.0.1", port, "test.sine", 0.0);
            send.send(adapter);

            byte[] buffer = new byte[512];
            DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
            receiver.receive(packet);

            String text = new String(packet.getData(), 0, packet.getLength(), StandardCharsets.UTF_8);
            for (String expected : expectedText) {
                assertTrue(text.contains(expected), () -> "OSC packet should contain " + expected);
            }
        } catch (SocketException e) {
            abort("UDP loopback is not available: " + e.getMessage());
        }
    }

    @FunctionalInterface
    private interface OscSend {
        void send(SuperColliderOscOutputAdapter adapter) throws Exception;
    }
}
