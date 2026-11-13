package syrincs.a_domain.rhythm.metricstate;

/** The four words of the 2-bit base dictionary. */
public enum Instruction {
    /** {@code 00}: strengthen the division, Δ → Δ/2. */
    REFINE("00"),
    /** {@code 01}: reduce the division, Δ → 2Δ. */
    COARSEN("01"),
    /** {@code 10}: play notes. */
    PLAY_NOTES("10"),
    /** {@code 11}: play rests. */
    PLAY_RESTS("11");

    public static final int BITS = 2;

    private final String code;

    Instruction(String code) {
        this.code = code;
    }

    public String code() {
        return code;
    }

    public static Instruction fromCode(String code) {
        for (Instruction instruction : values())
            if (instruction.code.equals(code)) return instruction;
        throw new IllegalArgumentException("Unknown instruction code: " + code);
    }
}
