package syrincs.c_adapters.cli;

/**
 * Centralized channel mapping helpers.
 * User-friendly semantics: accept 1..16 from the CLI and map to 0..15 internally.
 * Also provides a legacy clamp for zero-based inputs if needed.
 */
public final class ChannelMapper {
    private ChannelMapper() {}

    /**
     * Map an optional human channel (1..16) to zero-based (0..15); clamp to bounds; return fallback if null.
     */
    public static int toZeroBased(Integer userChannelOrNull, int fallbackZeroBased) {
        if (userChannelOrNull == null) return fallbackZeroBased;
        int user = userChannelOrNull;
        if (user < 1) user = 1; else if (user > 16) user = 16;
        return user - 1;
    }

    /**
     * Legacy: clamp an optional zero-based channel value to [0,15]; return fallback if null.
     */
    public static int clampZeroBased(Integer userChannelOrNull, int fallbackZeroBased) {
        if (userChannelOrNull == null) return fallbackZeroBased;
        int v = userChannelOrNull;
        if (v < 0) v = 0; else if (v > 15) v = 15;
        return v;
    }
}
