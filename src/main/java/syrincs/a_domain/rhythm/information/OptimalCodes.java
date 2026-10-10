package syrincs.a_domain.rhythm.information;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.IdentityHashMap;
import java.util.List;

/** Compact immutable DAG of all optimal codewords, including exponential PATCH ties. */
public final class OptimalCodes {
    public record GraphNode(
            int id,
            String kind,
            String prefix,
            List<Integer> children,
            String sourceBits,
            Integer period,
            List<Integer> tiedResidues,
            String count,
            String canonicalCode) {
        public GraphNode {
            children = List.copyOf(children);
            tiedResidues = List.copyOf(tiedResidues);
        }
    }

    public record Graph(int root, List<GraphNode> nodes) {
        public Graph {
            nodes = List.copyOf(nodes);
        }
    }

    private final String kind, prefix, source;
    private final List<OptimalCodes> children;
    private final int period;
    private final List<Integer> ties;
    private final String canonical;
    private final BigInteger count;

    private OptimalCodes(
            String kind,
            String prefix,
            List<OptimalCodes> children,
            String source,
            int period,
            List<Integer> ties) {
        this.kind = kind;
        this.prefix = prefix;
        this.children = List.copyOf(children);
        this.source = source;
        this.period = period;
        this.ties = List.copyOf(ties);
        switch (kind) {
            case "FIXED" -> {
                canonical = prefix;
                count = BigInteger.ONE;
            }
            case "PREFIX" -> {
                canonical = prefix + children.getFirst().canonical;
                count = children.getFirst().count;
            }
            case "PRODUCT" -> {
                canonical = prefix + children.get(0).canonical + children.get(1).canonical;
                count = children.get(0).count.multiply(children.get(1).count);
            }
            case "CHOICE" -> {
                canonical =
                        children.stream()
                                .map(c -> c.canonical)
                                .min(Comparator.naturalOrder())
                                .orElseThrow();
                count =
                        children.stream()
                                .map(c -> c.count)
                                .reduce(BigInteger.ZERO, BigInteger::add);
            }
            case "PATCH" -> {
                canonical = patchCode(BigInteger.ZERO);
                count = BigInteger.ONE.shiftLeft(ties.size());
            }
            default -> throw new IllegalArgumentException("Unknown optimal-code node");
        }
    }

    static OptimalCodes fixed(String code) {
        return new OptimalCodes("FIXED", code, List.of(), null, 0, List.of());
    }

    static OptimalCodes prefix(String prefix, OptimalCodes child) {
        return new OptimalCodes("PREFIX", prefix, List.of(child), null, 0, List.of());
    }

    static OptimalCodes product(String prefix, OptimalCodes a, OptimalCodes b) {
        return new OptimalCodes("PRODUCT", prefix, List.of(a, b), null, 0, List.of());
    }

    static OptimalCodes choice(List<OptimalCodes> choices) {
        return choices.size() == 1
                ? choices.getFirst()
                : new OptimalCodes("CHOICE", "", choices, null, 0, List.of());
    }

    static OptimalCodes patch(String source, int period, List<Integer> ties) {
        return new OptimalCodes("PATCH", "", List.of(), source, period, ties);
    }

    public String canonicalCode() {
        return canonical;
    }

    public int totalBits() {
        return canonical.length();
    }

    public BigInteger count() {
        return count;
    }

    public List<String> alternatives(int limit) {
        if (limit <= 0)
            throw new IllegalArgumentException("Alternative view limit must be positive");
        List<String> result = new ArrayList<>();
        switch (kind) {
            case "FIXED" -> result.add(prefix);
            case "PREFIX" -> {
                for (String code : children.getFirst().alternatives(limit))
                    result.add(prefix + code);
            }
            case "PRODUCT" -> {
                List<String> right = children.get(1).alternatives(limit);
                outer:
                for (String a : children.get(0).alternatives(limit))
                    for (String b : right) {
                        result.add(prefix + a + b);
                        if (result.size() == limit) break outer;
                    }
            }
            case "CHOICE" -> {
                for (OptimalCodes c : children) result.addAll(c.alternatives(limit));
                result.sort(Comparator.naturalOrder());
                if (result.size() > limit) result = new ArrayList<>(result.subList(0, limit));
            }
            case "PATCH" -> {
                BigInteger stop = count.min(BigInteger.valueOf(limit));
                for (BigInteger i = BigInteger.ZERO;
                        i.compareTo(stop) < 0;
                        i = i.add(BigInteger.ONE)) result.add(patchCode(i));
            }
            default -> throw new IllegalStateException(kind);
        }
        return List.copyOf(result);
    }

    private String patchCode(BigInteger variant) {
        char[] template = new char[period];
        for (int r = 0; r < period; r++) {
            int ones = 0, zeros = 0;
            for (int i = r; i < source.length(); i += period)
                if (source.charAt(i) == '1') ones++;
                else zeros++;
            template[r] = ones > zeros ? '1' : '0';
        }
        for (int i = 0; i < ties.size(); i++)
            template[ties.get(i)] = variant.testBit(ties.size() - 1 - i) ? '1' : '0';
        List<Integer> flips = new ArrayList<>();
        for (int i = 0; i < source.length(); i++)
            if (source.charAt(i) != template[i % period]) flips.add(i);
        StringBuilder code =
                new StringBuilder("101")
                        .append(DescriptionDecoder.field(source.length() / 2, period - 1))
                        .append(template)
                        .append(DescriptionDecoder.field(source.length() + 1, flips.size()));
        for (int i : flips) code.append(DescriptionDecoder.field(source.length(), i));
        return code.toString();
    }

    public Graph graph() {
        IdentityHashMap<OptimalCodes, Integer> ids = new IdentityHashMap<>();
        List<OptimalCodes> queue = new ArrayList<>();
        ids.put(this, 0);
        queue.add(this);
        List<GraphNode> nodes = new ArrayList<>();
        for (int i = 0; i < queue.size(); i++) {
            OptimalCodes node = queue.get(i);
            List<Integer> childIds = new ArrayList<>();
            for (OptimalCodes child : node.children) {
                if (!ids.containsKey(child)) {
                    ids.put(child, queue.size());
                    queue.add(child);
                }
                childIds.add(ids.get(child));
            }
            nodes.add(
                    new GraphNode(
                            i,
                            node.kind,
                            node.prefix,
                            childIds,
                            node.source,
                            node.period == 0 ? null : node.period,
                            node.ties,
                            node.count.toString(),
                            node.canonical));
        }
        return new Graph(0, nodes);
    }
}
