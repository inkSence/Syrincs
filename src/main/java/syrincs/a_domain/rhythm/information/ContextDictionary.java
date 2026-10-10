package syrincs.a_domain.rhythm.information;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public final class ContextDictionary {
    public static final String VERSION = "suffix-periods-16-v1";

    private ContextDictionary() {}

    public static List<String> fromHistory(String historyBits) {
        if (!historyBits.matches("[01]*"))
            throw new IllegalArgumentException("Invalid binary history");
        String h = historyBits.substring(Math.max(0, historyBits.length() - 32));
        List<String> result = new ArrayList<>();
        for (int p = 1; p <= Math.min(16, h.length() / 2); p++) {
            String word = h.substring(h.length() - p);
            if (!word.equals(h.substring(h.length() - 2 * p, h.length() - p))) continue;
            boolean primitive = true;
            for (int r = 1; r < p; r++)
                if (p % r == 0 && repeat(word.substring(0, r), p).equals(word)) {
                    primitive = false;
                    break;
                }
            if (primitive && !result.contains(word)) result.add(word);
        }
        result.sort(
                Comparator.comparingInt(String::length).thenComparing(Comparator.naturalOrder()));
        return List.copyOf(result);
    }

    public static String repeat(String word, int slots) {
        if (word.isEmpty()) throw new IllegalArgumentException("Empty motif");
        return word.repeat((slots + word.length() - 1) / word.length()).substring(0, slots);
    }

    public static String xor(String a, String b) {
        if (a.length() != b.length()) throw new IllegalArgumentException("XOR length mismatch");
        StringBuilder result = new StringBuilder(a.length());
        for (int i = 0; i < a.length(); i++) result.append(a.charAt(i) == b.charAt(i) ? '0' : '1');
        return result.toString();
    }
}
