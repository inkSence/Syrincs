package syrincs.a_domain.rhythm.information;

import static org.junit.jupiter.api.Assertions.*;

import com.google.gson.*;

import org.junit.jupiter.api.Test;

import java.math.BigInteger;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

class DescriptionFixtureTest {
    private final ExactDescriptionAnalyzer analyzer = new ExactDescriptionAnalyzer();
    private final DescriptionDecoder decoder = new DescriptionDecoder();

    private static JsonObject fixtures() throws Exception {
        return JsonParser.parseString(
                        Files.readString(
                                Path.of(
                                        "doc/rhythm-generator/specs-informationsmass/fixtures-description-v1.json")))
                .getAsJsonObject();
    }

    @Test
    void allPublishedPositiveAndNegativeContracts() throws Exception {
        var f = fixtures();
        int checked = 0;
        for (String group : List.of("baseCases", "originalConceptCases", "literatureModelCases"))
            for (JsonElement e : f.getAsJsonArray(group)) {
                JsonObject c = e.getAsJsonObject(),
                        expected = c.getAsJsonObject("globalDescription");
                String id = c.get("id").getAsString();
                OnsetSequence word = new OnsetSequence(c.get("normalizedOnsets").getAsString());
                var global = analyzer.analyze(word);
                assertEquals(expected.get("totalBits").getAsInt(), global.totalBits(), id);
                assertEquals(
                        expected.get("canonicalCode").getAsString(), global.canonicalCode(), id);
                if (expected.has("minimalDescriptionCount"))
                    assertEquals(
                            new BigInteger(expected.get("minimalDescriptionCount").getAsString()),
                            global.count(),
                            id);
                assertEquals(
                        word.bits(),
                        decoder.decode(global.canonicalCode(), word.slots()).bits(),
                        id);
                if (c.has("causalFourSlotWindows")) {
                    var p = c.getAsJsonObject("causalFourSlotWindows");
                    var result =
                            new RhythmInformationAnalyzer()
                                    .analyze(
                                            word,
                                            new AnalysisSettings(
                                                    AnalysisFrame.unknown(word.slots()),
                                                    Scope.CAUSAL_WINDOWS,
                                                    Segmentation.equal(
                                                            word.slots(),
                                                            p.get("windowSlots").getAsInt(),
                                                            null),
                                                    p.has("initialHistory")
                                                            ? p.get("initialHistory").getAsString()
                                                            : "",
                                                    List.of(),
                                                    null));
                    List<Integer> costs = new ArrayList<>();
                    for (JsonElement cost : p.getAsJsonArray("costs")) costs.add(cost.getAsInt());
                    List<String> codes = new ArrayList<>();
                    for (JsonElement code : p.getAsJsonArray("canonicalCodes"))
                        codes.add(code.getAsString());
                    assertEquals(costs, result.windowMetrics().values(), id);
                    assertEquals(
                            codes,
                            result.windows().stream().map(w -> w.witness().code()).toList(),
                            id);
                    assertEquals(p.get("continuationTotal").getAsInt(), result.totalBits(), id);
                }
                if (c.has("knownContext")) {
                    var h = c.getAsJsonObject("knownContext");
                    var result = analyzer.conditional(word, h.get("historyBits").getAsString());
                    assertEquals(h.get("conditionalBlockBits").getAsInt(), result.totalBits(), id);
                    assertEquals(h.get("canonicalCode").getAsString(), result.canonicalCode(), id);
                }
                checked++;
            }
        assertEquals(51, checked);
        for (JsonElement e : f.getAsJsonArray("invalidDescriptions")) {
            var c = e.getAsJsonObject();
            assertThrows(
                    IllegalArgumentException.class,
                    () -> decoder.decode(c.get("code").getAsString(), c.get("slots").getAsInt()),
                    c.get("id").getAsString());
        }
    }

    @Test
    void independentCodeEnumerationProvesAllShortMinimaAndTieCounts() {
        // Enumerate the actual language through the independent interpreter, not the DP recurrence.
        for (int n = 1; n <= 8; n++) {
            Map<String, List<String>> optimal = new HashMap<>();
            for (int length = 3; length <= n + 3; length++)
                for (int value = 0; value < (1 << length); value++) {
                    String code = binary(value, length);
                    String word;
                    try {
                        word = decoder.decode(code, n).bits();
                    } catch (IllegalArgumentException invalid) {
                        continue;
                    }
                    List<String> codes = optimal.computeIfAbsent(word, k -> new ArrayList<>());
                    if (codes.isEmpty() || codes.getFirst().length() == length) codes.add(code);
                }
            assertEquals(1 << n, optimal.size());
            for (var entry : optimal.entrySet()) {
                var actual = analyzer.analyze(OnsetSequence.fromBits(entry.getKey()));
                List<String> codes = entry.getValue();
                assertEquals(codes.getFirst(), actual.canonicalCode(), entry.getKey());
                assertEquals(BigInteger.valueOf(codes.size()), actual.count(), entry.getKey());
                assertEquals(codes, actual.alternatives(codes.size()), entry.getKey());
            }
        }
    }

    @Test
    void completeN16HistogramAndIndependentRoundTrips() throws Exception {
        Map<Integer, Integer> actual = new TreeMap<>();
        for (int mask = 0; mask < 65536; mask++) {
            String bits = binary(mask, 16);
            var result = analyzer.analyze(OnsetSequence.fromBits(bits));
            actual.merge(result.totalBits(), 1, Integer::sum);
            assertEquals(bits, decoder.decode(result.canonicalCode(), 16).bits());
        }
        Map<Integer, Integer> expected = new TreeMap<>();
        for (var e :
                fixtures()
                        .getAsJsonObject("verification")
                        .getAsJsonObject("completeN16Histogram")
                        .entrySet())
            expected.put(Integer.valueOf(e.getKey()), e.getValue().getAsInt());
        assertEquals(expected, actual);
    }

    @Test
    void independentConditionalMinimaWithActualSmallHistories() {
        for (int hLength = 0; hLength <= 5; hLength++)
            for (int h = 0; h < (1 << hLength); h++) {
                String history = hLength == 0 ? "" : binary(h, hLength);
                for (int n = 1; n <= 4; n++) {
                    var partition = Segmentation.equal(n, n, null);
                    Map<String, List<String>> optimal = new HashMap<>();
                    for (int length = 2; length <= n + 4; length++)
                        for (int value = 0; value < (1 << length); value++) {
                            String code = binary(value, length), word;
                            try {
                                word =
                                        decoder.decode(
                                                        code,
                                                        n,
                                                        Scope.CAUSAL_WINDOWS,
                                                        partition,
                                                        history)
                                                .bits();
                            } catch (IllegalArgumentException invalid) {
                                continue;
                            }
                            List<String> codes =
                                    optimal.computeIfAbsent(word, k -> new ArrayList<>());
                            if (codes.isEmpty() || codes.getFirst().length() == length)
                                codes.add(code);
                        }
                    assertEquals(1 << n, optimal.size());
                    for (var entry : optimal.entrySet()) {
                        var result =
                                analyzer.conditional(
                                        OnsetSequence.fromBits(entry.getKey()), history);
                        assertEquals(
                                entry.getValue().getFirst(),
                                result.canonicalCode(),
                                history + " -> " + entry.getKey());
                        assertEquals(BigInteger.valueOf(entry.getValue().size()), result.count());
                    }
                }
            }
    }

    static String binary(int value, int length) {
        String bits = Integer.toBinaryString(value);
        return "0".repeat(length - bits.length()) + bits;
    }
}
