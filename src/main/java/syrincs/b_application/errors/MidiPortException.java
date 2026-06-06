package syrincs.b_application.errors;

/**
 * Runtime exception representing failures in the MIDI output port.
 * This keeps the application boundary free from javax.sound.midi exception types.
 */
public class MidiPortException extends RuntimeException {
    public MidiPortException(String message) { super(message); }
    public MidiPortException(String message, Throwable cause) { super(message, cause); }
}
