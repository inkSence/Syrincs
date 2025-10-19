package syrincs.c_adapters.midi;

import syrincs.a_domain.rhythm.Pattern;
import syrincs.a_domain.rhythm.RhythmSpec;
import syrincs.a_domain.rhythm.VoiceSpec;

import javax.sound.midi.InvalidMidiDataException;
import javax.sound.midi.MetaMessage;
import javax.sound.midi.MidiEvent;
import javax.sound.midi.Sequence;
import javax.sound.midi.ShortMessage;
import java.util.List;
import java.util.Map;

public class SequenceBuilder {

    public Sequence build(Pattern pattern, RhythmSpec spec, List<VoiceSpec> voices) throws InvalidMidiDataException {
        int ppq = spec.ppq;
        Sequence seq = new Sequence(Sequence.PPQ, ppq);
        var track = seq.createTrack();

        // Tempo meta (microseconds per quarter note)
        int tempo = spec.tempoBpm;
        int mpqn = Math.round(60_000_000f / Math.max(1, tempo));
        byte[] data = new byte[]{
                (byte) ((mpqn >> 16) & 0xFF),
                (byte) ((mpqn >> 8) & 0xFF),
                (byte) (mpqn & 0xFF)};
        MetaMessage tempoMsg = new MetaMessage();
        tempoMsg.setMessage(0x51, data, data.length);
        track.add(new MidiEvent(tempoMsg, 0));

        int stepTicks = spec.stepTicks();
        int gateTicksKick = Math.round(stepTicks * (voices.get(0).gatePercent / 100f));
        int gateTicksSnare = Math.round(stepTicks * (voices.get(1).gatePercent / 100f));
        if (gateTicksKick < 1) gateTicksKick = 1;
        if (gateTicksSnare < 1) gateTicksSnare = 1;

        Map<String, boolean[]> vmap = pattern.voices();
        for (VoiceSpec v : voices) {
            boolean[] steps = vmap.get(v.name);
            if (steps == null) continue;
            for (int idx = 0; idx < steps.length; idx++) {
                if (!steps[idx]) continue;
                long startTick = (long) idx * stepTicks;
                int gate = v.name.equalsIgnoreCase("kick") ? gateTicksKick : gateTicksSnare;
                long endTick = startTick + gate;
                // Note On
                ShortMessage on = new ShortMessage();
                on.setMessage(ShortMessage.NOTE_ON, v.channel, v.note, v.velocity);
                track.add(new MidiEvent(on, startTick));
                // Note Off
                ShortMessage off = new ShortMessage();
                off.setMessage(ShortMessage.NOTE_OFF, v.channel, v.note, 0);
                track.add(new MidiEvent(off, endTick));
            }
        }
        return seq;
    }
}
