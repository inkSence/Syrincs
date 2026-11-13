package syrincs.c_adapters.cli;

import static org.junit.jupiter.api.Assertions.*;

import com.google.gson.*;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import picocli.CommandLine;

import syrincs.b_application.rhythm.information.RhythmInformationFacade;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.nio.file.Files;
import java.nio.file.Path;

class RhythmInfoCliTest {
    @TempDir Path temporary;

    record Run(int code, String out, String err) {
        JsonObject json() {
            return JsonParser.parseString(out).getAsJsonObject();
        }
    }

    private Run run(String... args) {
        var command =
                new CommandLine(new RootCmd(null, null, null, null, new RhythmInformationFacade()));
        var out = new StringWriter();
        var err = new StringWriter();
        command.setOut(new PrintWriter(out));
        command.setErr(new PrintWriter(err));
        return new Run(command.execute(args), out.toString(), err.toString());
    }

    @Test
    void newAnalysisAndDecodeExposeExactCostsAndDistinctScopes() {
        var global = run("rhythm-info", "analyze", "xooo xooo xooo xooo", "--format", "json");
        assertEquals(0, global.code, global.err);
        assertEquals("", global.err);
        assertEquals(13, global.json().getAsJsonObject("primary").get("totalBits").getAsInt());
        assertEquals(
                "GLOBAL_SCOPE_HAS_NO_LOCAL_PROFILE",
                global.json()
                        .getAsJsonObject("primary")
                        .get("windowMetricsUnavailableReason")
                        .getAsString());
        assertEquals(
                "NO_BAR_GROUPING",
                global.json()
                        .getAsJsonObject("primary")
                        .get("barMetricsUnavailableReason")
                        .getAsString());
        var causal =
                run(
                        "rhythm-info",
                        "analyze",
                        "xooo xooo xooo xooo",
                        "--scope",
                        "causal",
                        "--window-slots",
                        "4",
                        "--bar-slots",
                        "16",
                        "--include-global",
                        "--format",
                        "json");
        assertEquals(0, causal.code, causal.err);
        assertEquals(
                22, causal.json().getAsJsonObject("primary").get("continuationTotal").getAsInt());
        assertEquals(
                13, causal.json().getAsJsonObject("additionalGlobal").get("totalBits").getAsInt());
        assertTrue(
                causal.json()
                        .getAsJsonObject("primary")
                        .get("windowMetricsUnavailableReason")
                        .isJsonNull());
        assertTrue(
                causal.json()
                        .getAsJsonObject("primary")
                        .get("barMetricsUnavailableReason")
                        .isJsonNull());
        var decoded =
                run(
                        "rhythm-info",
                        "decode",
                        "--slots",
                        "16",
                        "--code",
                        "0101000100010001000",
                        "--analyze",
                        "--format",
                        "json");
        assertEquals(0, decoded.code, decoded.err);
        assertEquals(19, decoded.json().get("witnessLength").getAsInt());
        assertEquals(13, decoded.json().getAsJsonObject("analysis").get("totalBits").getAsInt());
    }

    @Test
    void invalidNewOptionsAndCodeFailAsInputWithoutPartialStdout() {
        assertEquals(2, run("rhythm-info", "analyze", "x-o").code);
        assertEquals(2, run("rhythm-info", "analyze", "xooo", "--scope", "causal").code);
        assertEquals(2, run("rhythm-info", "analyze", "xooo", "--tempo", "108").code);
        assertEquals(2, run("rhythm-info", "decode", "--slots", "16", "--code", "0000").code);
        var invalid =
                run(
                        "rhythm-info",
                        "generate",
                        "--slots",
                        "4",
                        "--info",
                        "3",
                        "--strategy",
                        "exhaustive",
                        "--required-positions",
                        "0",
                        "--forbidden-positions",
                        "0",
                        "--format",
                        "json");
        assertEquals(2, invalid.code);
        assertEquals("", invalid.out);
        var error = JsonParser.parseString(invalid.err).getAsJsonObject();
        assertEquals("rhythm-info-result-v1", error.get("schemaVersion").getAsString());
        assertEquals("error", error.get("kind").getAsString());
    }

    @Test
    void generationStatusesAndLimitAreVisibleThroughRealCli() {
        var full =
                run(
                        "rhythm-info",
                        "generate",
                        "--slots",
                        "4",
                        "--info-min",
                        "3",
                        "--info-max",
                        "7",
                        "--strategy",
                        "exhaustive",
                        "--limit",
                        "2",
                        "--format",
                        "json");
        assertEquals(0, full.code, full.err);
        assertEquals("16", full.json().get("totalMatches").getAsString());
        assertEquals(2, full.json().get("returnedCount").getAsInt());
        var partial =
                run(
                        "rhythm-info",
                        "generate",
                        "--slots",
                        "4",
                        "--info",
                        "7",
                        "--strategy",
                        "code-length",
                        "--attempt-budget",
                        "1",
                        "--format",
                        "json");
        assertEquals(3, partial.code, partial.err);
        assertFalse(partial.json().get("searchComplete").getAsBoolean());
        assertTrue(partial.json().get("totalMatches").isJsonNull());
        assertEquals("BUDGET_EXHAUSTED", partial.json().get("stopReason").getAsString());
    }

    @Test
    void jsonRequestRejectsUnknownDuplicatesAndMixedFlags() throws Exception {
        String valid =
                """
{"schemaVersion":"rhythm-info-request-v1","kind":"analyze","frame":{"slots":4},"onsets":"xooo"}
""";
        var path = temporary.resolve("request.json");
        Files.writeString(path, valid);
        assertEquals(
                0,
                run("rhythm-info", "analyze", "--request", path.toString(), "--format", "json")
                        .code);
        assertEquals(
                2,
                run("rhythm-info", "analyze", "--request", path.toString(), "--tempo", "108").code);
        Files.writeString(path, valid.replace("\"onsets\":", "\"typo\":false,\"onsets\":"));
        assertEquals(2, run("rhythm-info", "analyze", "--request", path.toString()).code);
        Files.writeString(path, valid.replace("\"slots\":4", "\"slots\":4,\"slots\":4"));
        assertEquals(2, run("rhythm-info", "analyze", "--request", path.toString()).code);
        Files.writeString(path, "{\"schemaVersion\":");
        assertEquals(2, run("rhythm-info", "analyze", "--request", path.toString()).code);
    }

    @Test
    void largeSeedRemainsAnExactDecimalStringInJsonAndOverflowIsInputError() throws Exception {
        String request =
                """
{"schemaVersion":"rhythm-info-request-v1","kind":"generate","frame":{"slots":1},
"target":{"information":3},"search":{"strategy":"BOUNDED_RANDOM", "attemptBudget":"1",
"resultLimit":1,"seed":"9223372036854775807"}}
""";
        var path = temporary.resolve("seed.json");
        Files.writeString(path, request);
        var result =
                run("rhythm-info", "generate", "--request", path.toString(), "--format", "json");
        assertEquals(0, result.code, result.err);
        assertEquals(
                "9223372036854775807",
                result.json().getAsJsonObject("request").get("seed").getAsString());
        assertTrue(
                result.json()
                        .getAsJsonObject("request")
                        .get("seed")
                        .getAsJsonPrimitive()
                        .isString());
        Files.writeString(path, request.replace("9223372036854775807", "9223372036854775808"));
        assertEquals(2, run("rhythm-info", "generate", "--request", path.toString()).code);
    }

    @Test
    void exactEventTimesArePreservedAndNeverQuantizedOrMerged() throws Exception {
        String request =
                """
{"schemaVersion":"rhythm-info-request-v1","kind":"analyze","frame":{"slots":4,"time":{"status":"KNOWN","slotDurationMs":"125/1"}},"eventTimesMs":["0/1","250/1"]}
""";
        var path = temporary.resolve("events.json");
        Files.writeString(path, request);
        var valid = run("rhythm-info", "analyze", "--request", path.toString(), "--format", "json");
        assertEquals(0, valid.code, valid.err);
        assertEquals(
                "xoxo",
                valid.json().getAsJsonObject("primary").get("normalizedOnsets").getAsString());
        assertEquals(2, valid.json().getAsJsonArray("sourceEventTimesMs").size());
        Files.writeString(path, request.replace("250/1", "249/1"));
        assertEquals(2, run("rhythm-info", "analyze", "--request", path.toString()).code);
        Files.writeString(path, request.replace("250/1", "0/1"));
        assertEquals(2, run("rhythm-info", "analyze", "--request", path.toString()).code);
    }

    @Test
    void tempoIsExactAndLegacyComparisonStaysInSymbols() {
        var run =
                run(
                        "rhythm-info",
                        "analyze",
                        "xooo xooo xooo xooo",
                        "--tempo",
                        "108",
                        "--ticks-per-quarter",
                        "4",
                        "--time-signature",
                        "4/4",
                        "--compare-legacy",
                        "--format",
                        "json");
        assertEquals(0, run.code, run.err);
        assertEquals(
                "1250/9",
                run.json()
                        .getAsJsonObject("primary")
                        .getAsJsonObject("frame")
                        .getAsJsonObject("time")
                        .get("slotDurationMs")
                        .getAsString());
        assertEquals(13, run.json().getAsJsonObject("primary").get("totalBits").getAsInt());
        assertEquals(
                "CODE_SYMBOLS",
                run.json().getAsJsonObject("legacyComparison").get("unit").getAsString());
        assertEquals(1, run.json().getAsJsonObject("legacyComparison").get("total").getAsInt());
    }

    @Test
    void explicitUnequalWindowsCanBeGroupedIntoMultipleBars() {
        var result =
                run(
                        "rhythm-info",
                        "analyze",
                        "xooo".repeat(8),
                        "--scope",
                        "causal",
                        "--boundaries",
                        "0,4,8,16,20,24,32",
                        "--bar-slots",
                        "16",
                        "--format",
                        "json");
        assertEquals(0, result.code, result.err);
        assertEquals(
                2,
                result.json().getAsJsonObject("primary").getAsJsonArray("barContributions").size());
        var invalid =
                run(
                        "rhythm-info",
                        "analyze",
                        "xooo".repeat(8),
                        "--scope",
                        "causal",
                        "--boundaries",
                        "0,4,8,20,24,32",
                        "--bar-slots",
                        "16");
        assertEquals(2, invalid.code);
    }

    @Test
    void oldAnalyzeEndpointDoesNotAcquireMeasureOption() {
        assertEquals(
                2,
                run("analyze", "rhythm", "xooo xooo xooo xooo", "--measure", "description-v1")
                        .code);
        var help = run("analyze", "rhythm", "--help");
        assertFalse(help.out.contains("--measure"));
        assertTrue(help.out.contains("--details"));
        assertTrue(run("rhythm-info", "generate", "--help").out.contains("--attempt-budget"));
    }
}
