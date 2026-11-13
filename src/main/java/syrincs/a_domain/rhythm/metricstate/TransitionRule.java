package syrincs.a_domain.rhythm.metricstate;

/** Rows of the transition table of the functional contract. */
public enum TransitionRule {
    START_WITH_ONSET("1"),
    START_WITHOUT_ONSET("2"),
    ONSET_AT_EXPECTATION("3"),
    ONSET_BEFORE_EXPECTATION("4"),
    MISSING_ONSET_PLAY_RESTS("5a"),
    MISSING_ONSET_COARSEN("5b"),
    REST_ONSET_AT_EXPECTATION("5a-G"),
    HELD_TONE_AT_EXPECTATION("5b-G"),
    REST_ONSET_BEFORE_EXPECTATION("4-G"),
    SILENCE_IN_RESTS("6"),
    ONSET_ON_SILENT_GRID("7"),
    ONSET_OFF_SILENT_GRID("8"),
    /** In N, between expectations, nothing audible changes: no instruction. */
    NO_CHANGE_IN_NOTES("-");

    private final String row;

    TransitionRule(String row) {
        this.row = row;
    }

    /** Row number in the contract's table, or {@code -} for the implicit no-change case. */
    public String row() {
        return row;
    }
}
