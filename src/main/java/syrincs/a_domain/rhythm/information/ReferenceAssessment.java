package syrincs.a_domain.rhythm.information;

import java.util.ArrayList;
import java.util.List;

public record ReferenceAssessment(
        MetricReference reference,
        String profile,
        Status status,
        String reason,
        List<Check> checks,
        List<ObservedLevel> observedLevels) {
    public static final String TIME_PROFILE = "london-time-window-v1";

    public enum Status {
        PASS,
        FAIL,
        NOT_EVALUABLE
    }

    public record Check(String condition, List<Rational> durationsMs, boolean passed) {
        public Check {
            durationsMs = List.copyOf(durationsMs);
        }
    }

    public record ObservedLevel(String id, List<Integer> positions) {
        public ObservedLevel {
            positions = List.copyOf(positions);
        }
    }

    public ReferenceAssessment {
        checks = List.copyOf(checks);
        observedLevels = List.copyOf(observedLevels);
    }

    public static ReferenceAssessment assess(
            MetricReference reference, AnalysisFrame frame, String profile) {
        if (profile != null && !profile.equals(TIME_PROFILE))
            throw new IllegalArgumentException("Unsupported reference profile: " + profile);
        if (reference == null)
            return new ReferenceAssessment(
                    null, profile, Status.NOT_EVALUABLE, "ABSENT_REFERENCE", List.of(), List.of());
        List<ObservedLevel> observed =
                reference.levels().stream()
                        .map(l -> new ObservedLevel(l.id(), reference.inWindow(l, frame.slots())))
                        .toList();
        if (profile == null || frame.slotDurationMs() == null)
            return new ReferenceAssessment(
                    reference,
                    profile,
                    Status.NOT_EVALUABLE,
                    profile == null ? "NO_PROFILE_REQUESTED" : "UNKNOWN_SLOT_DURATION",
                    List.of(),
                    observed);
        List<Rational> fastest =
                gaps(
                        reference.levels().getFirst().positions(),
                        reference.cycleSlots(),
                        frame.slotDurationMs());
        List<Rational> beat =
                gaps(reference.beat().positions(), reference.cycleSlots(), frame.slotDurationMs());
        Rational cycle = frame.slotDurationMs().multiply(reference.cycleSlots());
        List<Check> checks =
                List.of(
                        new Check(
                                "FASTEST_GAPS_AT_LEAST_100_MS",
                                fastest,
                                fastest.stream().allMatch(d -> d.compareTo(Rational.of(100)) >= 0)),
                        new Check(
                                "BEAT_GAPS_400_TO_1200_MS",
                                beat,
                                beat.stream()
                                        .allMatch(
                                                d ->
                                                        d.compareTo(Rational.of(400)) >= 0
                                                                && d.compareTo(Rational.of(1200))
                                                                        <= 0)),
                        new Check(
                                "CYCLE_AT_MOST_5000_MS",
                                List.of(cycle),
                                cycle.compareTo(Rational.of(5000)) <= 0));
        boolean passed = checks.stream().allMatch(Check::passed);
        return new ReferenceAssessment(
                reference,
                profile,
                passed ? Status.PASS : Status.FAIL,
                passed ? null : "TIME_CONDITIONS_VIOLATED",
                checks,
                observed);
    }

    private static List<Rational> gaps(List<Integer> positions, int cycle, Rational time) {
        List<Rational> result = new ArrayList<>();
        for (int i = 0; i < positions.size(); i++) {
            long next =
                    i + 1 < positions.size()
                            ? positions.get(i + 1)
                            : (long) positions.getFirst() + cycle;
            result.add(time.multiply(next - positions.get(i)));
        }
        return List.copyOf(result);
    }
}
