package syrincs.a_domain.rhythm.metricstate;

import static org.junit.jupiter.api.Assertions.*;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestFactory;

import java.math.BigInteger;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/** Hand-calculated contracts; a deviation is a finding and must not be fixed in the fixture. */
class MetricStateFixtureTest {
    private static final Path FIXTURES =
            Path.of(
                    "doc/rhythm-generator/specs-metrischer-spielzustand/fixtures-metric-state-v1.json");

    private final MetricStateAnalyzer analyzer = new MetricStateAnalyzer();

    private static JsonArray cases() throws Exception {
        JsonObject root = JsonParser.parseString(Files.readString(FIXTURES)).getAsJsonObject();
        assertEquals(MetricStateAnalyzer.VERSION, root.get("measure").getAsString());
        assertEquals(Instruction.BITS, root.get("bitsPerInstruction").getAsInt());
        return root.getAsJsonArray("cases");
    }

    @Test
    void fixtureFileContainsAllPublishedCases() throws Exception {
        assertEquals(24, cases().size());
    }

    @TestFactory
    List<DynamicTest> everyHandCalculatedCase() throws Exception {
        List<DynamicTest> tests = new ArrayList<>();
        for (JsonElement element : cases()) {
            JsonObject c = element.getAsJsonObject();
            String id = c.get("id").getAsString();
            tests.add(DynamicTest.dynamicTest(id, () -> check(id, c)));
        }
        return tests;
    }

    private void check(String id, JsonObject c) {
        SoundCondition sound = SoundCondition.valueOf(c.get("sound").getAsString());
        MetricFrame frame =
                new MetricFrame(c.get("beatSlots").getAsInt(), c.get("barSlots").getAsInt(), sound);
        MetricStateAnalysis result =
                analyzer.analyze(MetricExample.parse(frame, c.get("events").getAsString()), 64);

        assertEquals(c.get("valueBits").getAsInt(), result.valueBits(), id + " valueBits");

        Set<List<String>> expected = new LinkedHashSet<>();
        for (JsonElement sequence : c.getAsJsonArray("minimalSequences")) {
            List<String> codes = new ArrayList<>();
            for (JsonElement code : sequence.getAsJsonArray()) codes.add(code.getAsString());
            expected.add(codes);
        }
        assertTrue(result.minimalSequencesComplete(), id);
        assertEquals(expected, result.minimalSequenceCodes(), id + " minimalSequences");

        if (c.has("valueAfterBars")) {
            List<Integer> values = new ArrayList<>();
            for (JsonElement value : c.getAsJsonArray("valueAfterBars")) values.add(value.getAsInt());
            assertEquals(values, result.valueAfterBars(), id + " valueAfterBars");
        }

        // Internal consistency of the result object.
        assertEquals(result.valueBits(), result.instructionCount() * Instruction.BITS, id);
        assertEquals(
                new MetricStateMeasurementKey(
                        "metric-state-v1", sound, frame.beatSlots(), frame.barSlots()),
                result.key(),
                id);
        assertFalse(result.minimalPathsTruncated(), id);
        assertEquals(BigInteger.valueOf(result.minimalPaths().size()), result.minimalPathCount(), id);
        for (MetricInterpretation path : result.minimalPaths()) {
            assertEquals(result.valueBits(), path.costBits(), id);
            assertEquals(result.example().slots(), path.protocol().size(), id);
            assertTrue(result.minimalSequences().contains(path.instructions()), id);
        }
        assertEquals(
                result.valueBits(),
                result.valueAfterBars().get(result.valueAfterBars().size() - 1),
                id);
        if (sound == SoundCondition.SUSTAINED)
            assertEquals(BigInteger.ONE, result.minimalPathCount(), id + " no 5a/5b tie under G");
    }
}
