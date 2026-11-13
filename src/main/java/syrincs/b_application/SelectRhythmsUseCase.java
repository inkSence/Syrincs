package syrincs.b_application;

import syrincs.a_domain.rhythm.HuffmanRhythm;
import syrincs.b_application.ports.dto.DeviationRange;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;

/** Read-only selection stage: reads candidates, but never prints or plays them. */
public final class SelectRhythmsUseCase {
    private final SearchRhythmsUseCase search;

    public SelectRhythmsUseCase(SearchRhythmsUseCase search) {
        this.search = Objects.requireNonNull(search);
    }

    /**
     * One java.util.Random per call; exactly one nextInt(candidateCount) per
     * requested position, including singleton sets. Repeated grades are drawn
     * with replacement from the same complete, canonical set within this call.
     * Null range retains the strict playback default; null seed generates a seed.
     */
    public Selection select(List<Integer> informationGrades, DeviationRange range, Long seed) {
        if (informationGrades == null || informationGrades.isEmpty()) {
            throw new IllegalArgumentException("At least one information grade is required");
        }
        for (int i = 0; i < informationGrades.size(); i++) {
            Integer grade = informationGrades.get(i);
            if (grade == null || grade < 0) {
                throw new IllegalArgumentException("Information grade at position " + (i + 1) + " must be non-negative");
            }
        }
        Map<Integer, List<HuffmanRhythm>> candidatesByGrade = new HashMap<>();
        List<String> missing = new ArrayList<>();
        for (int i = 0; i < informationGrades.size(); i++) {
            int grade = informationGrades.get(i);
            var candidates = candidatesByGrade.computeIfAbsent(grade, key -> search.findPlaybackCandidates(key, range));
            if (candidates.isEmpty()) missing.add("position " + (i + 1) + " (info=" + grade + ")");
        }
        if (!missing.isEmpty()) {
            throw new IllegalStateException("No stored Huffman rhythms found for " + String.join(", ", missing)
                    + (range == null ? " with deviation > " + AppDefaults.MIN_HUFFMAN_RHYTHM_DEVIATION
                                     : " with inclusive deviation range " + range)
                    + ". Fill the rhythm database with `syrincs init` and `syrincs calculate rhythms`, "
                    + "then retry `syrincs play rhythm info "
                    + informationGrades.stream().map(String::valueOf).collect(Collectors.joining(" "))
                    + (range != null && range.min() != null ? " --deviation-min " + range.min() : "")
                    + (range != null && range.max() != null ? " --deviation-max " + range.max() : "")
                    + (seed == null ? "" : " --seed " + seed) + "`.");
        }
        long actualSeed = seed == null ? ThreadLocalRandom.current().nextLong() : seed;
        var random = new Random(actualSeed);
        List<SelectedRhythm> selected = new ArrayList<>();
        for (int i = 0; i < informationGrades.size(); i++) {
            int grade = informationGrades.get(i);
            var candidates = candidatesByGrade.get(grade);
            selected.add(new SelectedRhythm(i + 1, grade, candidates.get(random.nextInt(candidates.size()))));
        }
        return new Selection(actualSeed, range, selected);
    }

    public record SelectedRhythm(int position, int requestedInformation, HuffmanRhythm rhythm) {}

    public record Selection(long seed, DeviationRange deviationRange, List<SelectedRhythm> positions) {
        public Selection {
            positions = List.copyOf(positions);
        }

        public List<HuffmanRhythm> rhythms() {
            return positions.stream().map(SelectedRhythm::rhythm).toList();
        }
    }
}
