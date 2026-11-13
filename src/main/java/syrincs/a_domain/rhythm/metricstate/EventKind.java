package syrincs.a_domain.rhythm.metricstate;

/** Audible event at one grid position, derived from the notation and the sound condition. */
public enum EventKind {
    /** A new tone starts ({@code x}). */
    ONSET,
    /** A sustained tone keeps sounding ({@code =}, SUSTAINED only). */
    HOLD,
    /** First silence after a sounding position (SUSTAINED only); heard as an onset of the rest. */
    REST_ONSET,
    /** No onset (IMPULSE) or continued silence (SUSTAINED). */
    SILENCE
}
