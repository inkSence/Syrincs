package syrincs.a_domain.rhythm.information;

import java.math.BigInteger;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.stream.IntStream;

public record OnsetSequence(String normalizedOnsets) {
    public static final int MAX_SLOTS = 128;

    public OnsetSequence {
        normalizedOnsets = normalize(normalizedOnsets, false);
        if (normalizedOnsets.length() > MAX_SLOTS)
            throw new IllegalArgumentException("At most 128 slots are supported");
    }

    public static String normalize(String input, boolean allowEmpty) {
        Objects.requireNonNull(input, "Onsets");
        String value = input.replaceAll("(?U)\\s", "").toLowerCase(Locale.ROOT);
        if ((!allowEmpty && value.isEmpty()) || !value.matches("[xo]*"))
            throw new IllegalArgumentException(
                    "Onsets must contain only x/o and whitespace, with a nonempty target");
        return value;
    }

    public static OnsetSequence fromBits(String bits) {
        if (bits == null || !bits.matches("[01]+"))
            throw new IllegalArgumentException("Expected nonempty binary word");
        return new OnsetSequence(bits.replace('1', 'x').replace('0', 'o'));
    }

    public String bits() {
        return normalizedOnsets.replace('x', '1').replace('o', '0');
    }

    public int slots() {
        return normalizedOnsets.length();
    }

    public List<Integer> positions() {
        return IntStream.range(0, slots())
                .filter(i -> normalizedOnsets.charAt(i) == 'x')
                .boxed()
                .toList();
    }

    public int onsetCount() {
        return (int) normalizedOnsets.chars().filter(c -> c == 'x').count();
    }

    public BigInteger mask() {
        return new BigInteger(bits(), 2);
    }
}
