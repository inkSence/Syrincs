package syrincs.b_application.ports;

import syrincs.b_application.ports.dto.MidiEndpoint;

import java.util.List;

/**
 * Application-side port for querying MIDI devices without exposing framework types.
 */
public interface MidiDeviceQueryPort {
    List<MidiEndpoint> listOutputs();
    MidiEndpoint findOutput(String nameSubstring);
}
