package syrincs.a_domain.rhythm;

import java.util.ArrayList;
import java.util.List;

public class HufmanRhythm extends Rhythm {

    int information;

    public HufmanRhythm(int numerator, int denominator, int tempo, String onsetList) {
        super(numerator, denominator, tempo, onsetList);
    }

    // --- State Pattern implementation (Idle/Playing × NoteValue) ---
    private interface RhythmState {
        void handle(FSMContext ctx, char c, int position);
    }

    private static final class FSMContext {
        // Wörterbuch = { 00 Trennung erniedrigen, 01 Trennung erhöhen, 10 Pausen spielen, 11 Noten spielen }
        void joinOnce() {codeList.add("00"); }
        void splitOnce() { codeList.add("01"); }
        void emitRestOn() { codeList.add("10"); }
        void emitNoteOn() { codeList.add("11"); }
        void setState(RhythmState s) {
            this.state = s;
            this.playingNote =
                    s == PlayingAndQuarter.INSTANCE
                    || s == PlayingAndEighth.INSTANCE
                    || s == PlayingAndSixteenth.INSTANCE;
        }
        List<String> getCodeList() { return codeList; }
        boolean isPlayingNote() { return playingNote; }

        private RhythmState state = IdleAndQuarter.INSTANCE;
        private final List<String> codeList = new ArrayList<>();
        private int noteValue = 4; // 4,2,1
        private boolean playingNote = false;


    }

    int calculateInformation(String onsetString){

        FSMContext ctx = new FSMContext();
        for (int i = 0; i < onsetList.length(); i++) { // bewusst onsetList.length() wie zuvor
            char c = onsetString.charAt(i);
            int position = getPositionOfBeat(i);
            ctx.state.handle(ctx, c, position);
        }
        return ctx.getCodeList().size();
    }

    private enum IdleAndQuarter implements RhythmState {
        INSTANCE; // Diese enums haben jeweils nur ein Feld.
        @Override public void handle(FSMContext ctx, char c, int position) {
            if (position == 0) {
                ctx.noteValue = 4;
                if (c == 'x') { ctx.emitNoteOn(); ctx.setState(PlayingAndQuarter.INSTANCE); }
                return;
            }  // videt

            if (c == 'x') {
                ctx.noteValue = 2;
                ctx.splitOnce();
                ctx.setState(PlayingAndEighth.INSTANCE);
            } // videt
        }
    }

    private enum PlayingAndQuarter implements RhythmState {
        INSTANCE;
        @Override public void handle(FSMContext ctx, char c, int position) {
            if (position == 0) {
                ctx.noteValue = 4;
                if (c == 'o') { ctx.emitRestOn(); ctx.setState(IdleAndQuarter.INSTANCE); }
                return;
            } // videt
            else if(position == 1 || position == 3 && c == 'x'){
                ctx.noteValue = 1;
                ctx.splitOnce();
                ctx.splitOnce();
                ctx.setState(PlayingAndSixteenth.INSTANCE);
            }
            // innerhalb der Gruppe: Split von 4→2 bei 'x'
            if (ctx.noteValue == 4 && c == 'x') {
                ctx.noteValue = 2;
                ctx.splitOnce();
                ctx.setState(PlayingAndEighth.INSTANCE);
                return;
            }
        }
    }

    private enum IdleAndEighth implements RhythmState {
        INSTANCE;
        @Override public void handle(FSMContext ctx, char c, int position) {
            if (position == 0) {
                ctx.noteValue = 4;
                if (c == 'x') { ctx.emitNoteOn(); ctx.setState(PlayingAndQuarter.INSTANCE); }
                else { ctx.setState(IdleAndQuarter.INSTANCE); }
                return;
            }
            // kein 2→1 Split in der aktuellen Logik
        }
    }

    private enum PlayingAndEighth implements RhythmState {
        INSTANCE;
        @Override public void handle(FSMContext ctx, char c, int position) {
            if (position == 0) {
                ctx.noteValue = 4;
                if (c == 'o') { ctx.emitRestOn(); ctx.setState(IdleAndQuarter.INSTANCE); }
                else { ctx.setState(PlayingAndQuarter.INSTANCE); }
                return;
            }
            // kein 2→1 Split in der aktuellen Logik
        }
    }

    private enum IdleAndSixteenth implements RhythmState {
        INSTANCE;
        @Override public void handle(FSMContext ctx, char c, int position) {
            if (position == 0) {
                ctx.noteValue = 4;
                if (c == 'x') { ctx.emitNoteOn(); ctx.setState(PlayingAndQuarter.INSTANCE); }
                else { ctx.setState(IdleAndQuarter.INSTANCE); }
                return;
            }
        }
    }

    private enum PlayingAndSixteenth implements RhythmState {
        INSTANCE;
        @Override public void handle(FSMContext ctx, char c, int position) {
            if (position == 0) {
                ctx.noteValue = 4;
                if (c == 'o') { ctx.emitRestOn(); ctx.setState(IdleAndQuarter.INSTANCE); }
                else { ctx.setState(PlayingAndQuarter.INSTANCE); }
                return;
            }
            if (c == 'o') {
                if (position == 2) {
                    ctx.emitRestOn();
                    ctx.noteValue = 2;
                    ctx.setState(IdleAndEighth.INSTANCE);
                } else if (position == 3) {
                    ctx.emitRestOn();
                    ctx.setState(IdleAndQuarter.INSTANCE);
                } //videt
            }
        }
    }



    public static void main(String[] args){
        var rhythm = new HufmanRhythm(4,4,90, "xooo oooo oooo oooo");
        int info = rhythm.calculateInformation(rhythm.onsetList);
        System.out.println(info);

    }
}
