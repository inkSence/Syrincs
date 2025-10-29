package syrincs.a_domain.rhythm;

public class Rhythm {
    // "regular recurring motion" https://en.wikipedia.org/wiki/Rhythm
    int numerator = 4;
    int denominator = 4;
    int positionsPerBeat = 4;
    int tempo; // bpm, must be provided by callers
    String onsetList;

    public Rhythm(int numerator, int denominator, int tempo, String onsetList) {
        this.numerator = numerator;
        this.denominator = denominator;
        this.tempo = tempo;
        this.onsetList = onsetList.replaceAll(" ", "").toLowerCase();
    }

    boolean hasValidLength(String onsetList){
        int length = onsetList.length();
        int positionsPerBar = positionsPerBeat * numerator;
        return length % positionsPerBar == 0 && length > 0;
    }

    boolean hasValidCharacters(String onsetList){
        return onsetList.matches("[xo]*");
    }

    boolean isOnBeat(int positionInString){
        return getPositionOfBeat(positionInString) % positionsPerBeat == 0;
    }

    int getPositionOfBeat(int positionInString){
        if(positionInString < 0 || positionInString >= onsetList.length()){
            throw new IllegalArgumentException("Invalid position in string: " + positionInString);
        }
        return positionInString % positionsPerBeat;
    }

    public int getNumerator() {
        return numerator;
    }

    public int getDenominator() {
        return denominator;
    }

    public int getTempo() {
        return tempo;
    }

    public String getOnsetList() {
        return onsetList;
    }
}


