package syrincs.b_application.rhythm.information;

import syrincs.a_domain.rhythm.information.AnalysisSettings;

import java.math.BigInteger;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

public record GenerationRequest(
        AnalysisSettings settings,
        IntRange information,
        IntRange additionalGlobalInformation,
        EventConstraints events,
        ProfileConstraints profiles,
        boolean requireReferencePass,
        Strategy strategy,
        BigInteger attemptBudget,
        int resultLimit,
        Selection selection,
        Long seed) {
    public enum Strategy {
        EXHAUSTIVE,
        CODE_LENGTH_SEARCH,
        BOUNDED_RANDOM
    }

    public enum Selection {
        FIRST,
        RANDOM_WITHOUT_REPLACEMENT
    }

    public enum ProfileBasis {
        WINDOWS,
        BARS
    }

    public record EventConstraints(
            IntRange onsetCount,
            RationalRange density,
            Set<Integer> requiredPositions,
            Set<Integer> forbiddenPositions,
            Integer minOnsetGap,
            Integer maxZeroRun) {
        public EventConstraints {
            requiredPositions = Collections.unmodifiableSortedSet(new TreeSet<>(requiredPositions));
            forbiddenPositions =
                    Collections.unmodifiableSortedSet(new TreeSet<>(forbiddenPositions));
        }

        public static EventConstraints none() {
            return new EventConstraints(null, null, Set.of(), Set.of(), null, null);
        }
    }

    public record ProfileConstraints(
            List<IntRange> windows,
            List<IntRange> bars,
            RationalRange deviation,
            ProfileBasis deviationBasis,
            Set<Integer> peaks,
            ProfileBasis peakBasis) {
        public ProfileConstraints {
            windows = List.copyOf(windows);
            bars = List.copyOf(bars);
            peaks = Collections.unmodifiableSortedSet(new TreeSet<>(peaks));
        }

        public static ProfileConstraints none() {
            return new ProfileConstraints(List.of(), List.of(), null, null, Set.of(), null);
        }
    }
}
