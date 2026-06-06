package syrincs.a_domain.rhythm;

/**
 * Immutable header values parsed from RDL-0. All fields are optional; defaults are applied later.
 */
public record PatternHeader(
        Integer timeNum,
        Integer timeDen,
        Integer tempo,
        Integer resPerBeat,
        Integer bars
) {}
