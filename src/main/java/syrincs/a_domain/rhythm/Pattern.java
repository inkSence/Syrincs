package syrincs.a_domain.rhythm;

import java.util.LinkedHashMap;
import java.util.Map;

public class Pattern {
    // voiceName -> hits array (true=x, false=-). All arrays must have same length.
    private final Map<String, boolean[]> voices = new LinkedHashMap<>();

    public void put(String voiceName, boolean[] hits) {
        voices.put(voiceName, hits);
    }

    public Map<String, boolean[]> voices() { return voices; }

    public int length() {
        int len = -1;
        for (boolean[] arr : voices.values()) {
            if (arr == null) continue;
            if (len < 0) len = arr.length; else if (len != arr.length) return -1;
        }
        return Math.max(len, 0);
    }
}
