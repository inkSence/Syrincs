package syrincs.c_adapters.midi;

import syrincs.a_domain.rhythm.Pattern;
import syrincs.a_domain.rhythm.RhythmSpec;
import syrincs.a_domain.rhythm.VoiceSpec;
import syrincs.b_application.ports.RhythmPlaybackPort;

import javax.sound.midi.MidiDevice;
import javax.sound.midi.MidiSystem;
import javax.sound.midi.Sequence;
import java.util.List;
import java.util.Objects;

/**
 * Adapter implementation that converts domain rhythm objects to a javax.sound.midi.Sequence
 * and delegates playback to a SequencePlayer (adapter-layer), keeping javax types outside the application boundary.
 */
public class RhythmPlaybackService implements RhythmPlaybackPort {
    private final SequenceBuilder sequenceBuilder;
    private final SequencePlayer sequencePlayer;

    public RhythmPlaybackService(SequenceBuilder sequenceBuilder, SequencePlayer sequencePlayer) {
        this.sequenceBuilder = Objects.requireNonNull(sequenceBuilder, "sequenceBuilder");
        this.sequencePlayer = Objects.requireNonNull(sequencePlayer, "sequencePlayer");
    }

    @Override
    public void play(Pattern pattern, RhythmSpec spec, List<VoiceSpec> voices) throws Exception {
        play(pattern, spec, voices, null);
    }

    @Override
    public void play(Pattern pattern, RhythmSpec spec, List<VoiceSpec> voices, String deviceNameSubstring) throws Exception {
        Sequence seq = sequenceBuilder.build(pattern, spec, voices);
        MidiDevice.Info info = DeviceService.resolveOutput(deviceNameSubstring);
        if (info == null) {
            throw new IllegalStateException(missingDeviceMessage(deviceNameSubstring));
        }
        MidiDevice device = MidiSystem.getMidiDevice(info);
        sequencePlayer.play(seq, device);
    }

    private String missingDeviceMessage(String deviceNameSubstring) {
        if (deviceNameSubstring != null && !deviceNameSubstring.isBlank()) {
            return "No MIDI output device matching '" + deviceNameSubstring + "' found.";
        }
        return "No suitable MIDI output device found. Set env SYRINCS_MIDI_DEVICE or pass --device.";
    }
}
