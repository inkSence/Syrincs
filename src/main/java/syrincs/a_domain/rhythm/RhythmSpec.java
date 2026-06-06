package syrincs.a_domain.rhythm;

public class RhythmSpec {
    public final int meterNumerator;  // beats per bar
    public final int meterDenominator;
    public final int tempoBpm;
    public final int resPerBeat; // resolution steps per beat (e.g., 4 => 16ths)
    public final int bars;

    public RhythmSpec(int meterNumerator, int meterDenominator, int tempoBpm, int resPerBeat, int bars) {
        this.meterNumerator = meterNumerator;
        this.meterDenominator = meterDenominator;
        this.tempoBpm = tempoBpm;
        this.resPerBeat = resPerBeat;
        this.bars = bars;
    }

    public int beats() { return meterNumerator; }

    public int totalSteps() { return bars * meterNumerator * resPerBeat; }

}
