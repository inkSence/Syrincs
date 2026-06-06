package syrincs.a_domain.chord;

import java.util.*;

public class NoteCombinator {

    public List<List<Integer>> generateChords(int k, int minLowerNote, int maxUpperNote) {
        if (k <= 0) return List.of();
        if (minLowerNote > maxUpperNote) return List.of();
        // Wenn nicht genug Töne in der Range vorhanden sind, gibt es keine Kombinationen
        if ((maxUpperNote - minLowerNote + 1) < k) return List.of();

        // 1) Alle Kombinationen generieren (ohne Pitch-Class-/Spannweiten-Filter)
        List<List<Integer>> all = new ArrayList<>();
        int[] buf = new int[k];
        backtrack(buf, 0, minLowerNote, maxUpperNote, all);
        return keepUniquePitchClasses(all);
    }

    public List<List<Integer>> keepWidthLessThanOctaves(List<List<Integer>> all, int octaves){
        List<List<Integer>> filtered = new ArrayList<>();
        for (List<Integer> chord : all) {
            if (withinOctaves(chord, octaves)) {
                filtered.add(chord);
            }
        }
        return filtered;
    }

    private List<List<Integer>> keepUniquePitchClasses(List<List<Integer>> all){
        List<List<Integer>> filtered = new ArrayList<>();
        for (List<Integer> chord : all) {
            if(hasUniquePitchClasses(chord)){
                filtered.add(chord);
            }
        }
        return filtered;
    }

    private void backtrack(int[] buf, int index, int min, int max, List<List<Integer>> out) {
        int k = buf.length;
        if (index == k) {
            List<Integer> chord = new ArrayList<>(k);
            for (int v : buf) chord.add(v);
            out.add(chord);
            return;
        }

        // Standard-Kombinationsgeneration (streng ansteigend), ohne weitere Filter
        for (int start = min; start <= max - (k - index - 1); start++) {
            buf[index] = start;
            backtrack(buf, index + 1, start + 1, max, out);
        }
    }

    private static boolean withinOctaves(List<Integer> chord, int octaves) {
        if (chord.isEmpty()) return true;
        int lowest = chord.getFirst();
        int highest = chord.getLast();
        return (highest - lowest) <= (octaves * 12);
    }

    private static boolean hasUniquePitchClasses(List<Integer> chord) {
        boolean[] seen = new boolean[12];
        for (int n : chord) {
            int pc = Math.floorMod(n, 12);
            if (seen[pc]) return false;
            seen[pc] = true;
        }
        return true;
    }

}
