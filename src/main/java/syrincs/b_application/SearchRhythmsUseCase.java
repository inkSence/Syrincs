package syrincs.b_application;

import syrincs.a_domain.rhythm.HuffmanRhythm;
import syrincs.b_application.ports.RhythmRepository;
import syrincs.b_application.ports.dto.DeviationRange;

import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.TreeMap;

/** Read-only catalog search; no selection, mapping or playback dependencies. */
public final class SearchRhythmsUseCase {
    private final RhythmRepository repository;

    public SearchRhythmsUseCase(RhythmRepository repository) {
        this.repository = Objects.requireNonNull(repository);
    }

    /**
     * Filters stored catalog aggregates through the repository. Returned analysis
     * values are reconstructed from onsets; legacy catalog values may differ.
     * The limit applies only after content deduplication and canonical ordering.
     */
    public Result search(int information, DeviationRange range, int limit) {
        if (information < 0) throw new IllegalArgumentException("--info must be non-negative");
        if (limit <= 0) throw new IllegalArgumentException("--limit must be positive");
        Objects.requireNonNull(range, "deviation range");

        List<HuffmanRhythm> matches = findCandidates(information, range);
        return new Result(information, range, limit, matches.size(), matches.stream().limit(limit)
                .map(r -> new Candidate(r.getOnsetList(), r.getNumerator(), r.getDenominator(),
                        r.getBeatInformation(), r.getInformation(), r.getStandardDeviation())).toList());
    }

    /** Complete unique candidate set, without the CLI display limit. */
    public List<HuffmanRhythm> findCandidates(int information, DeviationRange range) {
        validateInformation(information);
        Objects.requireNonNull(range, "deviation range");
        return canonicalCandidates(repository.getAllByInformationAndDeviationRange(information, range));
    }

    /** Null range preserves the historical strict minimum only for playback selection. */
    public List<HuffmanRhythm> findPlaybackCandidates(int information, DeviationRange range) {
        validateInformation(information);
        return range == null
                ? canonicalCandidates(repository.getAllByInformationAndMinDeviation(information,
                        AppDefaults.MIN_HUFFMAN_RHYTHM_DEVIATION))
                : findCandidates(information, range);
    }

    private static void validateInformation(int information) {
        if (information < 0) throw new IllegalArgumentException("Information grade must be non-negative");
    }

    private static List<HuffmanRhythm> canonicalCandidates(List<HuffmanRhythm> matches) {
        if (matches == null) throw new IllegalStateException("RhythmRepository returned no result list");
        var unique = new TreeMap<Identity, HuffmanRhythm>(Comparator.comparingInt(Identity::numerator)
                .thenComparingInt(Identity::denominator).thenComparing(Identity::onsets));
        for (HuffmanRhythm rhythm : matches) {
            var identity = new Identity(rhythm.getNumerator(), rhythm.getDenominator(), rhythm.getOnsetList());
            unique.putIfAbsent(identity, rhythm);
        }
        return List.copyOf(unique.values());
    }

    private record Identity(int numerator, int denominator, String onsets) {}

    public record Candidate(String onsets, int numerator, int denominator, List<Integer> beatInformation,
                            int information, double deviation) {
        public Candidate {
            beatInformation = List.copyOf(beatInformation);
        }
    }

    public record Result(int information, DeviationRange deviationRange, int limit, int totalMatches,
                         List<Candidate> candidates) {
        public Result {
            candidates = List.copyOf(candidates);
        }
    }
}
