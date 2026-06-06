package syrincs.a_domain.hindemith;

import syrincs.a_domain.Interval;
import syrincs.a_domain.chord.Chord;

import java.util.*;
import java.util.stream.Collectors;

public class ChordAnalysis {
    // Algorithmus zur Bestimmung eines Akkordes:
    // "Die Bestandaufnahme der Klänge scheidet darum das gesamte Akkordmaterial zunächst in zwei Hauptgruppen: In der Gruppe A sind alle tritonusfreien Klänge. Den Akkorden mit Tritonus wird die Gruppe B zugewiesen." S.119

    public enum Column { A_TRITONE_FREE, B_WITH_TRITONE }

    public static final class Result {
        public final Column column;
        public final int rootNote; // MIDI
        public final int group; // 1..18
        public final int frameInterval;
        public final List<Integer> notes; // sorted copy

        public Result(Column column,
                      int rootNote,
                      int group,
                      int frameInterval,
                      List<Integer> notes) {
            this.column = column;
            this.rootNote = rootNote;
            this.group = group;
            this.frameInterval = frameInterval;
            this.notes = notes;
        }
    }

    private static final Map<Integer, ChordSpecification> GROUP_SPECIFICATIONS =
            new ChordSpecificationRepository().getGroupSpecifications();

    private final Series2 series2 = new Series2();

    /**
     * Analysiert einen Akkord (Liste von MIDI-Noten) nach Hindemith.
     * - Gruppiert nach Tritonus vorhanden (Spalten A/B)
     * - Bestimmt optionalen Grundton (über bestehende Chord-Logik)
     * - Ermittelt eine Akkordgruppe 1..18, indem die vorhandenen Constraints geprüft werden
     */
    public Result analyze(List<Integer> midiNotes) {
        if (midiNotes == null || midiNotes.size() < 3) {
            throw new IllegalArgumentException("Need at least 3 notes to analyze a chord");
        }
        List<Integer> notes = midiNotes.stream().sorted().toList();
        int frame = Collections.max(notes) - Collections.min(notes);

        Column column = hasTritoneInPitchClasses(notes) ? Column.B_WITH_TRITONE : Column.A_TRITONE_FREE;

        Chord chord = new Chord(notes);
        List<HindemithInterval> intervals = mapIntervals(chord.getAllIntervals());
        Integer root = calculateRootNote(intervals);
        List<HindemithInterval> rootNoteIntervals = calculateRootIntervals(intervals, chord.getNumNotes());
        List<HindemithInterval> pcHindemithIntervals = calculateAllIntervalsOfPitchClasses(chord.getNotes());

        int group = classifyChordGroup(notes, root, intervals, rootNoteIntervals, pcHindemithIntervals);

        return new Result(column, root, group, frame, notes);
    }




    private List<HindemithInterval> calculateRootIntervals(List<HindemithInterval> intervals, int numNotes){
        // Provided that Intervals are in the right order
        return intervals.subList(0, numNotes - 1);
    }

    private Integer calculateRootNote(List<HindemithInterval> hindemithIntervals){
        List<HindemithInterval> bestHindemithInterval = calculateBestInterval(hindemithIntervals);

        if (bestHindemithInterval.isEmpty()) {
            throw new IllegalArgumentException("No best hindemith interval found");
        }
        if (bestHindemithInterval.size() == 1) {
            return bestHindemithInterval.getFirst().getRootNote();
        }
        // Immer das unterste nehmen. Vgl. Unterweisung im Tonsatz S. 120
        return bestHindemithInterval.stream().mapToInt(HindemithInterval::getRootNote).min().orElse(bestHindemithInterval.getFirst().getRootNote());

    }

    private List<HindemithInterval> calculateBestInterval(List<HindemithInterval> hindemithIntervals){
        List<HindemithInterval> bestHindemithInterval = new ArrayList<>();

        // Wir berechnen die besten Intervalle basierend auf allen Notenpaaren
        for(HindemithInterval hindemithInterval : hindemithIntervals){
            if(bestHindemithInterval.isEmpty()){
                bestHindemithInterval.add(hindemithInterval);
                continue;
            }

            if ( bestHindemithInterval.getFirst().getQuality() > hindemithInterval.getQuality() && hindemithInterval.getQuality() != -1 ) {
                bestHindemithInterval.clear();
                bestHindemithInterval.add(hindemithInterval);
            } else if (bestHindemithInterval.getFirst().getQuality() == hindemithInterval.getQuality()) {
                bestHindemithInterval.add(hindemithInterval);
            }
        }
        return bestHindemithInterval;
    }



    public List<HindemithInterval> calculateAllIntervalsOfPitchClasses(List<Integer> notes){
        List<HindemithInterval> hindemithIntervalList = new ArrayList<>();
        List<Integer> pitchClasses = notes.stream().map(n-> n%12).distinct().sorted().toList();
        for (int i = 0; i < pitchClasses.size(); i++) {
            for (int gap = 1; i + gap < pitchClasses.size(); gap++) {
                HindemithInterval hindemithInterval = new HindemithInterval(pitchClasses.get(i), pitchClasses.get(i+gap));
                hindemithIntervalList.add(hindemithInterval);
            }
        }
        return hindemithIntervalList;
    }


    private List<HindemithInterval> mapIntervals(List<Interval> from){
        return from.stream()
                .map(i -> new HindemithInterval(i.getLowNote(), i.getHighNote()))
                .collect(Collectors.toList());
    }

    private boolean hasTritoneInPitchClasses(List<Integer> notes) {
        List<Integer> pcs = notes.stream().map(n -> Math.floorMod(n, 12)).distinct().toList();
        for (int i = 0; i < pcs.size(); i++) {
            for (int j = i + 1; j < pcs.size(); j++) {
                int st = Math.floorMod(pcs.get(j) - pcs.get(i), 12);
                if (series2.isTritone(st)) return true;
            }
        }
        return false;
    }

    private int classifyChordGroup(
            List<Integer> notes,
            int rootNote,
            List<HindemithInterval> intervals,
            List<HindemithInterval> rootNoteIntervals,
            List<HindemithInterval> pcHindemithIntervals
    ) {
        int bassNote = notes.getFirst();
        List<Integer> groupSpecsList = GROUP_SPECIFICATIONS.keySet().stream().sorted().toList();
        // Determine by group specification order (insertion/group-number order)
        for (Integer g : groupSpecsList) {
            ChordSpecification spec = GROUP_SPECIFICATIONS.get(g);
            if (spec == null) continue;
            boolean match1 = ChordRules.matchesIntervalsOnly(intervals, rootNoteIntervals, pcHindemithIntervals, spec);
            boolean match2 = ChordRules.rootRelation(bassNote, rootNote, spec.getRootNoteEqual());
            boolean match3 = ChordRules.columnRequirement(pcHindemithIntervals, spec.getColumnRequirement());

            if (match1 && match2 && match3) {
                return g + 1; // keep existing 1-based group numbering
            }
        }
        throw new IllegalStateException("Chord has no matching group: " + notes);
    }

    public List<HindemithChord> analyzeList(List<List<Integer>> noteSets) {
        List<HindemithChord> hindemithChords = new ArrayList<>();
        for (List<Integer> notes : noteSets) {
            var result = analyze(notes);
            HindemithChord hindemithChord = new HindemithChord(
                    notes,
                    result.rootNote,
                    result.group
            );
            hindemithChords.add(hindemithChord);
        }
        return hindemithChords;
    }
}
