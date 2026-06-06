package syrincs.b_application.ports.dto;

/**
 * Framework-agnostic DTO representing a MIDI device endpoint.
 */
public record MidiEndpoint(
        String name,
        boolean in,
        boolean out,
        String vendor,
        String description
) {
}
