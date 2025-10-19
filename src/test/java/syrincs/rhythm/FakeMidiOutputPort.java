package syrincs.rhythm;

import syrincs.a_domain.Tone;
import syrincs.a_domain.chord.Chord;
import syrincs.b_application.ports.MidiOutputPort;

import javax.sound.midi.MidiDevice;
import javax.sound.midi.MidiEvent;
import javax.sound.midi.Sequence;
import javax.sound.midi.ShortMessage;
import javax.sound.midi.InvalidMidiDataException;
import javax.sound.midi.MidiUnavailableException;
import java.util.ArrayList;
import java.util.List;

/**
 * Test fake for MidiOutputPort that records NOTE_ON/OFF events from a provided Sequence.
 */
public class FakeMidiOutputPort implements MidiOutputPort {

    public static class EventRec {
        public final long tick;
        public final int channel;
        public final int note;
        public final int velocity;
        public final boolean on; // true=NoteOn, false=NoteOff
        public EventRec(long tick, int channel, int note, int velocity, boolean on) {
            this.tick = tick; this.channel = channel; this.note = note; this.velocity = velocity; this.on = on;
        }
        @Override public String toString() {
            return (on?"ON":"OFF")+" t="+tick+" ch="+channel+" n="+note+" v="+velocity;
        }
    }

    private final List<EventRec> events = new ArrayList<>();

    @Override
    public MidiDevice.Info[] listMidiOutputs() {
        return new MidiDevice.Info[0];
    }

    @Override
    public MidiDevice.Info findOutputByName(String nameSubstring) {
        return null;
    }

    @Override
    public void sendToneToDevice(Tone tone, String deviceNameSubstring) throws MidiUnavailableException, InvalidMidiDataException, InterruptedException {
        // no-op for tests
    }

    @Override
    public void sendChordToDevice(Chord chord, String deviceNameSubstring, long duration) {
        // no-op for tests
    }

    @Override
    public void playSequence(Sequence sequence, String deviceNameSubstring) {
        // Device name ignored in fake
        events.clear();
        if (sequence == null) return;
        var track = sequence.getTracks()[0];
        for (int i = 0; i < track.size(); i++) {
            MidiEvent ev = track.get(i);
            if (ev.getMessage() instanceof ShortMessage sm) {
                int cmd = sm.getCommand();
                if (cmd == ShortMessage.NOTE_ON || cmd == ShortMessage.NOTE_OFF) {
                    boolean on = cmd == ShortMessage.NOTE_ON && sm.getData2() > 0;
                    events.add(new EventRec(ev.getTick(), sm.getChannel(), sm.getData1(), sm.getData2(), on));
                }
            }
        }
    }

    public List<EventRec> getEvents() { return events; }
}
