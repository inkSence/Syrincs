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

    // -- Required private helpers (names as specified) --
    // Generates all numbers from 0 (inclusive) to 2^16-1 (inclusive)
    private List<Integer> generateAllNumbersForRhythmsOfFourQuarters() {
        int max = (1 << 16); // 65536
        List<Integer> out = new ArrayList<>(max);
        for (int i = 0; i < max; i++) out.add(i);
        return out;
    }

    // Converts numbers to 16-character binary strings (left-padded with '0')
    private List<String> convertToBinaryStringOf16Digits(List<Integer> numbers) {
        Objects.requireNonNull(numbers, "numbers");
        List<String> out = new ArrayList<>(numbers.size());
        for (Integer n : numbers) {
            if (n == null) continue;
            String bin = Integer.toBinaryString(n);
            if (bin.length() < 16) {
                bin = "0".repeat(16 - bin.length()) + bin;
            } else if (bin.length() > 16) {
                bin = bin.substring(bin.length() - 16); // safety clip
            }
            out.add(bin);
        }
        return out;
    }

    // Persists rhythms using the repository
    private List<Long> persist(List<HuffmanRhythm> rhythms) {
        return repository.saveAll(rhythms);
    }

    // -- Public API --

    /**
     * Full pipeline: generate numbers, convert to binary strings, map to HuffmanRhythm (4/4, tempo default), persist, return.
     */
    public void generateAllRhythmsOfFourQuarters() {
        List<Integer> nums = generateAllNumbersForRhythmsOfFourQuarters();
        List<String> binaries = convertToBinaryStringOf16Digits(nums);
        List<HuffmanRhythm> rhythms = toRhythms(binaries);
        persist(rhythms);
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

    // -- Internal mapping helper --
    private static List<HuffmanRhythm> toRhythms(List<String> binaries) {
        List<HuffmanRhythm> out = new ArrayList<>(binaries.size());
        for (String b : binaries) {
            if (b == null) continue;
            String s = b.trim();
            if (s.length() != 16) continue;
            // build onset string of 'x' and 'o' with spaces per beat (4 groups of 4)
            StringBuilder onset = new StringBuilder(19); // 16 + 3 spaces
            for (int i = 0; i < 16; i++) {
                char c = s.charAt(i);
                onset.append(c == '1' ? 'x' : 'o');
                // if (i % 4 == 3 && i < 15) onset.append(' '); // Todo: Wäre schön als Methode beim Persistieren, um die Lesbarkeit zu steigern.
            }
            out.add(new HuffmanRhythm(4, 4, AppDefaults.DEFAULT_TEMPO_BPM, onset.toString()));
        }
        return out;
    }
}
