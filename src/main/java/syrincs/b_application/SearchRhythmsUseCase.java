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

        List<HuffmanRhythm> matches = repository.getAllByInformationAndDeviationRange(information, range);
        if (matches == null) throw new IllegalStateException("RhythmRepository returned no result list");
        var unique = new TreeMap<Identity, Candidate>(Comparator.comparingInt(Identity::numerator)
                .thenComparingInt(Identity::denominator).thenComparing(Identity::onsets));
        for (HuffmanRhythm rhythm : matches) {
            var identity = new Identity(rhythm.getNumerator(), rhythm.getDenominator(), rhythm.getOnsetList());
            unique.putIfAbsent(identity, new Candidate(identity.onsets(), identity.numerator(), identity.denominator(),
                    rhythm.getBeatInformation(), rhythm.getInformation(), rhythm.getStandardDeviation()));
        }
        return new Result(information, range, limit, unique.size(), unique.values().stream().limit(limit).toList());
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
