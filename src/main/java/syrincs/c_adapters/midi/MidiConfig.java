package syrincs.c_adapters.midi;

//public record MidiConfig(String defaultDeviceSubstring) {
//    public static MidiConfig defaults(){
//        return new MidiConfig("Roland Digital Piano");
//    }
//}
public record MidiConfig(
        String defaultDeviceSubstring,
        int ppq,
        int rhythmChannel,
        int snareNote,
        int basedrumNote
) {
    public static MidiConfig defaults(){
        return new MidiConfig(
                "Roland Digital Piano",
                480,
                9,
                38,
                36
        );
    }
}
