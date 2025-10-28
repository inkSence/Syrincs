package syrincs.a_domain.rhythm;

import java.util.ArrayList;
import java.util.List;

public class HuffmanRhythm extends Rhythm {

    int information;

    public HuffmanRhythm(int numerator, int denominator, int tempo, String onsetList) {
        super(numerator, denominator, tempo, onsetList);
        this.information = calculateInformation(this.onsetList);
    }

    // --- State Pattern implementation (Idle/Playing × NoteValue) ---
    private interface RhythmState {
        void handle(FSMContext ctx, char c, int position);
    }

    private static final class FSMContext {
        // Quellalphabet = { 00 Trennung erniedrigen, 01 Trennung erhöhen, 10 Pausen spielen, 11 Noten spielen }
        void joinOnce() {codeList.add("00"); }
        void splitOnce() { codeList.add("01"); }
        void emitRestOn() { codeList.add("10"); }
        void emitNoteOn() { codeList.add("11"); }
        void setState(RhythmState s) {
            this.state = s;
        }
        List<String> getCodeList() { return codeList; }

        private RhythmState state = IdleAndQuarter.INSTANCE;
        private final List<String> codeList = new ArrayList<>();
    }

    int calculateInformation(String onsetString){

        FSMContext ctx = new FSMContext();
        for (int i = 0; i < onsetList.length(); i++) { // bewusst onsetList.length() wie zuvor
            char c = onsetString.charAt(i);
            int position = getPositionOfBeat(i);
            ctx.state.handle(ctx, c, position);
        }
        System.out.println("codeList: "+ ctx.codeList);
        return ctx.getCodeList().size();
    }

    private enum IdleAndQuarter implements RhythmState {
        INSTANCE; // Diese enums haben jeweils nur ein Feld.
        @Override public void handle(FSMContext ctx, char c, int position) {
            if (c == 'x') {
                ctx.emitNoteOn();
                if (position == 0) {
                    ctx.setState(PlayingAndQuarter.INSTANCE);
                }
                else handlePositionsOneToThreeAndPlayingCharacter(ctx, position);
            }
        }
    }

    private enum PlayingAndQuarter implements RhythmState {
        INSTANCE;
        @Override public void handle(FSMContext ctx, char c, int position) {
            if (c == 'o') {
                if (position == 0) {
                    ctx.emitRestOn();
                    ctx.setState(IdleAndQuarter.INSTANCE);
                }
            } else {
                handlePositionsOneToThreeAndPlayingCharacter(ctx, position);
            }
        }


    }

    private enum IdleAndEighth implements RhythmState {
        INSTANCE;
        @Override public void handle(FSMContext ctx, char c, int position) {
            // Nicht-Spielen und Achtel kann nicht erreicht werden.
        }
    }

    private enum PlayingAndEighth implements RhythmState {
        INSTANCE;
        @Override public void handle(FSMContext ctx, char c, int position) {
            if (c == 'o') {
                if (position == 0) {
                    ctx.emitRestOn();
                    ctx.setState(IdleAndQuarter.INSTANCE);
                }
            } else {
                if (position == 0) {
                    ctx.setState(PlayingAndQuarter.INSTANCE);
                } else if (position == 3){
                    ctx.splitOnce();
                    ctx.setState(PlayingAndSixteenth.INSTANCE);
                }
            }
        }
    }

    private enum IdleAndSixteenth implements RhythmState {
        INSTANCE;
        @Override public void handle(FSMContext ctx, char c, int position) {
            if (c == 'x') {
                ctx.emitNoteOn();
                if (position == 0) {
                    ctx.setState(PlayingAndQuarter.INSTANCE);
                } else {
                    ctx.setState(PlayingAndSixteenth.INSTANCE);
                }
            } else {
                if (position == 0) {
                    ctx.setState(IdleAndQuarter.INSTANCE);
                }
            }

        }
    }

    private enum PlayingAndSixteenth implements RhythmState {
        INSTANCE;
        @Override public void handle(FSMContext ctx, char c, int position) {
            if (c == 'o') {
                ctx.emitRestOn();
                if (position == 0) {
                    ctx.setState(IdleAndQuarter.INSTANCE);
                } else if (position == 2) {
                    ctx.setState(IdleAndSixteenth.INSTANCE);
                } else if (position == 3) {
                    ctx.setState(IdleAndSixteenth.INSTANCE);
                }
            } else {
                if (position == 0) {
                    ctx.setState(PlayingAndQuarter.INSTANCE);
                }
            }
        }
    }

    private static void handlePositionsOneToThreeAndPlayingCharacter(FSMContext ctx, int position) {
        if (position == 1 || position == 3) {
            ctx.splitOnce();
            ctx.splitOnce();
            ctx.setState(PlayingAndSixteenth.INSTANCE);
        } else if (position == 2) {
            ctx.splitOnce();
            ctx.setState(PlayingAndEighth.INSTANCE);
        }
    }

    public int getInformation() {
        return information;
    }
}
