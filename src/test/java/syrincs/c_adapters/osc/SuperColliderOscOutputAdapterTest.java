package syrincs.c_adapters.osc;

import org.junit.jupiter.api.Test;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.assertTrue;

class SuperColliderOscOutputAdapterTest {

    @Test
    void sendsNoteAsOscUdpPacket() throws Exception {
        InetAddress loopback = InetAddress.getLoopbackAddress();

        try (DatagramSocket receiver = new DatagramSocket(0, loopback)) {
            receiver.setSoTimeout(1_000);
            int port = receiver.getLocalPort();
            SuperColliderOscOutputAdapter adapter =
                    new SuperColliderOscOutputAdapter("127.0.0.1", port, "basic.sine", 0.0);

            adapter.sendNote("basic.sine", 60, 0.7, 0.5, 0.0);

            byte[] buffer = new byte[512];
            DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
            receiver.receive(packet);

            String text = new String(packet.getData(), 0, packet.getLength(), StandardCharsets.UTF_8);
            assertTrue(text.contains("/note"));
            assertTrue(text.contains(",sifff"));
            assertTrue(text.contains("basic.sine"));
        }
    }
}
