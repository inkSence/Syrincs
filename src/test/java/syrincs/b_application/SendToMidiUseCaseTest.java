package syrincs.b_application;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Assumptions;

import syrincs.a_domain.Tone;
import syrincs.a_domain.hindemith.HindemithChord;
import syrincs.c_adapters.midi.JdkMidiOutputAdapter;
import syrincs.b_application.ports.MidiDeviceQueryPort;
import syrincs.b_application.ports.dto.MidiEndpoint;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class SendToMidiUseCaseTest {

    private SendToMidiUseCase sendToMidiUseCase;
    private MidiDeviceQueryPort queryPort;

    @BeforeEach
    void setUp() {
        var adapter = new JdkMidiOutputAdapter();
        sendToMidiUseCase = new SendToMidiUseCase(adapter);
        queryPort = adapter; // adapter implements MidiDeviceQueryPort
    }

    @Test
    @DisplayName("Sende Akkord, falls Roland/DP603 verfügbar (sonst überspringen)")
    void sendChordIfRolandPresent() {
        String[] needles = {"Roland Digital Piano", "DP603", "Roland"};
        MidiEndpoint target = null;
        for (String needle : needles) {
            target = queryPort.findOutput(needle);
            if (target != null) break;
        }

        Assumptions.assumeTrue(target != null,
                "[MIDI] No Roland/DP603 output found. Skipping chord send test.");

        final String deviceName = target.name();
        System.out.println("[MIDI] Found target output: " + deviceName + " -> attempting to send a chord");

        // C major triad within safe MIDI range
        HindemithChord hindemithChord = new HindemithChord(List.of(64, 67, 72), 72, 1);

        assertDoesNotThrow(() -> sendToMidiUseCase.sendChordToDevice(hindemithChord, deviceName, 100L),
                "Sending chord to device should not throw");
    }


    @Test
    @DisplayName("Liste der MIDI-Outputs kann abgefragt werden")
    void listOutputsDoesNotThrow() {
        var outs = queryPort.listOutputs();
        assertNotNull(outs, "Device list should not be null");

        for (var ep : outs) {
            System.out.println("[MIDI] Output device: " + ep.name()
                    + " | in=" + ep.in()
                    + " | out=" + ep.out());
        }
    }

    @Test
    @DisplayName("Sende Note, falls Roland/DP603 verfügbar (sonst überspringen)")
    void sendNoteIfRolandPresent() {
        String[] needles = {"Roland Digital Piano", "DP603", "Roland"};
        MidiEndpoint target = null;
        for (String needle : needles) {
            target = queryPort.findOutput(needle);
            if (target != null) break;
        }

        Assumptions.assumeTrue(target != null,
                "[MIDI] No Roland/DP603 output found. Skipping send test.");

        final String deviceName = target.name();

        System.out.println("[MIDI] Found target output: " + deviceName + " -> attempting to send a note");

        Tone tone = new Tone(100, 60, 0.25);

        assertDoesNotThrow(() -> sendToMidiUseCase.sendToneToDevice(tone, deviceName),
                "Sending tone to device should not throw");
    }
}
