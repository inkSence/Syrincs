package syrincs.b_application;

import syrincs.a_domain.rhythm.HuffmanRhythm;
import syrincs.b_application.ports.RhythmRepository;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Use case to generate all 4/4 rhythms with sixteenth resolution (16 positions per bar)
 * and persist them using the RhythmRepository.
 */
public class GenerateAndPersistRhythmUseCase {

    private final RhythmRepository repository;

    public GenerateAndPersistRhythmUseCase(RhythmRepository repository) {
        this.repository = Objects.requireNonNull(repository, "repository");
    }

    private List<Long> persist(List<HuffmanRhythm> rhythms) {
        return repository.saveAll(rhythms);
    }

    /**
     * Full pipeline: generate all 16-step onset masks for 4/4, map to HuffmanRhythm,
     * persist, and return generated ids.
     */
    public List<Long> generateAllRhythmsOfFourQuarters() {
        int max = (1 << 16);
        List<HuffmanRhythm> rhythms = new ArrayList<>(max);
        for (int mask = 0; mask < max; mask++) {
            rhythms.add(toRhythm(mask));
        }
        return persist(rhythms);
    }

    /**
     * Maps provided binary strings (length 16, using '0'/'1') to HuffmanRhythm, persists, and returns the list.
     * This overload is provided as requested to be callable with a List<String>.
     */
    public List<HuffmanRhythm> generateAllRhythmsOfFourQuarters(List<String> binaryStrings) {
        Objects.requireNonNull(binaryStrings, "binaryStrings");
        List<HuffmanRhythm> rhythms = toRhythms(binaryStrings);
        persist(rhythms);
        return rhythms;
    }

    private static HuffmanRhythm toRhythm(int mask) {
        StringBuilder onset = new StringBuilder(16);
        for (int bit = 15; bit >= 0; bit--) {
            onset.append((mask & (1 << bit)) == 0 ? 'o' : 'x');
        }
        return new HuffmanRhythm(4, 4, AppDefaults.DEFAULT_TEMPO_BPM, onset.toString());
    }

    private static List<HuffmanRhythm> toRhythms(List<String> binaries) {
        List<HuffmanRhythm> out = new ArrayList<>(binaries.size());
        for (String b : binaries) {
            if (b == null) continue;
            String s = b.trim();
            if (s.length() != 16) continue;
            StringBuilder onset = new StringBuilder(16);
            for (int i = 0; i < 16; i++) {
                char c = s.charAt(i);
                if (c != '0' && c != '1') {
                    throw new IllegalArgumentException("Binary rhythm strings may only contain 0 or 1: " + s);
                }
                onset.append(c == '1' ? 'x' : 'o');
            }
            out.add(new HuffmanRhythm(4, 4, AppDefaults.DEFAULT_TEMPO_BPM, onset.toString()));
        }
        return out;
    }
}
