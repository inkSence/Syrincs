package syrincs.a_domain.rhythm;

import java.util.ArrayList;
import java.util.List;

public class HufmanRhythm extends Rhythm {

    int information;

    public HufmanRhythm(int numerator, int denominator, int tempo, String onsetList) {
        super(numerator, denominator, tempo, onsetList);
    }

    int calculateInformation(String onsetString){
        /* Wörterbuch = {
         00 Trennung erniedrigen,
         01 Trennung erhöhen,
         10 Pausen spielen,
         11 Noten spielen } */

        List<String> rhythmCodation = new ArrayList<>();
        boolean playNotes = false;
        int noteValue = 4;
        for( int i = 0; i < onsetList.length(); i++ ){
            Character c =  onsetString.charAt(i);
            int position = getPositionOfBeat(i);

            if(0 == position) {
                noteValue = 4;
                if (
                    c.equals('x') &&
                    !playNotes
                ) {
                    rhythmCodation.add("11");
                    playNotes = true;
                } else if (
                    c.equals('o') &&
                    playNotes
                ) {
                    rhythmCodation.add("10");
                    playNotes = false;
                }
            } else {
                if(
                    4 == noteValue &&
                    c.equals('x')
                ) {
                    noteValue /= 2;
                    rhythmCodation.add("01");
                }
                if(
                    2 != position // Das zweite oder letzte Zeichen der Gruppe.
                ){
                    if(
                        4 == noteValue &&
                        c.equals('x')
                    ) {
                        noteValue /= 4;
                        rhythmCodation.addAll(List.of("01", "01"));
                    }
                    if(
                            3 == position  && // letztes Zeichen der Gruppe
                            1 == noteValue &&
                            playNotes &&
                            c.equals('o')
                    ){
                        rhythmCodation.add("10");
                    }
                } else { // das "dritte" Zeichen der Gruppe
                    if(
                        1 == noteValue &&
                        playNotes &&
                        c.equals('o')
                    ){
                        noteValue *= 2;
                        rhythmCodation.add("10");
                        playNotes = false;
                    }
                }
            }
        }
        return rhythmCodation.size();
    }

    public static void main(String[] args){
        var rhythm = new HufmanRhythm(4,4,90, "xooo oooo oooo oooo");
        int info = rhythm.calculateInformation(rhythm.onsetList);
        System.out.println(info);

    }




}
