package syrincs.b_application;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Assumptions;

import syrincs.a_domain.Tone;
import syrincs.a_domain.hindemith.HindemithChord;
import syrincs.c_adapters.midi.JdkMidiOutputAdapter;

import javax.sound.midi.MidiDevice;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class SendToMidiUseCaseTest {

    private SendToMidiUseCase sendToMidiUseCase;

    @BeforeEach
    void setUp() {
        sendToMidiUseCase = new SendToMidiUseCase(new JdkMidiOutputAdapter());
    }

    @Test
    @DisplayName("Sende Akkord, falls Roland/DP603 verfügbar (sonst überspringen)")
    void sendChordIfRolandPresent() {
        String[] needles = {"Roland Digital Piano", "DP603", "Roland"};
        MidiDevice.Info target = null;
        for (String needle : needles) {
            target = sendToMidiUseCase.findOutputByName(needle);
            if (target != null) break;
        }

        Assumptions.assumeTrue(target != null,
                "[MIDI] No Roland/DP603 output found. Skipping chord send test.");

        final String deviceName = target.getName();
        System.out.println("[MIDI] Found target output: " + deviceName + " -> attempting to send a chord");

        // C major triad within safe MIDI range
        HindemithChord hindemithChord = new HindemithChord(List.of(64, 67, 72), 72, 1);

        assertDoesNotThrow(() -> sendToMidiUseCase.sendChordToDevice(hindemithChord, deviceName, 100L),
                "Sending chord to device should not throw");
    }


    @Test
    @DisplayName("Liste der MIDI-Outputs kann abgefragt werden")
    void listOutputsDoesNotThrow() {
        MidiDevice.Info[] outs = sendToMidiUseCase.listMidiOutputs();
        assertNotNull(outs, "Device list should not be null");

        for (MidiDevice.Info info : outs) {
            System.out.println("[MIDI] Output device: " + info.getName()
                    + " | " + info.getDescription()
                    + " | " + info.getVendor());
        }
    }

    @Test
    @DisplayName("Sende Note, falls Roland/DP603 verfügbar (sonst überspringen)")
    void sendNoteIfRolandPresent() {
        String[] needles = {"Roland Digital Piano", "DP603", "Roland"};
        MidiDevice.Info target = null;
        for (String needle : needles) {
            target = sendToMidiUseCase.findOutputByName(needle);
            if (target != null) break;
        }

        Assumptions.assumeTrue(target != null,
                "[MIDI] No Roland/DP603 output found. Skipping send test.");

        final String deviceName = target.getName();

        System.out.println("[MIDI] Found target output: " + deviceName + " -> attempting to send a note");

        Tone tone = new Tone(100, 60, 0.25);

        assertDoesNotThrow(() -> sendToMidiUseCase.sendToneToDevice(tone, deviceName),
                "Sending tone to device should not throw");
    }
}
