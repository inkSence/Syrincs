package syrincs.a_domain.rhythm;

public class VoiceSpec {
    public final String name;
    public final int channel; // 0-15 (10 means channel 11 in MIDI spec; here we use 10)
    public final int note;    // 0-127
    public final int velocity; // 0-127
    public final int gatePercent; // 0-100

    public VoiceSpec(String name, int channel, int note, int velocity, int gatePercent) {
        this.name = name;
        this.channel = channel;
        this.note = note;
        this.velocity = velocity;
        this.gatePercent = gatePercent;
    }

    public VoiceSpec withGate(int newGate) { return new VoiceSpec(name, channel, note, velocity, newGate); }

    public VoiceSpec withOverrides(Integer channel, Integer note, Integer velocity, Integer gate) {
        return new VoiceSpec(name,
                channel != null ? channel : this.channel,
                note != null ? note : this.note,
                velocity != null ? velocity : this.velocity,
                gate != null ? gate : this.gatePercent);
    }
}
