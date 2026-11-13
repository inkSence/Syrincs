package syrincs.b_application;

import syrincs.a_domain.rhythm.HuffmanRhythm;
import syrincs.b_application.ports.RhythmRepository;
import syrincs.b_application.ports.dto.BeatProfileCriteria;
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
        return search(information, range, limit, new BeatProfileCriteria(null, null));
    }

    /** Profile criteria use reconstructed beat values, before deduplication, count and limit. */
    public Result search(int information, DeviationRange range, int limit, BeatProfileCriteria criteria) {
        if (information < 0) throw new IllegalArgumentException("--info must be non-negative");
        if (limit <= 0) throw new IllegalArgumentException("--limit must be positive");
        Objects.requireNonNull(range, "deviation range");
        Objects.requireNonNull(criteria, "beat profile criteria").validateInformation(information);

        List<HuffmanRhythm> matches = findCandidates(information, range, criteria);
        return new Result(information, range, limit, matches.size(), matches.stream().limit(limit)
                .map(r -> new Candidate(r.getOnsetList(), r.getNumerator(), r.getDenominator(),
                        r.getBeatInformation(), r.getInformation(), r.getStandardDeviation())).toList());
    }

    /** Complete unique candidate set, without the CLI display limit. */
    public List<HuffmanRhythm> findCandidates(int information, DeviationRange range) {
        return findCandidates(information, range, new BeatProfileCriteria(null, null));
    }

    private List<HuffmanRhythm> findCandidates(int information, DeviationRange range, BeatProfileCriteria criteria) {
        validateInformation(information);
        Objects.requireNonNull(range, "deviation range");
        List<HuffmanRhythm> matches = repository.getAllByInformationAndDeviationRange(information, range);
        if (matches == null) throw new IllegalStateException("RhythmRepository returned no result list");
        if (criteria.active()) {
            matches = matches.stream()
                    .filter(r -> r.getNumerator() == 4 && r.getDenominator() == 4)
                    .filter(r -> criteria.matches(r.getBeatInformation())).toList();
        }
        return canonicalCandidates(matches);
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
