package syrincs.a_domain.rhythm.information;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

import java.util.List;

class MetricReferenceAssessmentTest {
    private MetricReference fourFour() {
        return new MetricReference(
                "4/4",
                16,
                0,
                "beat",
                List.of(
                        new MetricReference.Level(
                                "subdivision",
                                MetricReference.LevelKind.SUBDIVISION,
                                java.util.stream.IntStream.range(0, 16).boxed().toList()),
                        new MetricReference.Level(
                                "beat", MetricReference.LevelKind.BEAT, List.of(0, 4, 8, 12)),
                        new MetricReference.Level(
                                "bar", MetricReference.LevelKind.CYCLE_MARKER, List.of(0))),
                MetricReference.Provenance.SUPPLIED,
                "test");
    }

    @Test
    void exactClosedBoundariesAndUnknownTime() {
        var r = fourFour();
        assertEquals(ReferenceAssessment.Status.PASS, assess(r, Rational.of(100)).status());
        assertEquals(ReferenceAssessment.Status.FAIL, assess(r, Rational.of(99999, 1000)).status());
        assertEquals(ReferenceAssessment.Status.PASS, assess(r, Rational.of(300)).status());
        assertEquals(
                ReferenceAssessment.Status.FAIL, assess(r, Rational.of(300001, 1000)).status());
        assertEquals(ReferenceAssessment.Status.NOT_EVALUABLE, assess(r, null).status());
        assertEquals(
                ReferenceAssessment.Status.NOT_EVALUABLE, assess(null, Rational.of(125)).status());
    }

    @Test
    void nonisochronousReferenceAndPhaseAreExplicit() {
        var r =
                new MetricReference(
                        "unequal",
                        8,
                        1,
                        "beat",
                        List.of(
                                new MetricReference.Level(
                                        "beat", MetricReference.LevelKind.BEAT, List.of(0, 3, 5))),
                        MetricReference.Provenance.CANDIDATE,
                        "explicit");
        var a = assess(r, Rational.of(200));
        assertEquals(ReferenceAssessment.Status.PASS, a.status());
        assertEquals(List.of(1, 4, 6, 9, 12, 14), a.observedLevels().getFirst().positions());
    }

    @Test
    void technicalRasterDoesNotBecomeFastestActiveLevel() {
        var r =
                new MetricReference(
                        "50ms",
                        80,
                        0,
                        "beat",
                        List.of(
                                new MetricReference.Level(
                                        "beat",
                                        MetricReference.LevelKind.BEAT,
                                        List.of(0, 10, 20, 30, 40, 50, 60, 70))),
                        MetricReference.Provenance.SUPPLIED,
                        "test");
        assertEquals(ReferenceAssessment.Status.PASS, assess(r, Rational.of(50)).status());
    }

    @Test
    void fiveSecondCycleBoundaryIsSeparateFromBeatBounds() {
        var r =
                new MetricReference(
                        "five",
                        50,
                        0,
                        "beat",
                        List.of(
                                new MetricReference.Level(
                                        "beat",
                                        MetricReference.LevelKind.BEAT,
                                        List.of(0, 10, 20, 30, 40))),
                        MetricReference.Provenance.SUPPLIED,
                        "test");
        assertEquals(ReferenceAssessment.Status.PASS, assess(r, Rational.of(100)).status());
        var failed = assess(r, Rational.of(100001, 1000));
        assertEquals(ReferenceAssessment.Status.FAIL, failed.status());
        assertTrue(failed.checks().get(1).passed());
        assertFalse(failed.checks().get(2).passed());
    }

    private ReferenceAssessment assess(MetricReference r, Rational time) {
        return ReferenceAssessment.assess(
                r,
                new AnalysisFrame(16, time, null, null, null, null),
                ReferenceAssessment.TIME_PROFILE);
    }
}
