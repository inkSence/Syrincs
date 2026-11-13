package syrincs.a_domain.rhythm.information;

import java.util.ArrayList;
import java.util.List;

/** Strict, independent interpreter of M04/M06. It never calls the minimizer. */
public final class DescriptionDecoder {
    public record Decoded(String bits, List<Description> messages) {
        public Decoded {
            messages = List.copyOf(messages);
        }
    }

    private record Part(String bits, Description description) {}

    public Decoded decode(String code, int slots) {
        return decode(code, slots, Scope.GLOBAL_DESCRIPTION, null, "");
    }

    public Decoded decode(
            String code, int slots, Scope scope, Segmentation segmentation, String historyBits) {
        AnalysisFrame frame = AnalysisFrame.unknown(slots);
        if (scope == null) throw new IllegalArgumentException("Missing scope");
        if (!historyBits.matches("[01]*")) throw new IllegalArgumentException("Invalid history");
        if (scope != Scope.CAUSAL_WINDOWS && !historyBits.isEmpty())
            throw new IllegalArgumentException("History requires causal scope");
        if ((scope == Scope.GLOBAL_DESCRIPTION) != (segmentation == null))
            throw new IllegalArgumentException("Segmentation requires local scope");
        Cursor cursor = new Cursor(code);
        List<Description> messages = new ArrayList<>();
        StringBuilder bits = new StringBuilder();
        if (scope == Scope.GLOBAL_DESCRIPTION) {
            Part p = base(cursor, slots);
            bits.append(p.bits);
            messages.add(p.description);
        } else {
            segmentation.validate(frame);
            for (int i = 1; i < segmentation.boundaries().size(); i++) {
                int n = segmentation.boundaries().get(i) - segmentation.boundaries().get(i - 1);
                Part p =
                        scope == Scope.CAUSAL_WINDOWS
                                ? conditional(cursor, n, historyBits + bits)
                                : base(cursor, n);
                bits.append(p.bits);
                messages.add(p.description);
            }
        }
        if (cursor.position != code.length())
            throw new IllegalArgumentException("Extra bits after complete description");
        return new Decoded(bits.toString(), messages);
    }

    private Part base(Cursor c, int n) {
        int start = c.position;
        String tag = c.read(3), bits;
        Integer period = null, split = null;
        String template = null;
        List<Integer> exceptions = new ArrayList<>();
        List<Description> children = new ArrayList<>();
        int model, residual;
        String form;
        switch (tag) {
            case "000" -> {
                form = "EMPTY";
                bits = "0".repeat(n);
                model = 3;
                residual = 0;
            }
            case "001" -> {
                form = "FULL";
                bits = "1".repeat(n);
                model = 3;
                residual = 0;
            }
            case "010" -> {
                form = "LITERAL";
                bits = c.read(n);
                model = 3;
                residual = n;
            }
            case "011" -> {
                if (n == 1) throw new IllegalArgumentException("REPEAT unavailable for n=1");
                form = "REPEAT";
                period = c.field(n / 2) + 1;
                Part motif = base(c, period);
                children.add(motif.description);
                bits = ContextDictionary.repeat(motif.bits, n);
                model = c.position - start;
                residual = 0;
            }
            case "100" -> {
                if (n == 1) throw new IllegalArgumentException("CONCAT unavailable for n=1");
                form = "CONCAT";
                split = c.field(n - 1) + 1;
                Part a = base(c, split), b = base(c, n - split);
                children.add(a.description);
                children.add(b.description);
                bits = a.bits + b.bits;
                model = 3 + width(n - 1) + a.description.modelBits() + b.description.modelBits();
                residual = a.description.residualBits() + b.description.residualBits();
            }
            case "101" -> {
                if (n == 1) throw new IllegalArgumentException("PATCH unavailable for n=1");
                form = "PATCH";
                period = c.field(n / 2) + 1;
                template = c.read(period);
                int q = c.field(n + 1);
                char[] word = ContextDictionary.repeat(template, n).toCharArray();
                int previous = -1;
                for (int i = 0; i < q; i++) {
                    int position = c.field(n);
                    if (position <= previous)
                        throw new IllegalArgumentException(
                                "PATCH indices must be strictly increasing");
                    previous = position;
                    exceptions.add(position);
                    word[position] = word[position] == '0' ? '1' : '0';
                }
                bits = new String(word);
                model = 3 + width(n / 2) + period;
                residual = width(n + 1) + q * width(n);
            }
            default -> throw new IllegalArgumentException("Reserved description tag: " + tag);
        }
        return new Part(
                bits,
                new Description(
                        form,
                        n,
                        c.code.substring(start, c.position),
                        model,
                        residual,
                        period,
                        split,
                        template,
                        null,
                        exceptions,
                        children));
    }

    private Part conditional(Cursor c, int n, String history) {
        int start = c.position;
        if (c.read(1).equals("0")) {
            Part child = base(c, n);
            return new Part(
                    child.bits,
                    new Description(
                            "DIRECT",
                            n,
                            c.code.substring(start, c.position),
                            1 + child.description.modelBits(),
                            child.description.residualBits(),
                            null,
                            null,
                            null,
                            null,
                            List.of(),
                            List.of(child.description)));
        }
        boolean change = c.read(1).equals("1");
        List<String> dictionary = ContextDictionary.fromHistory(history);
        if (dictionary.isEmpty())
            throw new IllegalArgumentException("REUSE requires a nonempty context dictionary");
        int index = c.field(dictionary.size());
        String template = dictionary.get(index), prediction = ContextDictionary.repeat(template, n);
        Part child = change ? base(c, n) : null;
        String bits = change ? ContextDictionary.xor(prediction, child.bits) : prediction;
        return new Part(
                bits,
                new Description(
                        change ? "REUSE_CHANGE" : "REUSE_CONFIRM",
                        n,
                        c.code.substring(start, c.position),
                        2 + width(dictionary.size()),
                        child == null ? 0 : child.description.totalBits(),
                        template.length(),
                        null,
                        template,
                        index,
                        List.of(),
                        child == null ? List.of() : List.of(child.description)));
    }

    public static int width(int alternatives) {
        if (alternatives < 1) throw new IllegalArgumentException("No alternatives for field");
        return 32 - Integer.numberOfLeadingZeros(alternatives - 1);
    }

    public static String field(int alternatives, int value) {
        if (value < 0 || value >= alternatives)
            throw new IllegalArgumentException("Invalid field value");
        int width = width(alternatives);
        if (width == 0) return "";
        String bits = Integer.toBinaryString(value);
        return "0".repeat(width - bits.length()) + bits;
    }

    private static final class Cursor {
        final String code;
        int position;

        Cursor(String code) {
            if (code == null || !code.matches("[01]+"))
                throw new IllegalArgumentException("Code must be a nonempty binary string");
            this.code = code;
        }

        String read(int n) {
            if (n < 0 || position > code.length() - n)
                throw new IllegalArgumentException("Truncated description");
            String result = code.substring(position, position + n);
            position += n;
            return result;
        }

        int field(int alternatives) {
            String bits = read(width(alternatives));
            int result = bits.isEmpty() ? 0 : Integer.parseInt(bits, 2);
            if (result >= alternatives)
                throw new IllegalArgumentException("Unused field combination");
            return result;
        }
    }
}
