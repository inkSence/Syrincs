package syrincs.c_adapters.midi;

public record MidiConfig(String defaultDeviceSubstring) {
    public static MidiConfig defaults(){
        return new MidiConfig("Roland Digital Piano");
    }
}
