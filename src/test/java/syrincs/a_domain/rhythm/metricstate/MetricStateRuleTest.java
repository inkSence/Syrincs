package syrincs.a_domain.rhythm.metricstate;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/** One targeted test per row of the transition table (rows 1-10, 5a-G, 5b-G, 4-G). */
class MetricStateRuleTest {
    private static final MetricStateAnalyzer ANALYZER = new MetricStateAnalyzer();
    private static final SoundCondition I = SoundCondition.IMPULSE;
    private static final SoundCondition G = SoundCondition.SUSTAINED;

    private static MetricStateAnalysis run(SoundCondition sound, int beat, int bar, String events) {
        return ANALYZER.analyze(MetricExample.parse(new MetricFrame(beat, bar, sound), events), 64);
    }

    private static Set<List<String>> sequences(MetricStateAnalysis a) {
        return a.minimalSequenceCodes();
    }

    private static List<String> only(MetricStateAnalysis a) {
        assertEquals(1, a.minimalSequenceCodes().size(), "expected a single minimal sequence");
        return a.minimalSequenceCodes().iterator().next();
    }

    private static ProtocolStep step(MetricInterpretation path, int position) {
        return path.protocol().get(position);
    }

    private static MetricState n(long delta, int anchor, long expectation) {
        return MetricState.notes(BigInteger.valueOf(delta), anchor, BigInteger.valueOf(expectation));
    }

    private static MetricState p(long delta, int anchor) {
        return MetricState.rests(BigInteger.valueOf(delta), anchor);
    }

    private static Set<TransitionRule> rulesAt(MetricStateAnalysis a, int position) {
        return a.minimalPaths().stream()
                .map(path -> step(path, position).rule())
                .collect(Collectors.toSet());
    }

    // ---- row 1 and 2: beginning ---------------------------------------------------------------

    @Test
    void row1_onsetOnTheOneCostsTenAndStartsNotes() {
        MetricStateAnalysis a = run(I, 4, 16, "xooo xooo xooo xooo");
        assertEquals(2, a.valueBits());
        ProtocolStep first = step(a.minimalPaths().get(0), 0);
        assertEquals(TransitionRule.START_WITH_ONSET, first.rule());
        assertEquals(List.of(Instruction.PLAY_NOTES), first.instructions());
        assertNull(first.stateBefore());
        assertEquals(n(4, 0, 4), first.stateAfter());
    }

    @Test
    void row2_leadingSilenceCostsEleven() {
        MetricStateAnalysis a = run(I, 4, 16, "oooo oooo oooo oooo");
        assertEquals(2, a.valueBits());
        assertEquals(List.of("11"), only(a));
        ProtocolStep first = step(a.minimalPaths().get(0), 0);
        assertEquals(TransitionRule.START_WITHOUT_ONSET, first.rule());
        assertEquals(p(4, 0), first.stateAfter());
    }

    // ---- row 3 and 4 --------------------------------------------------------------------------

    @Test
    void row3_onsetAtExpectationIsFreeAndBecomesAnchor() {
        MetricStateAnalysis a = run(I, 4, 16, "xooo xooo xooo xooo");
        MetricInterpretation path = a.minimalPaths().get(0);
        for (int t : new int[] {4, 8, 12}) {
            ProtocolStep s = step(path, t);
            assertEquals(TransitionRule.ONSET_AT_EXPECTATION, s.rule());
            assertEquals(0, s.costBits());
            assertEquals(n(4, t, t + 4), s.stateAfter());
        }
    }

    @Test
    void row4_sixteenthsFromQuartersCostTwoRefinements() {
        MetricStateAnalysis a = run(I, 4, 4, "xxxx");
        assertEquals(6, a.valueBits());
        assertEquals(List.of("10", "00", "00"), only(a));
        ProtocolStep s = step(a.minimalPaths().get(0), 1);
        assertEquals(TransitionRule.ONSET_BEFORE_EXPECTATION, s.rule());
        assertEquals(List.of(Instruction.REFINE, Instruction.REFINE), s.instructions());
        assertEquals(n(1, 1, 2), s.stateAfter());
        // The following onsets sit on the new expectation and are free (row 3).
        assertEquals(TransitionRule.ONSET_AT_EXPECTATION, step(a.minimalPaths().get(0), 2).rule());
    }

    @Test
    void row4_singleRefinementForAnEighth() {
        MetricStateAnalysis a = run(I, 4, 4, "xoxo");
        assertEquals(List.of("10", "00"), only(a));
    }

    @Test
    void row4_beatOfOneSlotNeverRefinesBelowTheGrid() {
        // beatSlots = 1: every onset lies on the grid; the missing onset at 2 is cheaper as 01.
        MetricStateAnalysis a = run(I, 1, 4, "xxox");
        assertEquals(Set.of(List.of("10", "01")), sequences(a));
    }

    // ---- rows 5a and 5b -----------------------------------------------------------------------

    @Test
    void row5a_missingOnsetCanBePlayRests() {
        MetricStateAnalysis a = run(I, 4, 16, "xooo oooo oooo oooo");
        assertEquals(Set.of(List.of("10", "11")), sequences(a));
        ProtocolStep s = step(a.minimalPaths().get(0), 4);
        assertEquals(TransitionRule.MISSING_ONSET_PLAY_RESTS, s.rule());
        assertEquals(n(4, 0, 4), s.stateBefore());
        assertEquals(p(4, 0), s.stateAfter());
    }

    @Test
    void row5b_missingOnsetCanDoubleTheSpacing() {
        MetricStateAnalysis a = run(I, 4, 16, "xooo oooo xooo xooo");
        MetricInterpretation viaCoarsen =
                a.minimalPaths().stream()
                        .filter(path -> step(path, 4).rule() == TransitionRule.MISSING_ONSET_COARSEN)
                        .findFirst()
                        .orElseThrow();
        ProtocolStep s = step(viaCoarsen, 4);
        assertEquals(List.of(Instruction.COARSEN), s.instructions());
        assertEquals(n(4, 0, 4), s.stateBefore());
        assertEquals(n(8, 0, 8), s.stateAfter());
    }

    @Test
    void rows5aAnd5b_bothWaysAreKept() {
        MetricStateAnalysis a = run(I, 4, 16, "xooo xooo xooo oooo");
        assertEquals(Set.of(List.of("10", "01"), List.of("10", "11")), sequences(a));
        assertEquals(BigInteger.TWO, a.minimalPathCount());
        assertEquals(
                Set.of(TransitionRule.MISSING_ONSET_PLAY_RESTS, TransitionRule.MISSING_ONSET_COARSEN),
                rulesAt(a, 12));
        assertTrue(a.isAmbiguous());
    }

    @Test
    void row5b_expectationAfterDoublingIsAnchorPlusNewDelta() {
        // x at 0, silence: 01 gives Δ=8 and e=8; the onset at 8 is then free.
        MetricStateAnalysis a = run(I, 4, 16, "xooo oooo xooo oooo");
        assertEquals(4, a.valueBits());
        assertEquals(Set.of(List.of("10", "01")), sequences(a));
        assertEquals(TransitionRule.ONSET_AT_EXPECTATION, step(a.minimalPaths().get(0), 8).rule());
    }

    // ---- rows 6, 7, 8 -------------------------------------------------------------------------

    @Test
    void row6_longSilenceInRestsCostsNothing() {
        MetricStateAnalysis a = run(I, 4, 16, "xooo" + "oooo".repeat(31));
        assertEquals(4, a.valueBits());
        assertEquals(Set.of(List.of("10", "11")), sequences(a));
        MetricStateAnalysis allSilent = run(I, 4, 16, "oooo".repeat(32));
        assertEquals(2, allSilent.valueBits());
        for (int t = 1; t < 128; t++)
            assertEquals(
                    TransitionRule.SILENCE_IN_RESTS,
                    step(allSilent.minimalPaths().get(0), t).rule(),
                    "position " + t);
    }

    @Test
    void row7_onsetOnTheSilentGridCostsOnlyTen() {
        MetricStateAnalysis a = run(I, 4, 8, "oooo xooo");
        assertEquals(List.of("11", "10"), only(a));
        ProtocolStep s = step(a.minimalPaths().get(0), 4);
        assertEquals(TransitionRule.ONSET_ON_SILENT_GRID, s.rule());
        assertEquals(p(4, 0), s.stateBefore());
        assertEquals(n(4, 4, 8), s.stateAfter());
    }

    @Test
    void row8_onsetBesideTheSilentGridRefinesFirstThenTen() {
        MetricStateAnalysis a = run(I, 4, 4, "ooxo");
        assertEquals(6, a.valueBits());
        assertEquals(List.of("11", "00", "10"), only(a));
        ProtocolStep s = step(a.minimalPaths().get(0), 2);
        assertEquals(TransitionRule.ONSET_OFF_SILENT_GRID, s.rule());
        assertEquals(List.of(Instruction.REFINE, Instruction.PLAY_NOTES), s.instructions());
        assertEquals(n(2, 2, 4), s.stateAfter());
    }

    @Test
    void row8_twoRefinementsForASixteenthOffset() {
        MetricStateAnalysis a = run(I, 4, 4, "oxxx");
        assertEquals(List.of("11", "00", "00", "10"), only(a));
    }

    // ---- row 9: bar change --------------------------------------------------------------------

    @Test
    void row9_barChangeResetsAnchorAndExpectsTheOneInNotes() {
        MetricStateAnalysis a = run(I, 4, 16, "xooo xooo xooo xooo xooo xooo xooo xooo");
        MetricInterpretation path = a.minimalPaths().get(0);
        ProtocolStep s = step(path, 16);
        assertTrue(s.barStart());
        assertEquals(n(4, 16, 16), s.stateBefore());
        assertEquals(TransitionRule.ONSET_AT_EXPECTATION, s.rule());
        assertEquals(0, s.costBits());
        assertEquals(2, a.valueBits());
        assertEquals(List.of(2, 2), a.valueAfterBars());
        assertEquals(List.of(2, 0), costBars(a));
    }

    @Test
    void row9_barChangeInRestsOnlyMovesTheAnchor() {
        MetricStateAnalysis a = run(I, 4, 16, "oooo".repeat(8));
        ProtocolStep s = step(a.minimalPaths().get(0), 16);
        assertTrue(s.barStart());
        assertEquals(p(4, 16), s.stateBefore());
        assertEquals(p(4, 16), s.stateAfter());
        assertEquals(0, s.costBits());
        // An onset two slots after the new bar start is measured from the bar start.
        MetricStateAnalysis b = run(I, 4, 8, "oooo oooo oooo xooo");
        assertEquals(List.of("11", "10"), only(b));
        ProtocolStep onset = step(b.minimalPaths().get(0), 12);
        assertEquals(TransitionRule.ONSET_ON_SILENT_GRID, onset.rule());
        assertEquals(p(4, 8), onset.stateBefore());
    }

    @Test
    void row9_shiftedQuarterPulseOverTwoBarsPaysTheDownbeatInEveryBar() {
        MetricStateAnalysis a = run(I, 4, 16, "ooxo ooxo ooxo ooxo ".repeat(2));
        assertEquals(16, a.valueBits());
        assertEquals(List.of(8, 16), a.valueAfterBars());
        assertEquals(List.of(8, 8), costBars(a));
        assertEquals(
                Set.of(
                        List.of("11", "00", "10", "01", "11", "00", "10", "01"),
                        List.of("11", "00", "10", "01", "01", "00", "00", "01")),
                sequences(a));
        for (MetricInterpretation path : a.minimalPaths()) {
            ProtocolStep s = step(path, 16);
            assertTrue(s.barStart());
            // The expectation 18 (last onset at 14 plus Δ=4) has expired: the one is expected.
            assertEquals(PlayMode.NOTES, s.stateBefore().mode());
            assertEquals(16, s.stateBefore().anchor());
            assertEquals(BigInteger.valueOf(16), s.stateBefore().expectation());
            assertTrue(s.costBits() > 0, "the missing downbeat is paid");
        }
    }

    @Test
    void row9_returnToTheOneIsFreeAfterAShiftedBar() {
        MetricStateAnalysis a = run(I, 4, 16, "ooxo ooxo ooxo ooxo xooo xooo xooo xooo");
        assertEquals(8, a.valueBits());
        assertEquals(List.of(8, 8), a.valueAfterBars());
        assertEquals(List.of(8, 0), costBars(a));
        assertEquals(TransitionRule.ONSET_AT_EXPECTATION, step(a.minimalPaths().get(0), 16).rule());
    }

    @Test
    void row9_laterBarCanChangeTheCheapestReadingOfAnEarlierBar() {
        // Guiding principle 2: F05.2 alone is 4 (10 11); with a second bar it is 6 (10 01 01).
        MetricStateAnalysis a = run(I, 4, 16, "xooo oooo oooo oooo xooo oooo oooo oooo");
        assertEquals(List.of(4, 6), a.valueAfterBars());
        assertEquals(Set.of(List.of("10", "01", "01")), sequences(a));
        assertEquals(List.of(6, 0), costBars(a));
    }

    // ---- row 10: end ---------------------------------------------------------------------------

    @Test
    void row10_endCostsNothingAndExpectationsBeyondItAreNotEvaluated() {
        // The expectation at 4 lies beyond the end of the one-slot-per-position example.
        MetricStateAnalysis a = run(I, 4, 4, "xooo");
        assertEquals(List.of("10"), only(a));
        assertEquals(2, a.valueBits());
    }

    // ---- G rows -------------------------------------------------------------------------------

    @Test
    void row5a_G_restOnsetAtExpectationPlaysRests() {
        MetricStateAnalysis a = run(G, 4, 16, "x=== x=== oooo oooo");
        assertEquals(List.of("10", "11"), only(a));
        ProtocolStep s = step(a.minimalPaths().get(0), 8);
        assertEquals(EventKind.REST_ONSET, s.event());
        assertEquals(TransitionRule.REST_ONSET_AT_EXPECTATION, s.rule());
        assertEquals(p(4, 8), s.stateAfter());
    }

    @Test
    void row5b_G_heldToneAtExpectationDoublesTheSpacing() {
        MetricStateAnalysis a = run(G, 4, 16, "x=== ==== oooo oooo");
        assertEquals(List.of("10", "01", "11"), only(a));
        ProtocolStep hold = step(a.minimalPaths().get(0), 4);
        assertEquals(EventKind.HOLD, hold.event());
        assertEquals(TransitionRule.HELD_TONE_AT_EXPECTATION, hold.rule());
        assertEquals(n(8, 0, 8), hold.stateAfter());
        ProtocolStep end = step(a.minimalPaths().get(0), 8);
        assertEquals(TransitionRule.REST_ONSET_AT_EXPECTATION, end.rule());
    }

    @Test
    void row4_G_restOnsetBeforeExpectationRefinesThenPlaysRests() {
        MetricStateAnalysis a = run(G, 4, 4, "x=oo");
        assertEquals(List.of("10", "00", "11"), only(a));
        ProtocolStep s = step(a.minimalPaths().get(0), 2);
        assertEquals(TransitionRule.REST_ONSET_BEFORE_EXPECTATION, s.rule());
        assertEquals(List.of(Instruction.REFINE, Instruction.PLAY_RESTS), s.instructions());
        // The rest onset becomes the anchor (silent grid counts from it).
        assertEquals(p(2, 2), s.stateAfter());
    }

    @Test
    void row3_G_seamlessRetriggerAtExpectationIsFree() {
        MetricStateAnalysis a = run(G, 4, 16, "x=== x=== x=== x===");
        assertEquals(List.of("10"), only(a));
        assertEquals(TransitionRule.ONSET_AT_EXPECTATION, step(a.minimalPaths().get(0), 4).rule());
    }

    @Test
    void heldToneOverTheDownbeatCostsCoarsen() {
        MetricStateAnalysis a = run(G, 4, 8, "x=== ==== ==== ====");
        assertEquals(List.of("10", "01", "01"), only(a));
        ProtocolStep s = step(a.minimalPaths().get(0), 8);
        assertTrue(s.barStart());
        assertEquals(EventKind.HOLD, s.event());
        assertEquals(TransitionRule.HELD_TONE_AT_EXPECTATION, s.rule());
        assertEquals(List.of(Instruction.COARSEN), s.instructions());
        assertEquals(n(8, 8, 8), s.stateBefore());
        assertEquals(n(16, 8, 24), s.stateAfter());
    }

    @Test
    void tieAcrossTheBarLineThenRestOnsetBeforeExpectation() {
        // 0:o 11 | 1:x 00 10 | 2,3: hold at e: 01 01 | 4: bar, hold at e: 01 | 6: rest onset 00 00 11.
        MetricStateAnalysis a = run(G, 2, 4, "ox== ==oo");
        assertEquals(
                List.of("11", "00", "10", "01", "01", "01", "00", "00", "11"), only(a));
        assertEquals(18, a.valueBits());
    }

    @Test
    void sustainedNeverBranchesBetween5aAnd5b() {
        List<String> examples =
                List.of(
                        "x=== ==== oooo oooo",
                        "x=== x=== x=== ====",
                        "oooo oooo x=== ====",
                        "xooo xooo oooo oooo",
                        "x=oo x=== ==oo xxx=",
                        "ox== ==oo oooo oooo");
        for (String events : examples) {
            MetricStateAnalysis a = run(G, 4, 16, events);
            assertEquals(BigInteger.ONE, a.minimalPathCount(), events);
            for (ProtocolStep s : a.minimalPaths().get(0).protocol()) {
                assertNotEquals(TransitionRule.MISSING_ONSET_PLAY_RESTS, s.rule(), events);
                assertNotEquals(TransitionRule.MISSING_ONSET_COARSEN, s.rule(), events);
            }
        }
    }

    @Test
    void xWithoutHoldSoundsExactlyOnePosition() {
        // x at 0 is a one-position tone: its end is heard at 1 as a rest onset before e = 4.
        MetricStateAnalysis a = run(G, 4, 4, "xooo");
        assertEquals(List.of("10", "00", "00", "11"), only(a));
        assertEquals(EventKind.REST_ONSET, step(a.minimalPaths().get(0), 1).event());
    }

    @Test
    void sameNotationUnderBothConditionsCanDifferInValue() {
        // Half note with half rest: I pays 10 11, G additionally pays 01 for the held tone.
        assertEquals(4, run(I, 4, 16, "xooo oooo oooo oooo").valueBits());
        assertEquals(6, run(G, 4, 16, "x=== ==== oooo oooo").valueBits());
    }

    // ---- result object ------------------------------------------------------------------------

    @Test
    void displayLimitDoesNotChangeCountOrSequences() {
        MetricExample example = MetricExample.parse(new MetricFrame(4, 16, I), "xooo xooo xooo oooo");
        MetricStateAnalysis limited = ANALYZER.analyze(example, 1);
        assertEquals(1, limited.minimalPaths().size());
        assertTrue(limited.minimalPathsTruncated());
        assertEquals(BigInteger.TWO, limited.minimalPathCount());
        assertEquals(2, limited.minimalSequences().size());
        assertEquals(0, ANALYZER.analyze(example, 0).minimalPaths().size());
        assertEquals(
                new MetricStateMeasurementKey("metric-state-v1", I, 4, 16), limited.key());
    }

    @Test
    void beatAndBarCostRangesSpanAllMinimalPaths() {
        MetricStateAnalysis a = run(I, 4, 16, "xooo xooo xooo oooo");
        assertEquals(4, a.beatCosts().size());
        assertEquals(new CostRange(0, 0, 4, 2, 2), a.beatCosts().get(0));
        assertEquals(new CostRange(3, 12, 16, 2, 2), a.beatCosts().get(3));
        assertEquals(new CostRange(0, 0, 16, 4, 4), a.barCosts().get(0));
    }

    private static List<Integer> costBars(MetricStateAnalysis a) {
        List<Integer> list = new ArrayList<>();
        for (CostRange range : a.barCosts()) {
            assertTrue(range.isUnambiguous(), "bar range " + range);
            list.add(range.minBits());
        }
        return list;
    }
}
