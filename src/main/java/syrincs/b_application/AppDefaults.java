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
}
