package syrincs.a_domain.rhythm.information;

import java.util.List;

/** A decoded witness, not a claim that its code is minimal. */
public record Description(
        String form,
        int slots,
        String code,
        int modelBits,
        int residualBits,
        Integer period,
        Integer split,
        String template,
        Integer templateIndex,
        List<Integer> exceptionPositions,
        List<Description> children) {
    public Description {
        exceptionPositions = List.copyOf(exceptionPositions);
        children = List.copyOf(children);
        if (modelBits < 0 || residualBits < 0 || modelBits + residualBits != code.length())
            throw new IllegalArgumentException("Witness costs do not equal code length");
    }

    public int totalBits() {
        return code.length();
    }
}
