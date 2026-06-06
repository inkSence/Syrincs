package syrincs.a_domain.rhythm;

import syrincs.a_domain.statistics.StandardDeviation;

import java.util.ArrayList;
import java.util.List;

public class HuffmanRhythm extends Rhythm {


    int information;
    double standardDeviation;

    public HuffmanRhythm(int numerator, int denominator, int tempo, String onsetList) {
        super(numerator, denominator, tempo, onsetList);
        List<Integer> informationOfEachBeat = calculateInformationForEachBeat();
        this.information = informationOfEachBeat.stream().mapToInt(Integer::intValue).sum();
        this.standardDeviation = StandardDeviation.calc(informationOfEachBeat);

        System.out.println("onsetList: " + onsetList + ", informationOfEachBeat: " + informationOfEachBeat + ", standardDeviation = " + standardDeviation);

    }

    private List<Integer> calculateInformationForEachBeat() {
        List<Integer> output = new ArrayList<>();
        boolean playing = false; // fortgeführter Zustand über Beat-Grenzen
        int idx = 0;
        for (String onsets : getOnsetListPerBeat()) {
            Beat beat = new Beat(idx * positionsPerBeat, onsets, playing);
            Integer info = calculateInformation(beat);
            output.add(info);
            playing = playingAfterBeat(playing, onsets);
            idx++;
        }
        return output;
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

    // Beat als ganze Zählzeit (4 Onsets bei 4/4) mit fortgeführtem Spielzustand am Beat-Anfang
    private static final class Beat {
        final int globalIndex; // Index des ersten Onsets dieses Beats (0,4,8,...)
        final String onsets;   // genau 4 Zeichen 'x' oder 'o'
        final boolean playing; // Zustand am Beat-Beginn
        Beat(int globalIndex, String onsets, boolean playing) {
            this.globalIndex = globalIndex;
            this.onsets = onsets;
            this.playing = playing;
        }
    }

    // Leitet den Spielzustand nach dem Beat aus dem Startzustand und den 4 Onsets ab
    private boolean playingAfterBeat(boolean playingAtStart, String onsets) {
        boolean playing = playingAtStart;
        for (int pos = 0; pos < onsets.length(); pos++) {
            char c = onsets.charAt(pos);
            if (c == 'x') {
                playing = true;
            } else { // 'o'
                if (pos == 0) playing = false; // nur auf Zählzeit 0 stoppen
            }
        }
        return playing;
    }

    private int calculateInformation(Beat beat){
        FSMContext ctx = new FSMContext();
        // Startzustand anhand des Beat-Startzustands setzen
        ctx.setState(beat.playing ? PlayingAndQuarter.INSTANCE : IdleAndQuarter.INSTANCE);
        for (int pos = 0; pos < beat.onsets.length(); pos++) {
            char c = beat.onsets.charAt(pos);
            int position = pos; // 0..3 innerhalb des Beats
            ctx.state.handle(ctx, c, position);
        }
        return ctx.getCodeList().size();
    }

    private int calculateInformation(String onsetString){

        FSMContext ctx = new FSMContext();
        for (int i = 0; i < onsetString.length(); i++) {
            char c = onsetString.charAt(i);
            int position = getPositionOfBeat(i);
            ctx.state.handle(ctx, c, position);
        }
        //System.out.println("codeList: "+ ctx.codeList);
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

    public double getStandardDeviation() {
        return standardDeviation;
    }
}
