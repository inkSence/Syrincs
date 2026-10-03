package syrincs.b_application;

import org.junit.jupiter.api.Test;
import syrincs.a_domain.rhythm.HuffmanRhythm;
import syrincs.b_application.ports.dto.DeviationRange;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class SearchRhythmsUseCaseTest {
    private static final DeviationRange ALL = new DeviationRange(null, null);
    private static final String LOW = "xooo xoxo xooo xoxo";
    private static final String HIGH = "xoxo xooo xoxo xooo";

    @Test
    void deduplicatesNormalizedContentSortsAndCountsBeforeLimiting() {
        var low = rhythm(4, 4, LOW);
        var high = rhythm(4, 4, HIGH);
        var source = List.of(high, rhythm(4, 4, "XOOO\tXOXO\nXOOO XOXO"), low, high);
        var repo = new FilteringRhythmRepository(source);
        var result = new SearchRhythmsUseCase(repo).search(3, ALL, 1);

        assertEquals(2, result.totalMatches());
        assertEquals(1, result.candidates().size());
        assertEquals(low.getOnsetList(), result.candidates().getFirst().onsets());
        assertEquals(List.of(1, 1, 0, 1), result.candidates().getFirst().beatInformation());
        assertEquals(3, result.information());
        assertEquals(ALL, result.deviationRange());
        assertEquals(1, result.limit());
        assertEquals(1, repo.rangeQueries);
        assertEquals(0, repo.strictQueries);
        assertEquals(ALL, repo.lastRange);

        var reversed = new ArrayList<>(source);
        Collections.reverse(reversed);
        assertEquals(result, new SearchRhythmsUseCase(new FilteringRhythmRepository(reversed)).search(3, ALL, 1));
        assertThrows(UnsupportedOperationException.class, () -> result.candidates().clear());
        assertThrows(UnsupportedOperationException.class, () -> result.candidates().getFirst().beatInformation().clear());
    }

    @Test
    void identityIncludesBothMeterFieldsAndIgnoresTempo() {
        var fixtures = List.of(rhythm(4, 8, LOW), rhythm(4, 4, HIGH), rhythm(2, 8, LOW),
                rhythm(4, 4, LOW), rhythm(2, 4, LOW), new HuffmanRhythm(4, 4, 60, LOW));
        var result = new SearchRhythmsUseCase(new FilteringRhythmRepository(fixtures)).search(3, ALL, 20);
        assertEquals(5, result.totalMatches());
        assertEquals(List.of("2/4", "2/8", "4/4", "4/4", "4/8"),
                result.candidates().stream().map(r -> r.numerator() + "/" + r.denominator()).toList());
        assertEquals(rhythm(4, 4, LOW).getOnsetList(), result.candidates().get(2).onsets());
        assertEquals(rhythm(4, 4, HIGH).getOnsetList(), result.candidates().get(3).onsets());
    }

    @Test
    void filtersExactInformationAndInclusiveBoundsWithoutLegacyMinimum() {
        var low = rhythm(4, 4, LOW);
        var high = rhythm(4, 4, HIGH);
        var repo = new FilteringRhythmRepository(List.of(high, low, rhythm(4, 4, "o".repeat(16))));
        var search = new SearchRhythmsUseCase(repo);
        assertEquals(2, search.search(3, ALL, 20).totalMatches());
        assertEquals(List.of(1, 1, 0, 1), search.search(3, new DeviationRange(null, low.getStandardDeviation()), 20)
                .candidates().getFirst().beatInformation());
        assertEquals(List.of(2, 0, 1, 0), search.search(3, new DeviationRange(high.getStandardDeviation(), null), 20)
                .candidates().getFirst().beatInformation());
        assertEquals(1, search.search(3, new DeviationRange(low.getStandardDeviation(), low.getStandardDeviation()), 20).totalMatches());
        assertEquals(1, search.search(0, new DeviationRange(0.0, 0.0), 20).totalMatches());
        assertEquals(0, repo.strictQueries);
    }

    @Test
    void storedMetadataControlsFilteringButCurrentAnalysisControlsOutput() {
        var low = rhythm(4, 4, LOW);
        var repo = new FilteringRhythmRepository(new FilteringRhythmRepository.Entry(low, 8, 0.9));
        var result = new SearchRhythmsUseCase(repo).search(8, new DeviationRange(0.9, 0.9), 20);
        assertEquals(1, result.totalMatches());
        assertEquals(3, result.candidates().getFirst().information());
        assertEquals(low.getStandardDeviation(), result.candidates().getFirst().deviation());
    }

    @Test
    void invalidInfoAndLimitFailBeforeQuery() {
        var repo = new FilteringRhythmRepository(List.of());
        var search = new SearchRhythmsUseCase(repo);
        assertThrows(IllegalArgumentException.class, () -> search.search(-1, ALL, 20));
        assertThrows(IllegalArgumentException.class, () -> search.search(3, ALL, 0));
        assertThrows(IllegalArgumentException.class, () -> search.search(3, ALL, -1));
        assertThrows(NullPointerException.class, () -> search.search(3, null, 20));
        assertEquals(0, repo.queries);
    }

    @Test
    void emptySearchIsValidAndRepositoryFailureIsNotAnEmptySearch() {
        assertEquals(0, new SearchRhythmsUseCase(new FilteringRhythmRepository(List.of())).search(3, ALL, 20).totalMatches());
        var failure = new RuntimeException("Database unavailable");
        var repo = new FilteringRhythmRepository(List.of()) {
            @Override public List<HuffmanRhythm> getAllByInformationAndDeviationRange(Integer info, DeviationRange range) {
                throw failure;
            }
        };
        assertSame(failure, assertThrows(RuntimeException.class, () -> new SearchRhythmsUseCase(repo).search(3, ALL, 20)));
    }

    private static HuffmanRhythm rhythm(int numerator, int denominator, String onsets) {
        return new HuffmanRhythm(numerator, denominator, 120, onsets);
    }
}
