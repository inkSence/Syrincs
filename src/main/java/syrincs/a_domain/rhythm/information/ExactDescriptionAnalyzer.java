package syrincs.a_domain.rhythm.information;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/** Complete M05 recurrence. All recursive children have strictly smaller length. */
public final class ExactDescriptionAnalyzer {
    public static final String VERSION = "description-v1";

    public OptimalCodes analyze(OnsetSequence sequence) {
        return new Engine().base(sequence.bits());
    }

    public OptimalCodes conditional(OnsetSequence sequence, String historyBits) {
        return new Engine().conditional(sequence.bits(), historyBits);
    }

    /** Request-local memoization; never retains other generator candidates. */
    static final class Engine {
        private final Map<String, OptimalCodes> memo = new HashMap<>();

        OptimalCodes base(String word) {
            OptimalCodes cached = memo.get(word);
            if (cached != null) return cached;
            int n = word.length();
            if (n < 1 || n > 128 || !word.matches("[01]+"))
                throw new IllegalArgumentException("Invalid target word");
            // Three bits is the proven universal lower bound, achieved uniquely here.
            if (word.indexOf('1') < 0) return remember(word, OptimalCodes.fixed("000"));
            if (word.indexOf('0') < 0) return remember(word, OptimalCodes.fixed("001"));
            Best best = new Best(OptimalCodes.fixed("010" + word));
            for (int p = 1; p <= n / 2; p++) {
                String motif = word.substring(0, p);
                if (ContextDictionary.repeat(motif, n).equals(word))
                    best.offer(
                            OptimalCodes.prefix(
                                    "011" + DescriptionDecoder.field(n / 2, p - 1), base(motif)));
            }
            int splitHeader = 3 + DescriptionDecoder.width(n - 1);
            if (splitHeader + 6 <= best.cost)
                for (int k = 1; k < n; k++) {
                    OptimalCodes left = base(word.substring(0, k));
                    if (splitHeader + left.totalBits() + 3 > best.cost) continue;
                    OptimalCodes right = base(word.substring(k));
                    if (splitHeader + left.totalBits() + right.totalBits() <= best.cost)
                        best.offer(
                                OptimalCodes.product(
                                        "100" + DescriptionDecoder.field(n - 1, k - 1),
                                        left,
                                        right));
                }
            for (int p = 1; p <= n / 2; p++) {
                int header =
                        3 + DescriptionDecoder.width(n / 2) + p + DescriptionDecoder.width(n + 1);
                if (header > best.cost) continue;
                int q = 0;
                List<Integer> ties = new ArrayList<>();
                for (int r = 0; r < p; r++) {
                    int ones = 0, zeros = 0;
                    for (int i = r; i < n; i += p)
                        if (word.charAt(i) == '1') ones++;
                        else zeros++;
                    q += Math.min(ones, zeros);
                    if (ones == zeros) ties.add(r);
                }
                if (header + q * DescriptionDecoder.width(n) <= best.cost)
                    best.offer(OptimalCodes.patch(word, p, ties));
            }
            return remember(word, OptimalCodes.choice(best.choices));
        }

        OptimalCodes conditional(String word, String historyBits) {
            Best best = new Best(OptimalCodes.prefix("0", base(word)));
            List<String> dictionary = ContextDictionary.fromHistory(historyBits);
            for (int j = 0; j < dictionary.size(); j++) {
                String index = DescriptionDecoder.field(dictionary.size(), j);
                String predicted = ContextDictionary.repeat(dictionary.get(j), word.length());
                if (predicted.equals(word)) best.offer(OptimalCodes.fixed("10" + index));
                if (2 + index.length() + 3 <= best.cost)
                    best.offer(
                            OptimalCodes.prefix(
                                    "11" + index, base(ContextDictionary.xor(word, predicted))));
            }
            return OptimalCodes.choice(best.choices);
        }

        private OptimalCodes remember(String word, OptimalCodes result) {
            memo.put(word, result);
            return result;
        }
    }

    private static final class Best {
        int cost;
        final List<OptimalCodes> choices = new ArrayList<>();

        Best(OptimalCodes initial) {
            cost = initial.totalBits();
            choices.add(initial);
        }

        void offer(OptimalCodes candidate) {
            if (candidate.totalBits() < cost) {
                cost = candidate.totalBits();
                choices.clear();
            }
            if (candidate.totalBits() == cost) choices.add(candidate);
        }
    }
}
