package syrincs.b_application.rhythm.information;

import static org.junit.jupiter.api.Assertions.*;

import static syrincs.b_application.rhythm.information.GenerationRequest.*;
import static syrincs.b_application.rhythm.information.GenerationResult.StopReason.*;

import org.junit.jupiter.api.Test;

import syrincs.a_domain.rhythm.information.*;

import java.math.BigInteger;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

class GenerateRhythmsByInformationUseCaseTest {
    private final GenerateRhythmsByInformationUseCase generator =
            new GenerateRhythmsByInformationUseCase(new RhythmInformationAnalyzer()::analyze);

    private GenerationRequest request(
            AnalysisSettings settings,
            IntRange info,
            EventConstraints events,
            ProfileConstraints profiles,
            Strategy strategy,
            BigInteger budget,
            int limit,
            Selection selection,
            Long seed) {
        return new GenerationRequest(
                settings, info, null, events, profiles, false, strategy, budget, limit, selection,
                seed);
    }

    private EventConstraints quarters(int n) {
        Set<Integer> positions = new TreeSet<>();
        for (int i = 0; i < n; i += 4) positions.add(i);
        return new EventConstraints(
                new IntRange(n / 4, n / 4), null, positions, Set.of(), null, null);
    }

    @Test
    void exactTargetAndOutputLimitDoNotChangeCompletenessOrCounts() {
        var r =
                generator.execute(
                        request(
                                AnalysisSettings.global(16),
                                new IntRange(13, 13),
                                quarters(16),
                                ProfileConstraints.none(),
                                Strategy.EXHAUSTIVE,
                                null,
                                8,
                                Selection.FIRST,
                                null));
        assertEquals(COMPLETE, r.stopReason());
        assertTrue(r.searchComplete());
        assertEquals(BigInteger.ONE, r.totalMatches());
        assertEquals(7, r.shortfall());
        assertEquals(
                "xooo".repeat(4), r.rhythms().getFirst().primary().sequence().normalizedOnsets());
        assertEquals(BigInteger.valueOf(65536), r.attempts());
        var empty =
                generator.execute(
                        request(
                                AnalysisSettings.global(16),
                                new IntRange(12, 12),
                                quarters(16),
                                ProfileConstraints.none(),
                                Strategy.EXHAUSTIVE,
                                null,
                                8,
                                Selection.FIRST,
                                null));
        assertEquals(NO_SOLUTION, empty.stopReason());
        assertEquals(BigInteger.ZERO, empty.totalMatches());
        var all =
                generator.execute(
                        request(
                                AnalysisSettings.global(16),
                                new IntRange(13, 13),
                                EventConstraints.none(),
                                ProfileConstraints.none(),
                                Strategy.EXHAUSTIVE,
                                null,
                                5,
                                Selection.FIRST,
                                null));
        assertEquals(BigInteger.valueOf(42), all.totalMatches());
        assertEquals(5, all.returnedCount());
    }

    @Test
    void codeSearchAgreesWithMaskEnumerationForEveryScope() {
        for (Scope scope : Scope.values()) {
            var settings =
                    scope == Scope.GLOBAL_DESCRIPTION
                            ? AnalysisSettings.global(4)
                            : new AnalysisSettings(
                                    AnalysisFrame.unknown(4),
                                    scope,
                                    Segmentation.equal(4, 2, 4),
                                    "",
                                    List.of(),
                                    null);
            IntRange info =
                    switch (scope) {
                        case GLOBAL_DESCRIPTION -> new IntRange(3, 7);
                        case ISOLATED_WINDOWS -> new IntRange(6, 10);
                        case CAUSAL_WINDOWS -> new IntRange(6, 12);
                    };
            var expected =
                    generator.execute(
                            request(
                                    settings,
                                    info,
                                    EventConstraints.none(),
                                    ProfileConstraints.none(),
                                    Strategy.EXHAUSTIVE,
                                    null,
                                    20,
                                    Selection.FIRST,
                                    null));
            var actual =
                    generator.execute(
                            request(
                                    settings,
                                    info,
                                    EventConstraints.none(),
                                    ProfileConstraints.none(),
                                    Strategy.CODE_LENGTH_SEARCH,
                                    BigInteger.valueOf(10000),
                                    20,
                                    Selection.FIRST,
                                    null));
            assertTrue(actual.searchComplete());
            assertEquals(BigInteger.valueOf(16), actual.totalMatches());
            assertEquals(words(expected), words(actual));
        }
    }

    @Test
    void nonminimalConstructionWitnessNeverBecomesFilterValue() {
        var result =
                generator.execute(
                        request(
                                AnalysisSettings.global(4),
                                new IntRange(7, 7),
                                EventConstraints.none(),
                                ProfileConstraints.none(),
                                Strategy.CODE_LENGTH_SEARCH,
                                BigInteger.valueOf(128),
                                20,
                                Selection.FIRST,
                                null));
        assertTrue(result.searchComplete());
        assertEquals(BigInteger.valueOf(14), result.totalMatches());
        assertFalse(words(result).contains("oooo"));
        assertFalse(words(result).contains("xxxx"));
    }

    @Test
    void completeLowInformationCodeSearchAt128AndLastBudgetAttempt() {
        var request =
                request(
                        AnalysisSettings.global(128),
                        new IntRange(16, 16),
                        quarters(128),
                        ProfileConstraints.none(),
                        Strategy.CODE_LENGTH_SEARCH,
                        BigInteger.valueOf(65536),
                        1,
                        Selection.FIRST,
                        null);
        var full = generator.execute(request);
        assertTrue(full.searchComplete());
        assertEquals(COMPLETE, full.stopReason());
        assertEquals(BigInteger.ONE, full.totalMatches());
        assertEquals(BigInteger.valueOf(270), full.uniqueCandidatesEvaluated());
        assertEquals("xooo".repeat(32), words(full).getFirst());
        assertEquals(BigInteger.ONE.shiftLeft(128), full.maskSpaceSize());
        assertEquals(BigInteger.ONE.shiftLeft(16), full.codeSpaceSize());
        var partial =
                generator.execute(
                        request(
                                request.settings(),
                                request.information(),
                                request.events(),
                                request.profiles(),
                                request.strategy(),
                                BigInteger.valueOf(65535),
                                1,
                                Selection.RANDOM_WITHOUT_REPLACEMENT,
                                42L));
        assertFalse(partial.searchComplete());
        assertEquals(BUDGET_EXHAUSTED, partial.stopReason());
        assertNull(partial.totalMatches());
        assertFalse(partial.selectionPerformed());
    }

    @Test
    void codeSpaceCounterStaysExactAboveThe128BitMaskSpace() {
        var result =
                generator.execute(
                        request(
                                AnalysisSettings.global(128),
                                new IntRange(131, 131),
                                EventConstraints.none(),
                                ProfileConstraints.none(),
                                Strategy.CODE_LENGTH_SEARCH,
                                BigInteger.ONE,
                                1,
                                Selection.FIRST,
                                null));
        assertEquals(BigInteger.ONE.shiftLeft(131), result.codeSpaceSize());
        assertEquals(BigInteger.ONE.shiftLeft(128), result.maskSpaceSize());
        assertEquals(BigInteger.ONE, result.attempts());
        assertFalse(result.searchComplete());
    }

    @Test
    void uniformSelectionUsesCompleteSortedMatchesAndOneDrawForEachPosition() {
        var first =
                generator.execute(
                        request(
                                AnalysisSettings.global(3),
                                new IntRange(3, 6),
                                EventConstraints.none(),
                                ProfileConstraints.none(),
                                Strategy.EXHAUSTIVE,
                                null,
                                8,
                                Selection.FIRST,
                                null));
        var expected = new ArrayList<>(words(first));
        Random reference = new Random(Long.MAX_VALUE);
        for (int i = 0; i < expected.size(); i++) {
            Collections.swap(expected, i, i + reference.nextInt(expected.size() - i));
        }
        var selected =
                generator.execute(
                        request(
                                AnalysisSettings.global(3),
                                new IntRange(3, 6),
                                EventConstraints.none(),
                                ProfileConstraints.none(),
                                Strategy.EXHAUSTIVE,
                                null,
                                8,
                                Selection.RANDOM_WITHOUT_REPLACEMENT,
                                Long.MAX_VALUE));
        assertEquals(expected, words(selected));
        assertEquals(8, new HashSet<>(words(selected)).size());
        assertTrue(selected.selectionPerformed());
        assertTrue(selected.searchComplete());
    }

    @Test
    void randomBudgetDuplicatesAndOrderingFollowSpecifiedRandomDraws() {
        var request =
                request(
                        AnalysisSettings.global(1),
                        new IntRange(3, 3),
                        EventConstraints.none(),
                        ProfileConstraints.none(),
                        Strategy.BOUNDED_RANDOM,
                        BigInteger.valueOf(10),
                        3,
                        Selection.FIRST,
                        42L);
        var result = generator.execute(request);
        assertEquals(BUDGET_EXHAUSTED, result.stopReason());
        assertEquals(BigInteger.TEN, result.attempts());
        assertEquals(BigInteger.TWO, result.uniqueCandidatesEvaluated());
        assertEquals(List.of("o", "x"), words(result));
        var again = generator.execute(request);
        assertEquals(words(result), words(again));
        assertFalse(result.searchComplete());
        var first =
                generator.execute(
                        request(
                                AnalysisSettings.global(4),
                                new IntRange(3, 7),
                                EventConstraints.none(),
                                ProfileConstraints.none(),
                                Strategy.BOUNDED_RANDOM,
                                BigInteger.TEN,
                                1,
                                Selection.FIRST,
                                42L));
        Random reference = new Random(42);
        StringBuilder expected = new StringBuilder();
        for (int i = 0; i < 4; i++) expected.append(reference.nextBoolean() ? 'x' : 'o');
        assertEquals(expected.toString(), words(first).getFirst());
        assertEquals(REQUESTED_COUNT_REACHED, first.stopReason());
        assertEquals(BigInteger.ONE, first.attempts());
    }

    @Test
    void exactProfilesPeaksDeviationAndCandidateContextReset() {
        var settings =
                new AnalysisSettings(
                        AnalysisFrame.unknown(16),
                        Scope.CAUSAL_WINDOWS,
                        Segmentation.equal(16, 4, 16),
                        "",
                        List.of(),
                        null);
        var profiles =
                new ProfileConstraints(
                        List.of(
                                new IntRange(8, 8),
                                new IntRange(8, 8),
                                new IntRange(3, 3),
                                new IntRange(3, 3)),
                        List.of(new IntRange(22, 22)),
                        new RationalRange(Rational.of(5, 2), Rational.of(5, 2)),
                        ProfileBasis.WINDOWS,
                        Set.of(1, 2),
                        ProfileBasis.WINDOWS);
        var r =
                generator.execute(
                        request(
                                settings,
                                new IntRange(22, 22),
                                quarters(16),
                                profiles,
                                Strategy.EXHAUSTIVE,
                                null,
                                3,
                                Selection.FIRST,
                                null));
        assertEquals(BigInteger.ONE, r.totalMatches());
        assertEquals(
                List.of(8, 8, 3, 3), r.rhythms().getFirst().primary().windowMetrics().values());
        assertEquals(words(r), words(generator.execute(r.request())));
    }

    @Test
    void invalidInputFailsBeforeAnalyzerAndInterruptionRetainsCorrectPartialStatus() {
        AtomicInteger calls = new AtomicInteger();
        var fake =
                new GenerateRhythmsByInformationUseCase(
                        (w, s) -> {
                            calls.incrementAndGet();
                            return new RhythmInformationAnalyzer().analyze(w, s);
                        });
        var bad =
                request(
                        AnalysisSettings.global(4),
                        new IntRange(3, 7),
                        new EventConstraints(null, null, Set.of(1), Set.of(1), null, null),
                        ProfileConstraints.none(),
                        Strategy.EXHAUSTIVE,
                        null,
                        1,
                        Selection.FIRST,
                        null);
        assertThrows(IllegalArgumentException.class, () -> fake.execute(bad));
        assertEquals(0, calls.get());
        var valid =
                request(
                        AnalysisSettings.global(4),
                        new IntRange(3, 7),
                        EventConstraints.none(),
                        ProfileConstraints.none(),
                        Strategy.EXHAUSTIVE,
                        null,
                        20,
                        Selection.FIRST,
                        null);
        var stopped = fake.execute(valid, () -> calls.get() >= 3);
        assertEquals(INTERRUPTED, stopped.stopReason());
        assertFalse(stopped.searchComplete());
        assertEquals(BigInteger.valueOf(3), stopped.attempts());
        assertEquals(3, stopped.returnedCount());
    }

    @Test
    void linearGapAndZeroRunConstraintsIncludeBothEdgesAndRejectContradictionsEarly() {
        var events = new EventConstraints(new IntRange(2, 2), null, Set.of(), Set.of(), 2, 1);
        var result =
                generator.execute(
                        request(
                                AnalysisSettings.global(4),
                                new IntRange(3, 7),
                                events,
                                ProfileConstraints.none(),
                                Strategy.EXHAUSTIVE,
                                null,
                                20,
                                Selection.FIRST,
                                null));
        assertEquals(List.of("oxox", "xoxo"), words(result));
        for (var impossible :
                List.of(
                        new EventConstraints(new IntRange(0, 0), null, Set.of(), Set.of(), null, 3),
                        new EventConstraints(null, null, Set.of(0, 1), Set.of(), 2, null),
                        new EventConstraints(null, null, Set.of(), Set.of(1), null, 0),
                        new EventConstraints(
                                null,
                                new RationalRange(Rational.of(1, 8), Rational.of(1, 8)),
                                Set.of(),
                                Set.of(),
                                null,
                                null))) {
            var rejecting =
                    new GenerateRhythmsByInformationUseCase(
                            (w, s) -> {
                                fail(
                                        "Contradictory event conditions must fail before candidate"
                                            + " analysis");
                                return null;
                            });
            assertThrows(
                    IllegalArgumentException.class,
                    () ->
                            rejecting.execute(
                                    request(
                                            AnalysisSettings.global(4),
                                            new IntRange(3, 7),
                                            impossible,
                                            ProfileConstraints.none(),
                                            Strategy.EXHAUSTIVE,
                                            null,
                                            20,
                                            Selection.FIRST,
                                            null)));
        }
    }

    @Test
    void rangeProfilesAndAdditionalGlobalTargetRetainSeparateMeasurementKeys() {
        var settings =
                new AnalysisSettings(
                        AnalysisFrame.unknown(8),
                        Scope.ISOLATED_WINDOWS,
                        Segmentation.equal(8, 4, 8),
                        "",
                        List.of(),
                        null);
        var profiles =
                new ProfileConstraints(
                        List.of(new IntRange(6, 7), new IntRange(6, 7)),
                        List.of(new IntRange(14, 14)),
                        new RationalRange(Rational.ZERO, Rational.ZERO),
                        ProfileBasis.BARS,
                        Set.of(1),
                        ProfileBasis.WINDOWS);
        var result =
                generator.execute(
                        new GenerationRequest(
                                settings,
                                new IntRange(14, 14),
                                new IntRange(10, 10),
                                EventConstraints.none(),
                                profiles,
                                false,
                                Strategy.EXHAUSTIVE,
                                null,
                                20,
                                Selection.FIRST,
                                null));
        assertEquals(List.of("oxoxoxox", "xoxoxoxo"), words(result));
        for (var rhythm : result.rhythms()) {
            assertEquals(14, rhythm.primary().totalBits());
            assertEquals(10, rhythm.additionalGlobal().totalBits());
            assertEquals(Scope.ISOLATED_WINDOWS, rhythm.primary().measurementKey().scope());
            assertEquals(
                    Scope.GLOBAL_DESCRIPTION, rhythm.additionalGlobal().measurementKey().scope());
        }
    }

    @Test
    void exactDensityEdgesAndNoCyclicGapAreNotRounded() {
        var events =
                new EventConstraints(
                        null,
                        new RationalRange(Rational.of(1, 4), Rational.of(1, 4)),
                        Set.of(),
                        Set.of(),
                        10,
                        null);
        var r =
                generator.execute(
                        request(
                                AnalysisSettings.global(4),
                                new IntRange(3, 7),
                                events,
                                ProfileConstraints.none(),
                                Strategy.EXHAUSTIVE,
                                null,
                                20,
                                Selection.FIRST,
                                null));
        assertEquals(
                BigInteger.valueOf(4), r.totalMatches()); // one onset: no neighbor gap to reject
        assertTrue(r.rhythms().stream().allMatch(a -> a.primary().sequence().onsetCount() == 1));
    }

    private List<String> words(GenerationResult r) {
        return r.rhythms().stream().map(a -> a.primary().sequence().normalizedOnsets()).toList();
    }
}
