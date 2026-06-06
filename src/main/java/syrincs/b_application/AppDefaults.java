package syrincs.b_application;

/**
 * Application-layer defaults and simple configuration constants.
 *
 * Centralizes default values used across use cases and adapters to avoid
 * scattering magic numbers. Domain layer must not depend on this class.
 */
public final class AppDefaults {
    private AppDefaults() {}

    /** Default tempo in beats per minute, used when no tempo is specified. */
    public static final int DEFAULT_TEMPO_BPM = 120;

    /** Minimum beat-information deviation for DB rhythm playback candidates. */
    public static final double MIN_HUFFMAN_RHYTHM_DEVIATION = 0.7;
}
