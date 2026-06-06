package syrincs.a_domain.rhythm;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class Rhythm {
    // "regular recurring motion" https://en.wikipedia.org/wiki/Rhythm
    int numerator = 4;
    int denominator = 4;
    int positionsPerBeat = 4;
    int tempo; // bpm, must be provided by callers
    String onsetList;
    List<String> onsetListPerBeat;

    public Rhythm(int numerator, int denominator, int tempo, String onsetList) {
        this.numerator = numerator;
        this.denominator = denominator;
        this.tempo = tempo;
        this.onsetList = Objects.requireNonNull(onsetList, "onsetList").replaceAll("\\s+", "").toLowerCase();
        hasValidLength(this.onsetList);
        hasValidCharacters(this.onsetList);
        this.onsetListPerBeat = makeOnsetBeatPerList(this.onsetList);
    }

    private void hasValidLength(String onsetList){
        int length = onsetList.length();
        int positionsPerBar = positionsPerBeat * numerator;
        boolean valid = length % positionsPerBar == 0 && length > 0;
        if(!valid){
            throw new IllegalArgumentException(String.format("length %d is not valid in %s", length, onsetList));
        }
    }

    private void hasValidCharacters(String onsetList){
        boolean matching =  onsetList.matches("[xo]*");
        if(!matching){
            throw new IllegalArgumentException("onsets containing invalid characters");
        }
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

    public List<String> getOnsetListPerBeat() {
        return List.copyOf(onsetListPerBeat);
    }

    private List<String> makeOnsetBeatPerList(String onsetList){
        List<String> onsetListPerBeat = new ArrayList<>();
        int beats = onsetList.length() / positionsPerBeat;
        for(int i = 0; i < beats; i++){
            int beginnIndex = i * positionsPerBeat;
            String subString = onsetList.substring(beginnIndex, beginnIndex +  positionsPerBeat);
            onsetListPerBeat.add(subString);
        }
        return onsetListPerBeat;
    }
}
