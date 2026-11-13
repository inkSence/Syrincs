package syrincs.b_application.rhythm.information;

import static syrincs.b_application.rhythm.information.GenerationRequest.*;

import syrincs.a_domain.rhythm.information.*;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/** One validated AND-predicate shared by every enumeration strategy. */
final class GenerationCriteria {
    private final GenerationRequest request;

    GenerationCriteria(GenerationRequest r) {
        request = r;
        if (r.settings() == null
                || r.information() == null
                || r.events() == null
                || r.profiles() == null
                || r.strategy() == null
                || r.selection() == null)
            throw new IllegalArgumentException("Incomplete generation request");
        int n = r.settings().frame().slots();
        if (r.resultLimit() <= 0)
            throw new IllegalArgumentException("Result limit must be positive");
        if (r.strategy() == Strategy.EXHAUSTIVE) {
            if (n > 16 || r.attemptBudget() != null)
                throw new IllegalArgumentException(
                        "Exhaustive supports N=1..16 and has no attempt budget");
        } else if (r.attemptBudget() == null || r.attemptBudget().signum() <= 0)
            throw new IllegalArgumentException("Bounded strategy needs a positive attempt budget");
        boolean random =
                r.strategy() == Strategy.BOUNDED_RANDOM
                        || r.selection() == Selection.RANDOM_WITHOUT_REPLACEMENT;
        if (random != (r.seed() != null))
            throw new IllegalArgumentException(
                    "A seed is required exactly when randomness is requested");
        if (r.strategy() == Strategy.BOUNDED_RANDOM && r.selection() != Selection.FIRST)
            throw new IllegalArgumentException("Random enumeration requires FIRST selection");
        IntRange scale =
                switch (r.settings().scope()) {
                    case GLOBAL_DESCRIPTION -> new IntRange(3, n + 3);
                    case ISOLATED_WINDOWS -> new IntRange(3 * windowCount(), n + 3 * windowCount());
                    case CAUSAL_WINDOWS ->
                            new IntRange(
                                    2 * windowCount()
                                            + (r.settings().initialHistory().isEmpty() ? 2 : 0),
                                    n + 4 * windowCount());
                };
        if (!scale.contains(r.information().min()) || !scale.contains(r.information().max()))
            throw new IllegalArgumentException("Information target outside scope scale " + scale);
        if (r.additionalGlobalInformation() != null
                && (r.settings().scope() == Scope.GLOBAL_DESCRIPTION
                        || r.additionalGlobalInformation().min() < 3
                        || r.additionalGlobalInformation().max() > n + 3))
            throw new IllegalArgumentException(
                    "Additional global target requires local scope and range 3..N+3");
        var e = r.events();
        if (e.onsetCount() != null && e.onsetCount().max() > n)
            throw new IllegalArgumentException("Onset count outside 0..N");
        if (e.density() != null
                && (e.density().min() != null && e.density().min().compareTo(Rational.ONE) > 0
                        || e.density().max() != null
                                && e.density().max().compareTo(Rational.ONE) > 0))
            throw new IllegalArgumentException("Density outside 0..1");
        Set<Integer> both = new HashSet<>(e.requiredPositions());
        both.retainAll(e.forbiddenPositions());
        if (!both.isEmpty())
            throw new IllegalArgumentException("Required and forbidden positions overlap");
        for (Set<Integer> set : List.of(e.requiredPositions(), e.forbiddenPositions()))
            for (int p : set)
                if (p < 0 || p >= n)
                    throw new IllegalArgumentException("Event position outside 0..N-1");
        int lo = e.onsetCount() == null ? 0 : e.onsetCount().min(),
                hi = e.onsetCount() == null ? n : e.onsetCount().max();
        if (e.density() != null) {
            if (e.density().min() != null)
                lo = Math.max(lo, e.density().min().multiply(n).ceil().intValueExact());
            if (e.density().max() != null)
                hi = Math.min(hi, e.density().max().multiply(n).floor().intValueExact());
        }
        lo = Math.max(lo, e.requiredPositions().size());
        hi = Math.min(hi, n - e.forbiddenPositions().size());
        if (lo > hi)
            throw new IllegalArgumentException("Contradictory density/count/position constraints");
        if (e.minOnsetGap() != null && e.minOnsetGap() <= 0
                || e.maxZeroRun() != null && (e.maxZeroRun() < 0 || e.maxZeroRun() > n))
            throw new IllegalArgumentException("Invalid gap/zero-run bound");
        if (!eventsFeasible(n, lo, hi, e))
            throw new IllegalArgumentException(
                    "Contradictory event constraints: no count/position/gap/zero-run combination is"
                        + " possible");
        var p = r.profiles();
        boolean hasProfile =
                !p.windows().isEmpty()
                        || !p.bars().isEmpty()
                        || p.deviation() != null
                        || !p.peaks().isEmpty();
        if (r.settings().scope() == Scope.GLOBAL_DESCRIPTION && hasProfile)
            throw new IllegalArgumentException("Profile criteria require local scope");
        validateProfile(p.windows(), windowCount(), "window");
        validateProfile(p.bars(), barCount(), "bar");
        if (p.deviation() != null && p.deviationBasis() == null
                || p.deviation() == null && p.deviationBasis() != null)
            throw new IllegalArgumentException("Deviation and basis must be supplied together");
        if (!p.peaks().isEmpty() && p.peakBasis() == null
                || p.peaks().isEmpty() && p.peakBasis() != null)
            throw new IllegalArgumentException("Peaks and basis must be supplied together");
        if (p.deviationBasis() == ProfileBasis.BARS || p.peakBasis() == ProfileBasis.BARS)
            if (barCount() == 0)
                throw new IllegalArgumentException("Bar criteria need bar grouping");
        int size = p.peakBasis() == ProfileBasis.BARS ? barCount() : windowCount();
        for (int position : p.peaks())
            if (position < 1 || position > size)
                throw new IllegalArgumentException("Peak outside 1..profile size");
        List<IntRange> peakProfile = p.peakBasis() == ProfileBasis.BARS ? p.bars() : p.windows();
        if (!peakProfile.isEmpty() && !p.peaks().isEmpty()) {
            int requiredFloor = peakProfile.stream().mapToInt(IntRange::min).max().orElseThrow();
            for (int peak : p.peaks())
                if (peakProfile.get(peak - 1).max() < requiredFloor)
                    throw new IllegalArgumentException("Peak contradicts profile");
        }
        if (!p.windows().isEmpty() && !p.bars().isEmpty()) {
            var seg = r.settings().segmentation();
            for (int i = 1; i < seg.barBoundaries().size(); i++) {
                int a = seg.boundaries().indexOf(seg.barBoundaries().get(i - 1)),
                        b = seg.boundaries().indexOf(seg.barBoundaries().get(i));
                if (!sumRange(p.windows().subList(a, b)).overlaps(p.bars().get(i - 1)))
                    throw new IllegalArgumentException("Bar profile contradicts window profile");
            }
        }
        if (r.requireReferencePass()
                && (r.settings().frame().slotDurationMs() == null
                        || r.settings().references().isEmpty()
                        || r.settings().referenceProfile() == null))
            throw new IllegalArgumentException(
                    "Reference PASS condition needs explicit reference, time and profile");
    }

    private record EventState(int lastOnset, int count) {}

    /** Exact finite feasibility check, independent of the information measure or search budget. */
    private static boolean eventsFeasible(int n, int lo, int hi, EventConstraints e) {
        Set<EventState> states = Set.of(new EventState(-1, 0));
        for (int position = 0; position < n; position++) {
            Set<EventState> next = new HashSet<>();
            for (EventState state : states) {
                if (!e.requiredPositions().contains(position)
                        && (e.maxZeroRun() == null
                                || position - state.lastOnset() <= e.maxZeroRun())) next.add(state);
                if (!e.forbiddenPositions().contains(position)
                        && state.count() < hi
                        && (state.lastOnset() == -1
                                || e.minOnsetGap() == null
                                || position - state.lastOnset() >= e.minOnsetGap()))
                    next.add(new EventState(position, state.count() + 1));
            }
            states = next;
            if (states.isEmpty()) return false;
        }
        return states.stream().anyMatch(s -> s.count() >= lo && s.count() <= hi);
    }

    private int windowCount() {
        return request.settings().segmentation() == null
                ? 0
                : request.settings().segmentation().boundaries().size() - 1;
    }

    private int barCount() {
        return request.settings().segmentation() == null
                        || request.settings().segmentation().barBoundaries().isEmpty()
                ? 0
                : request.settings().segmentation().barBoundaries().size() - 1;
    }

    private static IntRange sumRange(List<IntRange> ranges) {
        try {
            return new IntRange(
                    ranges.stream().mapToInt(IntRange::min).reduce(0, Math::addExact),
                    ranges.stream().mapToInt(IntRange::max).reduce(0, Math::addExact));
        } catch (ArithmeticException e) {
            throw new IllegalArgumentException("Profile range sum overflow", e);
        }
    }

    private void validateProfile(List<IntRange> profile, int size, String name) {
        if (profile.isEmpty()) return;
        if (size == 0
                || profile.size() != size
                || !sumRange(profile).overlaps(request.information()))
            throw new IllegalArgumentException("Invalid " + name + " profile length/sum");
    }

    boolean eventsMatch(OnsetSequence word) {
        var e = request.events();
        int n = word.slots(), count = word.onsetCount();
        if (e.onsetCount() != null && !e.onsetCount().contains(count)) return false;
        if (e.density() != null && !e.density().contains(Rational.of(count, n))) return false;
        for (int p : e.requiredPositions())
            if (word.normalizedOnsets().charAt(p) != 'x') return false;
        for (int p : e.forbiddenPositions())
            if (word.normalizedOnsets().charAt(p) != 'o') return false;
        int previous = -1, zeros = 0;
        for (int i = 0; i < n; i++) {
            if (word.normalizedOnsets().charAt(i) == 'x') {
                if (previous >= 0 && e.minOnsetGap() != null && i - previous < e.minOnsetGap())
                    return false;
                previous = i;
                zeros = 0;
            } else if (e.maxZeroRun() != null && ++zeros > e.maxZeroRun()) return false;
        }
        return true;
    }

    boolean analysisMatches(AnalysisResult result) {
        var a = result.primary();
        var p = request.profiles();
        if (!request.information().contains(a.totalBits())) return false;
        if (request.additionalGlobalInformation() != null
                && !request.additionalGlobalInformation()
                        .contains(result.additionalGlobal().totalBits())) return false;
        if (!matchesProfile(
                        p.windows(),
                        a.windows().stream().map(InformationAnalysis.Window::totalBits).toList())
                || !matchesProfile(p.bars(), a.barContributions())) return false;
        ProfileMetrics metric =
                p.deviationBasis() == ProfileBasis.BARS ? a.barMetrics() : a.windowMetrics();
        if (p.deviation() != null && !p.deviation().containsDeviationVariance(metric.variance()))
            return false;
        ProfileMetrics peak =
                p.peakBasis() == ProfileBasis.BARS ? a.barMetrics() : a.windowMetrics();
        if (!p.peaks().isEmpty() && !peak.peakPositions().containsAll(p.peaks())) return false;
        return !request.requireReferencePass()
                || a.referenceAssessments().stream()
                        .anyMatch(r -> r.status() == ReferenceAssessment.Status.PASS);
    }

    private boolean matchesProfile(List<IntRange> ranges, List<Integer> values) {
        if (ranges.isEmpty()) return true;
        for (int i = 0; i < ranges.size(); i++)
            if (!ranges.get(i).contains(values.get(i))) return false;
        return true;
    }
}
