package syrincs.b_application;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import syrincs.a_domain.chord.NoteCombinator;
import syrincs.a_domain.hindemith.ChordAnalysis;
import syrincs.a_domain.rhythm.FakeMidiOutputPort;
import syrincs.a_domain.rhythm.HuffmanRhythm;
import syrincs.b_application.ports.dto.DeviationRange;

import java.util.List;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

class UseCaseInteractorRhythmSelectionTest {
    private static final HuffmanRhythm LOW = rhythm("xooo xoxo xooo xoxo");
    private static final HuffmanRhythm HIGH = rhythm("xoxo xooo xoxo xooo");
    private static final HuffmanRhythm SILENCE = rhythm("oooo ".repeat(4));

    @Test
    void legacyOverloadsKeepStrictDefaultAndExcludeExactEquality() throws Exception {
        // Stored metadata may differ from reconstructed aggregates in older catalogs.
        var repo = new FilteringRhythmRepository(
                new FilteringRhythmRepository.Entry(LOW, 3, 0.7),
                new FilteringRhythmRepository.Entry(HIGH, 3, 0.8));
        var interactor = new CapturingInteractor(repo);

        interactor.playRhythmsByInformationGrades(List.of(3));
        assertEquals(List.of(HIGH), interactor.selected);
        interactor.playRhythmsByInformationGrades(List.of(3), "Virtual Out");
        assertEquals(List.of(HIGH), interactor.selected);
        assertEquals("Virtual Out", interactor.device);
        assertEquals(2, repo.strictQueries);
        assertEquals(0, repo.rangeQueries);
        assertEquals(AppDefaults.MIN_HUFFMAN_RHYTHM_DEVIATION, repo.lastStrictMinimum);

        interactor.playRhythmsByInformationGrades(List.of(3), null, new DeviationRange(0.7, 0.7));
        assertEquals(List.of(LOW), interactor.selected);
        assertEquals(1, repo.rangeQueries);
    }

    @ParameterizedTest
    @MethodSource("inclusiveRanges")
    void explicitRangesReplaceDefaultAndActuallyFilterGradeAndBounds(
            DeviationRange range, int grade, HuffmanRhythm expected) throws Exception {
        var repo = new FilteringRhythmRepository(List.of(LOW, HIGH, SILENCE,
                rhythm("xoxo xooo xooo xooo")));
        var interactor = new CapturingInteractor(repo);

        interactor.playRhythmsByInformationGrades(List.of(grade), "Virtual Out", range);

        assertEquals(List.of(expected), interactor.selected);
        assertEquals(1, interactor.plays);
        assertEquals("Virtual Out", interactor.device);
        assertEquals(range, repo.lastRange);
        assertEquals(grade, repo.lastInformation);
        assertEquals(0, repo.strictQueries);
        assertEquals(1, repo.rangeQueries);
    }

    static Stream<Arguments> inclusiveRanges() {
        double low = LOW.getStandardDeviation(), high = HIGH.getStandardDeviation();
        return Stream.of(
                Arguments.of(new DeviationRange(high, null), 3, HIGH),
                Arguments.of(new DeviationRange(null, low), 3, LOW),
                Arguments.of(new DeviationRange(low, low), 3, LOW),
                Arguments.of(new DeviationRange(high, high), 3, HIGH),
                Arguments.of(new DeviationRange(0.0, null), 0, SILENCE),
                Arguments.of(new DeviationRange(null, 0.0), 0, SILENCE),
                Arguments.of(new DeviationRange(null, null), 0, SILENCE));
    }

    @Test
    void unknownStoredDeviationOnlyMatchesWithoutBounds() {
        var repo = new FilteringRhythmRepository(new FilteringRhythmRepository.Entry(LOW, 3, null));
        assertEquals(List.of(LOW), repo.getAllByInformationAndDeviationRange(3, new DeviationRange(null, null)));
        assertTrue(repo.getAllByInformationAndDeviationRange(3, new DeviationRange(0.0, null)).isEmpty());
        assertTrue(repo.getAllByInformationAndDeviationRange(3, new DeviationRange(null, 1.0)).isEmpty());
        assertTrue(repo.getAllByInformationAndDeviationRange(3, new DeviationRange(0.0, 1.0)).isEmpty());
        assertTrue(repo.getAllByInformationAndMinDeviation(3, 0.7).isEmpty());
    }

    @Test
    void missingGradesStillSkipAndPreserveOrderOfSuccessfulSelections() throws Exception {
        var repo = new FilteringRhythmRepository(List.of(LOW, SILENCE));
        var interactor = new CapturingInteractor(repo);
        interactor.playRhythmsByInformationGrades(List.of(99, 3, 0, 3), null, new DeviationRange(0.0, null));
        assertEquals(List.of(LOW, SILENCE, LOW), interactor.selected);
        assertEquals(1, interactor.plays);
        assertEquals(4, repo.rangeQueries);
    }

    @Test
    void noMatchesReportChosenBoundsWithoutPlayback() {
        var interactor = new CapturingInteractor(new FilteringRhythmRepository(List.of(LOW)));
        var error = assertThrows(IllegalStateException.class, () ->
                interactor.playRhythmsByInformationGrades(List.of(3), null, new DeviationRange(1.0, 2.0)));
        assertTrue(error.getMessage().contains("--deviation-min 1.0 --deviation-max 2.0"));
        assertEquals(0, interactor.plays);
    }

    @Test
    void repositoryFailureDoesNotBecomeAnEmptyResultOrPlayback() {
        var repo = new FilteringRhythmRepository(List.of()) {
            @Override public List<HuffmanRhythm> getAllByInformationAndDeviationRange(Integer info, DeviationRange range) {
                throw new IllegalStateException("database unavailable");
            }
        };
        var interactor = new CapturingInteractor(repo);
        var error = assertThrows(IllegalStateException.class, () ->
                interactor.playRhythmsByInformationGrades(List.of(3), null, new DeviationRange(0.0, null)));
        assertEquals("database unavailable", error.getMessage());
        assertEquals(0, interactor.plays);
    }

    private static HuffmanRhythm rhythm(String onsets) { return new HuffmanRhythm(4, 4, 120, onsets); }

    private static final class CapturingInteractor extends UseCaseInteractor {
        List<HuffmanRhythm> selected;
        String device;
        int plays;

        CapturingInteractor(FilteringRhythmRepository repo) { this(repo, new FakeHindemithChordRepository()); }

        private CapturingInteractor(FilteringRhythmRepository repo, FakeHindemithChordRepository chords) {
            super(new SendToMidiUseCase(new FakeMidiOutputPort()), new ValidatePatternsUseCase(),
                    new PlaybackRhythmUseCase((pattern, spec, voices) -> {}), new AnalyseRhythmUseCase(),
                    chords, new GenerateChordsUseCase(new NoteCombinator(), new ChordAnalysis(), 3),
                    new AnalyseChordByHindemithUseCase(), new GetHindemithChordsFromDbUseCase(chords),
                    new PersistHindemithChordUseCase(chords), null, null, repo);
        }

        @Override public void playRhythms(List<HuffmanRhythm> rhythms, String device) {
            selected = List.copyOf(rhythms);
            this.device = device;
            plays++;
        }
    }
}
