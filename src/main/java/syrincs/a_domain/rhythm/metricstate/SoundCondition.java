package syrincs.a_domain.rhythm.metricstate;

/** Named sound condition of an example; there is deliberately no default. */
public enum SoundCondition {
    /** Short impulse: only onsets are audible events. */
    IMPULSE,
    /** Sustained sound with an audible end; the end of a tone is heard as a rest onset. */
    SUSTAINED
}
