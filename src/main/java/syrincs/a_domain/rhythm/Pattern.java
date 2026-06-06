package syrincs.a_domain.rhythm;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

public class Pattern {
    // voiceName -> hits array (true=x, false=-). All arrays must have same length.
    private final Map<String, boolean[]> voices = new LinkedHashMap<>();

    /**
     * Adds or replaces a voice pattern. Input array is defensively copied to preserve encapsulation.
     */
    public void put(String voiceName, boolean[] hits) {
        if (voiceName == null) throw new IllegalArgumentException("voiceName must not be null");
        if (hits == null) throw new IllegalArgumentException("hits must not be null");
        voices.put(voiceName, hits.clone());
    }

    /**
     * Returns an unmodifiable view with defensive copies of the underlying arrays to prevent external mutation.
     */
    public Map<String, boolean[]> voices() {
        Map<String, boolean[]> copy = new LinkedHashMap<>(voices.size());
        for (var e : voices.entrySet()) {
            copy.put(e.getKey(), e.getValue() == null ? null : e.getValue().clone());
        }
        return Collections.unmodifiableMap(copy);
    }

    public int length() {
        int len = -1;
        for (boolean[] arr : voices.values()) {
            if (arr == null) continue;
            if (len < 0) len = arr.length; else if (len != arr.length) return -1;
        }
        return Math.max(len, 0);
    }
}
