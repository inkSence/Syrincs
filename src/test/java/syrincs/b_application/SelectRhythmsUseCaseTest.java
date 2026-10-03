package syrincs.b_application;

import org.junit.jupiter.api.Test;
import syrincs.a_domain.rhythm.HuffmanRhythm;
import syrincs.b_application.ports.dto.DeviationRange;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class SelectRhythmsUseCaseTest {
    private static final HuffmanRhythm LOW = rhythm("xooo xoxo xooo xoxo");
    private static final HuffmanRhythm HIGH = rhythm("xoxo xooo xoxo xooo");
    private static final HuffmanRhythm SILENCE = rhythm("oooo ".repeat(4));
    private static final DeviationRange ALL = new DeviationRange(null, null);

    @Test
    void seed42HasOneDrawPerPositionIncludingSingletonAndRepeatedGrades() {
        var repo = new FilteringRhythmRepository(List.of(HIGH, LOW, SILENCE));
        var useCase = selector(repo);
        var result = useCase.select(List.of(3, 0, 3, 3), ALL, 42L);

        // java.util.Random(42), nextInt bounds [2,1,2,2] -> indices [1,0,1,0].
        assertEquals(List.of(HIGH, SILENCE, HIGH, LOW), result.rhythms());
        assertEquals(List.of(1, 2, 3, 4), result.positions().stream().map(p -> p.position()).toList());
        assertEquals(List.of(3, 0, 3, 3), result.positions().stream().map(p -> p.requestedInformation()).toList());
        assertEquals(42L, result.seed());
        assertEquals(ALL, result.deviationRange());
        assertEquals(2, repo.rangeQueries); // one complete set per distinct grade within the call
        assertEquals(result, useCase.select(List.of(3, 0, 3, 3), ALL, 42L));
        assertThrows(UnsupportedOperationException.class, () -> result.positions().clear());
        assertThrows(UnsupportedOperationException.class, () -> result.rhythms().clear());
    }

    @Test
    void duplicatesAndRepositoryOrderDoNotChangeAnySeededChoice() {
        var original = List.of(HIGH, LOW, SILENCE);
        var duplicateLow = rhythm("XOOO\tXOXO\nXOOO XOXO");
        var reordered = List.of(SILENCE, HIGH, duplicateLow, HIGH, LOW, LOW, SILENCE);
        for (long seed : new long[]{0, 1, 42, -1, Long.MIN_VALUE, Long.MAX_VALUE}) {
            var grades = List.of(3, 3, 0, 3, 3);
            var first = selector(new FilteringRhythmRepository(original)).select(grades, ALL, seed);
            var second = selector(new FilteringRhythmRepository(reordered)).select(grades, ALL, seed);
            assertEquals(onsets(first), onsets(second));
        }
    }

    @Test
    void selectionUsesCompleteCatalogNotSearchDisplayLimit() {
        var fixtures = new ArrayList<HuffmanRhythm>();
        for (int mask = 0; fixtures.size() < 25; mask++) {
            String bits = String.format("%16s", Integer.toBinaryString(mask)).replace(' ', '0');
            var candidate = rhythm(bits.replace('0', 'o').replace('1', 'x'));
            if (candidate.getInformation() == 3) fixtures.add(candidate);
        }
        var sorted = fixtures.stream().sorted(java.util.Comparator.comparing(HuffmanRhythm::getOnsetList)).toList();
        Collections.reverse(fixtures);
        var search = new SearchRhythmsUseCase(new FilteringRhythmRepository(fixtures));
        assertEquals(20, search.search(3, ALL, AppDefaults.DEFAULT_RHYTHM_SEARCH_LIMIT).candidates().size());
        // Random(0).nextInt(25) = 10; truncating to 20 would produce index 0.
        var result = new SelectRhythmsUseCase(search).select(List.of(3), ALL, 0L);
        assertSame(sorted.get(10), result.rhythms().getFirst());
        assertEquals(25, search.findCandidates(3, ALL).size());
    }

    @Test
    void singletonDrawCanSelectSamePatternAtSeveralPositions() {
        var result = selector(new FilteringRhythmRepository(List.of(HIGH))).select(List.of(3, 3, 3), null, 42L);
        assertEquals(List.of(HIGH, HIGH, HIGH), result.rhythms());
    }

    @Test
    void automaticSeedIsExposedAndReplaysWithinSameCatalog() {
        var select = selector(new FilteringRhythmRepository(List.of(LOW, HIGH, SILENCE)));
        var result = select.select(List.of(3, 0, 3), ALL, null);
        assertEquals(result, select.select(List.of(3, 0, 3), ALL, result.seed()));
    }

    @Test
    void allMissingPositionsAreReportedEvenWhenOtherPositionsHaveCandidates() {
        var repo = new FilteringRhythmRepository(List.of(LOW, SILENCE));
        var error = assertThrows(IllegalStateException.class, () ->
                selector(repo).select(List.of(99, 3, 98, 0, 99), ALL, 42L));
        assertTrue(error.getMessage().contains("position 1 (info=99)"));
        assertTrue(error.getMessage().contains("position 3 (info=98)"));
        assertTrue(error.getMessage().contains("position 5 (info=99)"));
        assertTrue(error.getMessage().contains("--seed 42"));
        assertEquals(4, repo.queries);
    }

    @Test
    void invalidGradesFailBeforeAnyCatalogRead() {
        var repo = new FilteringRhythmRepository(List.of());
        var select = selector(repo);
        assertThrows(IllegalArgumentException.class, () -> select.select(null, ALL, 42L));
        assertThrows(IllegalArgumentException.class, () -> select.select(List.of(), ALL, 42L));
        assertThrows(IllegalArgumentException.class, () -> select.select(List.of(3, -1), ALL, 42L));
        assertThrows(IllegalArgumentException.class, () -> select.select(Arrays.asList(3, null), ALL, 42L));
        assertEquals(0, repo.queries);
    }

    @Test
    void repositoryErrorsAreNotMissingCandidateErrors() {
        var failure = new IllegalStateException("Database unavailable");
        var repo = new FilteringRhythmRepository(List.of()) {
            @Override public List<HuffmanRhythm> getAllByInformationAndDeviationRange(Integer info, DeviationRange range) {
                throw failure;
            }
        };
        assertSame(failure, assertThrows(IllegalStateException.class, () -> selector(repo).select(List.of(3), ALL, 42L)));
    }

    private static List<String> onsets(SelectRhythmsUseCase.Selection selection) {
        return selection.rhythms().stream().map(HuffmanRhythm::getOnsetList).toList();
    }

    private static SelectRhythmsUseCase selector(FilteringRhythmRepository repo) {
        return new SelectRhythmsUseCase(new SearchRhythmsUseCase(repo));
    }

    private static HuffmanRhythm rhythm(String onsets) { return new HuffmanRhythm(4, 4, 120, onsets); }
}
