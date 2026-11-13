package syrincs.b_application.ports.dto;

import java.util.List;

/** Optional exact profile and one-based maximum position for a single 4/4 bar. */
public record BeatProfileCriteria(List<Integer> profile, Integer peakBeat) {
    private static final int BEAT_COUNT = 4;

    public BeatProfileCriteria {
        if (profile != null) {
            if (profile.size() != BEAT_COUNT || profile.stream().anyMatch(v -> v == null || v < 0)) {
                throw new IllegalArgumentException("--beat-profile requires exactly four non-negative integers");
            }
            profile = List.copyOf(profile);
        }
        if (peakBeat != null && (peakBeat < 1 || peakBeat > BEAT_COUNT)) {
            throw new IllegalArgumentException("--peak-beat must be between 1 and 4 (one-based)");
        }
        if (profile != null && peakBeat != null && !isPeak(profile, peakBeat)) {
            throw new IllegalArgumentException("--peak-beat contradicts --beat-profile: the requested beat is not a maximum");
        }
    }

    /** Uses a long sum so large valid integers cannot wrap into a matching grade. */
    public void validateInformation(int information) {
        if (profile != null && profile.stream().mapToLong(Integer::longValue).sum() != information) {
            throw new IllegalArgumentException("--beat-profile sum must equal --info");
        }
    }

    public boolean active() {
        return profile != null || peakBeat != null;
    }

    /** Ties, including an all-zero profile, count as maxima at every tied position. */
    public boolean matches(List<Integer> beatInformation) {
        if (!active()) return true;
        return beatInformation.size() == BEAT_COUNT
                && (profile == null || profile.equals(beatInformation))
                && (peakBeat == null || isPeak(beatInformation, peakBeat));
    }

    private static boolean isPeak(List<Integer> values, int oneBasedBeat) {
        int value = values.get(oneBasedBeat - 1);
        return values.stream().allMatch(v -> v <= value);
    }
}
