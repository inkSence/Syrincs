package syrincs.c_adapters.cli;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import picocli.CommandLine;
import syrincs.a_domain.hindemith.HindemithChord;
import syrincs.a_domain.rhythm.FakeMidiOutputPort;
import syrincs.a_domain.rhythm.HuffmanRhythm;
import syrincs.a_domain.rhythm.Pattern;
import syrincs.a_domain.rhythm.RhythmSpec;
import syrincs.a_domain.rhythm.VoiceSpec;
import syrincs.b_application.AnalyseChordByHindemithUseCase;
import syrincs.b_application.AnalyseRhythmUseCase;
import syrincs.b_application.GenerateChordsUseCase;
import syrincs.b_application.FilteringRhythmRepository;
import syrincs.b_application.AppDefaults;
import syrincs.b_application.GetHindemithChordsFromDbUseCase;
import syrincs.b_application.PersistHindemithChordUseCase;
import syrincs.b_application.PlayHuffmanRhythmsUseCase;
import syrincs.b_application.PlaybackRhythmUseCase;
import syrincs.b_application.SendToMidiUseCase;
import syrincs.b_application.UseCaseInteractor;
import syrincs.b_application.ValidatePatternsUseCase;
import syrincs.b_application.ports.HindemithChordRepositoryPort;
import syrincs.b_application.ports.MidiDeviceQueryPort;
import syrincs.b_application.ports.RhythmPlaybackPort;
import syrincs.b_application.ports.RhythmRepository;
import syrincs.b_application.ports.dto.DeviationRange;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class RootCmdRhythmCliTest {

    @TempDir
    Path tempDir;

    private static final String RDL = """
            time: 4/4
            tempo: 120
            res-per-beat: 4
            bars: 1

            voice kick  note=36 channel=9 vel=90 gate=50
            voice snare note=38 channel=9 vel=90 gate=50

            pattern kick:  | x - - - | x - - - | x - - - | x - - - |
            pattern snare: | - - - - | x - - - | - - - - | x - - - |
            """;

    @Test
    void playRhythm_passesDeviceToPlaybackPort() throws Exception {
        CapturingRhythmPlaybackPort playback = new CapturingRhythmPlaybackPort();
        RootCmd root = buildRoot(playback, null);
        Path file = writeRhythmFile();

        int code = new CommandLine(root).execute("play", "rhythm", "--in", file.toString(), "--device", "Virtual Out");

        assertEquals(0, code);
        assertEquals(1, playback.calls);
        assertEquals("Virtual Out", playback.deviceNameSubstring);
    }

    @Test
    void playRhythmInfo_acceptsDeviceAfterInfoSubcommand() {
        CapturingRhythmPlaybackPort playback = new CapturingRhythmPlaybackPort();
        RootCmd root = buildRoot(playback, new StubRhythmRepository());

        int code = new CommandLine(root).execute("play", "rhythm", "info", "--device", "Virtual Out", "3");

        assertEquals(0, code);
        assertEquals(1, playback.calls);
        assertEquals("Virtual Out", playback.deviceNameSubstring);
    }

    @Test
    void playRhythmInfo_concatenatesMultipleGradesIntoOnePlayback() {
        CapturingRhythmPlaybackPort playback = new CapturingRhythmPlaybackPort();
        RootCmd root = buildRoot(playback, new StubRhythmRepository());

        int code = new CommandLine(root).execute("play", "rhythm", "info", "3", "2");

        assertEquals(0, code);
        assertEquals(1, playback.calls);
        assertEquals(2, playback.spec.bars);
        assertEquals(32, playback.pattern.voices().get("kick").length);
        assertEquals(32, playback.pattern.voices().get("snare").length);
    }

    @Test
    void playRhythmInfo_reportsHowToFillEmptyDatabase() {
        CapturingRhythmPlaybackPort playback = new CapturingRhythmPlaybackPort();
        RootCmd root = buildRoot(playback, new EmptyRhythmRepository());
        ByteArrayOutputStream error = new ByteArrayOutputStream();
        PrintStream originalErr = System.err;
        try {
            System.setErr(new PrintStream(error));

            int code = new CommandLine(root).execute("play", "rhythm", "info", "3");

            assertEquals(1, code);
            assertEquals(0, playback.calls);
        } finally {
            System.setErr(originalErr);
        }

        String text = error.toString(StandardCharsets.UTF_8);
        assertTrue(text.contains("No stored Huffman rhythms found for position 1 (info=3)"));
        assertTrue(text.contains("syrincs init"));
        assertTrue(text.contains("syrincs calculate rhythms"));
        assertTrue(text.contains("syrincs play rhythm info 3"));
    }

    @Test
    void oldDbRhythmSourceSubcommand_isNoLongerAccepted() {
        RootCmd root = buildRoot(new CapturingRhythmPlaybackPort(), new StubRhythmRepository());
        PrintStream originalErr = System.err;
        try {
            System.setErr(new PrintStream(new ByteArrayOutputStream()));

            int code = new CommandLine(root).execute("play", "rhythm", "db", "1");

            assertTrue(code != 0);
        } finally {
            System.setErr(originalErr);
        }
    }

    @Test
    void calculateRhythms_reportsErrorsWithoutStackTrace() {
        RootCmd root = buildRoot(new CapturingRhythmPlaybackPort(), new EmptyRhythmRepository());
        ByteArrayOutputStream error = new ByteArrayOutputStream();
        PrintStream originalErr = System.err;
        try {
            System.setErr(new PrintStream(error));

            int code = new CommandLine(root).execute("calculate", "rhythms");

            assertEquals(1, code);
        } finally {
            System.setErr(originalErr);
        }

        String text = error.toString(StandardCharsets.UTF_8);
        assertTrue(text.contains("[ERROR] GenerateAndPersistRhythmUseCase not wired in UseCaseInteractor"));
        assertTrue(!text.contains("\tat syrincs."));
    }

    @Test
    void analyzeRhythm_printsNormalizedRhythmInformationAndDeviation() {
        RootCmd root = buildRoot(new CapturingRhythmPlaybackPort(), null);
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        PrintStream originalOut = System.out;
        try {
            System.setOut(new PrintStream(output));

            int code = new CommandLine(root).execute("analyze", "rhythm", "xooo\txoxo\nxooo xoxo");

            assertEquals(0, code);
        } finally {
            System.setOut(originalOut);
        }

        String text = output.toString(StandardCharsets.UTF_8);
        assertEquals(String.format(
                "[ANALYZE] Rhythm=xoooxoxoxoooxoxo | Info=3 | Deviation=%.6f | Beats=[xooo, xoxo, xooo, xoxo]%n",
                0.4330127018922193), text);
    }

    @Test
    void analyzeRhythm_detailsAfterOnsetsShowsFirstProfileWithoutPlayback() {
        var playback = new CapturingRhythmPlaybackPort();
        String text = analyzeOutput(playback, "Xooo\txoxo\nxooo Xoxo", "--details");

        assertEquals(String.format(
                "[ANALYZE] Rhythm=xoooxoxoxoooxoxo | Info=3 | Deviation=%.6f | Beats=[xooo, xoxo, xooo, xoxo]%n",
                0.4330127018922193)
                + "[DETAILS] BeatInformation=[1, 1, 0, 1] | Mean=0.750000" + System.lineSeparator(), text);
        assertEquals(0, playback.calls);
    }

    @Test
    void analyzeRhythm_detailsBeforeOnsetsShowsDifferentDistribution() {
        var playback = new CapturingRhythmPlaybackPort();
        String text = analyzeOutput(playback, "--details", "xoxo xooo xoxo xooo");

        assertEquals(String.format(
                "[ANALYZE] Rhythm=xoxoxoooxoxoxooo | Info=3 | Deviation=%.6f | Beats=[xoxo, xooo, xoxo, xooo]%n",
                0.82915619758885)
                + "[DETAILS] BeatInformation=[2, 0, 1, 0] | Mean=0.750000" + System.lineSeparator(), text);
        assertEquals(0, playback.calls);
    }

    @Test
    void analyzeRhythm_detailsIncludesAllBeatsWithoutResetAtBarBoundary() {
        var playback = new CapturingRhythmPlaybackPort();
        String text = analyzeOutput(playback, "xoxo ".repeat(8), "--details");

        assertTrue(text.contains(String.format("Info=9 | Deviation=%.6f", 0.33071891388307384)));
        assertTrue(text.contains("BeatInformation=[2, 1, 1, 1, 1, 1, 1, 1] | Mean=1.125000"));
        assertEquals(0, playback.calls);
    }

    @Test
    void analyzeRhythm_helpDescribesDetailsWithoutRunningAnalysis() {
        var output = new StringWriter();
        var command = new CommandLine(new RootCmd(null, (MidiDeviceQueryPort) null));
        command.setOut(new PrintWriter(output));

        assertEquals(0, command.execute("analyze", "rhythm", "--help"));
        assertTrue(output.toString().contains("--details"));
        assertTrue(output.toString().contains("BeatInformation"));
    }

    @Test
    void rootHelpIncludesRhythmAnalysisDetails() {
        var output = new ByteArrayOutputStream();
        PrintStream previous = System.out;
        try {
            System.setOut(new PrintStream(output));
            RootCmd.printExtendedHelp(new CommandLine(buildRoot(new CapturingRhythmPlaybackPort(), null)));
        } finally {
            System.setOut(previous);
        }

        String text = output.toString(StandardCharsets.UTF_8);
        assertTrue(text.contains("Subcommand 'analyze rhythm' usage:"));
        assertTrue(text.contains("--details"));
    }

    private String analyzeOutput(CapturingRhythmPlaybackPort playback, String... arguments) {
        var args = new ArrayList<>(List.of("analyze", "rhythm"));
        args.addAll(List.of(arguments));
        var output = new ByteArrayOutputStream();
        PrintStream previous = System.out;
        try {
            System.setOut(new PrintStream(output));
            assertEquals(0, new CommandLine(buildRoot(playback, null)).execute(args.toArray(String[]::new)));
        } finally {
            System.setOut(previous);
        }
        return output.toString(StandardCharsets.UTF_8);
    }

    @ParameterizedTest
    @MethodSource("inclusivePlaybackRequests")
    void playRhythmInfo_filtersByActualGradeAndInclusiveBounds(String[] arguments, DeviationRange expectedRange,
                                                              String expectedOnsets) {
        var playback = new CapturingRhythmPlaybackPort();
        var repo = freshCatalog();
        var args = new ArrayList<>(List.of("play", "rhythm", "info"));
        args.addAll(List.of(arguments));
        args.addAll(List.of("--device", "Virtual Out"));

        assertEquals(0, new CommandLine(buildRoot(playback, repo)).execute(args.toArray(String[]::new)));
        assertEquals(1, playback.calls);
        assertEquals("Virtual Out", playback.deviceNameSubstring);
        assertEquals(expectedRange, repo.lastRange);
        assertEquals(1, repo.rangeQueries);
        assertEquals(0, repo.strictQueries);
        assertPlayedOnsets(playback, expectedOnsets);
    }

    static Stream<Arguments> inclusivePlaybackRequests() {
        var low = new HuffmanRhythm(4, 4, 120, "xooo xoxo xooo xoxo");
        var high = new HuffmanRhythm(4, 4, 120, "xoxo xooo xoxo xooo");
        String l = Double.toString(low.getStandardDeviation()), h = Double.toString(high.getStandardDeviation());
        return Stream.of(
                Arguments.of(new String[]{"3", "--deviation-min", h}, new DeviationRange(high.getStandardDeviation(), null), high.getOnsetList()),
                Arguments.of(new String[]{"--deviation-max", l, "3"}, new DeviationRange(null, low.getStandardDeviation()), low.getOnsetList()),
                Arguments.of(new String[]{"3", "--deviation-min", l, "--deviation-max", l}, new DeviationRange(low.getStandardDeviation(), low.getStandardDeviation()), low.getOnsetList()),
                Arguments.of(new String[]{"0", "--deviation-min", "0"}, new DeviationRange(0.0, null), "o".repeat(16)),
                Arguments.of(new String[]{"0", "--deviation-max", "0"}, new DeviationRange(null, 0.0), "o".repeat(16)));
    }

    @Test
    void playRhythmInfo_withoutOptionsKeepsStrictDefaultAndExcludesEquality() {
        var low = new HuffmanRhythm(4, 4, 120, "xooo xoxo xooo xoxo");
        var high = new HuffmanRhythm(4, 4, 120, "xoxo xooo xoxo xooo");
        var repo = new FilteringRhythmRepository(new FilteringRhythmRepository.Entry(low, 3, 0.7),
                FilteringRhythmRepository.Entry.fresh(high));
        var playback = new CapturingRhythmPlaybackPort();

        assertEquals(0, new CommandLine(buildRoot(playback, repo)).execute("play", "rhythm", "info", "3"));
        assertEquals(1, repo.strictQueries);
        assertEquals(AppDefaults.MIN_HUFFMAN_RHYTHM_DEVIATION, repo.lastStrictMinimum);
        assertEquals(0, repo.rangeQueries);
        assertPlayedOnsets(playback, high.getOnsetList());

        assertEquals(0, new CommandLine(buildRoot(playback, repo)).execute("play", "rhythm", "info", "3",
                "--deviation-min", "0.7", "--deviation-max", "0.7"));
        assertEquals(1, repo.rangeQueries);
        assertPlayedOnsets(playback, low.getOnsetList());
    }

    @ParameterizedTest
    @MethodSource("invalidPlaybackBounds")
    void playRhythmInfo_invalidBoundsFailBeforeQueryOrPlayback(String[] options, String errorText) {
        var repo = freshCatalog();
        var playback = new CapturingRhythmPlaybackPort();
        var args = new ArrayList<>(List.of("play", "rhythm", "info", "3"));
        args.addAll(List.of(options));
        var error = new ByteArrayOutputStream();
        var previous = System.err;
        try {
            System.setErr(new PrintStream(error));
            assertEquals(1, new CommandLine(buildRoot(playback, repo)).execute(args.toArray(String[]::new)));
        } finally {
            System.setErr(previous);
        }
        assertTrue(error.toString(StandardCharsets.UTF_8).contains(errorText));
        assertTrue(!error.toString(StandardCharsets.UTF_8).contains("\tat syrincs."));
        assertEquals(0, repo.queries);
        assertEquals(0, playback.calls);
    }

    static Stream<Arguments> invalidPlaybackBounds() {
        return Stream.of(
                Arguments.of(new String[]{"--deviation-min=-0.1"}, "--deviation-min"),
                Arguments.of(new String[]{"--deviation-max=-0.1"}, "--deviation-max"),
                Arguments.of(new String[]{"--deviation-min=NaN"}, "finite and non-negative"),
                Arguments.of(new String[]{"--deviation-max=NaN"}, "finite and non-negative"),
                Arguments.of(new String[]{"--deviation-min=Infinity"}, "--deviation-min"),
                Arguments.of(new String[]{"--deviation-max=Infinity"}, "--deviation-max"),
                Arguments.of(new String[]{"--deviation-min=-Infinity"}, "--deviation-min"),
                Arguments.of(new String[]{"--deviation-max=-Infinity"}, "--deviation-max"),
                Arguments.of(new String[]{"--deviation-min=0.8", "--deviation-max=0.2"}, "must not exceed"));
    }

    @Test
    void playRhythmInfo_helpAndRootHelpExplainBounds() {
        var command = new CommandLine(new RootCmd(null, (MidiDeviceQueryPort) null));
        var help = new StringWriter();
        command.setOut(new PrintWriter(help));
        assertEquals(0, command.execute("play", "rhythm", "info", "--help"));
        assertTrue(help.toString().contains("--deviation-min"));
        assertTrue(help.toString().contains("--deviation-max"));
        assertTrue(help.toString().contains("Inclusive"));
        assertTrue(help.toString().contains("no implicit minimum"));

        var output = new ByteArrayOutputStream();
        var previous = System.out;
        try {
            System.setOut(new PrintStream(output));
            RootCmd.printExtendedHelp(command);
        } finally {
            System.setOut(previous);
        }
        assertTrue(output.toString(StandardCharsets.UTF_8).contains("Subcommand 'play rhythm info' usage:"));
        assertTrue(output.toString(StandardCharsets.UTF_8).contains("--deviation-max"));
    }

    private static FilteringRhythmRepository freshCatalog() {
        return new FilteringRhythmRepository(List.of(
                new HuffmanRhythm(4, 4, 120, "xooo xoxo xooo xoxo"),
                new HuffmanRhythm(4, 4, 120, "xoxo xooo xoxo xooo"),
                new HuffmanRhythm(4, 4, 120, "xoxo xooo xooo xooo"),
                new HuffmanRhythm(4, 4, 120, "o".repeat(16))));
    }

    @Test
    void seededPlaybackPrintsExactSelectionBeforeOneCombinedPlaybackAndDryRunIsIdentical() {
        var output = new StringWriter();
        var playback = new CapturingRhythmPlaybackPort() {
            @Override public void play(Pattern pattern, RhythmSpec spec, List<VoiceSpec> voices, String device) {
                assertTrue(output.toString().contains("Position=4 | RequestedInfo=3"));
                super.play(pattern, spec, voices, device);
            }
        };
        String[] arguments = {"3", "0", "3", "3", "--deviation-min", "0", "--seed", "42", "--device", "Virtual Out"};
        var command = new CommandLine(buildRoot(playback, freshCatalog())).setOut(new PrintWriter(output));
        var args = new ArrayList<>(List.of("play", "rhythm", "info"));
        args.addAll(List.of(arguments));

        assertEquals(0, command.execute(args.toArray(String[]::new)));
        assertEquals(1, playback.calls);
        assertEquals(4, playback.spec.bars);
        assertEquals("Virtual Out", playback.deviceNameSubstring);
        assertPlayedOnsets(playback, "xoxoxoooxoxoxooo" + "o".repeat(16) + "xoxoxoooxoxoxooo" + "xoooxoxoxoooxoxo");
        String expected = "[SELECTION] Seed=42 | Algorithm=java.util.Random | DeviationFilter=DeviationRange[min=0.0, max=null] | Count=4" + System.lineSeparator()
                + "[SELECTED] Position=1 | RequestedInfo=3 | Time=4/4 | Rhythm=xoxoxoooxoxoxooo | BeatInformation=[2, 0, 1, 0] | Info=3 | Deviation=0.829156" + System.lineSeparator()
                + "[SELECTED] Position=2 | RequestedInfo=0 | Time=4/4 | Rhythm=oooooooooooooooo | BeatInformation=[0, 0, 0, 0] | Info=0 | Deviation=0.000000" + System.lineSeparator()
                + "[SELECTED] Position=3 | RequestedInfo=3 | Time=4/4 | Rhythm=xoxoxoooxoxoxooo | BeatInformation=[2, 0, 1, 0] | Info=3 | Deviation=0.829156" + System.lineSeparator()
                + "[SELECTED] Position=4 | RequestedInfo=3 | Time=4/4 | Rhythm=xoooxoxoxoooxoxo | BeatInformation=[1, 1, 0, 1] | Info=3 | Deviation=0.433013" + System.lineSeparator();
        assertEquals(expected, output.toString());

        args.add("--dry-run");
        var dryPlayback = new CapturingRhythmPlaybackPort();
        var root = buildRoot(dryPlayback, freshCatalog());
        var dryOutput = new StringWriter();
        assertEquals(0, new CommandLine(withForbiddenDeviceQueries(root)).setOut(new PrintWriter(dryOutput))
                .execute(args.toArray(String[]::new)));
        assertEquals(expected, dryOutput.toString());
        assertEquals(0, dryPlayback.calls);
    }

    @Test
    void unseededDryRunShowsActualSeedAndCanBeReplayed() {
        var playback = new CapturingRhythmPlaybackPort();
        var first = runInfo(freshCatalog(), playback, "3", "3", "--deviation-min", "0", "--dry-run");
        assertEquals(0, first.code());
        var seed = java.util.regex.Pattern.compile("Seed=(-?\\d+)").matcher(first.output());
        assertTrue(seed.find());
        var second = runInfo(freshCatalog(), playback, "3", "3", "--deviation-min", "0", "--seed", seed.group(1), "--dry-run");
        assertEquals(0, second.code());
        assertEquals(first.output(), second.output());
        assertEquals(0, playback.calls);
    }

    @Test
    void incompleteSelectionReportsEveryMissingPositionWithoutPrintingOrPlayingPartialSelection() {
        for (boolean dryRun : List.of(false, true)) {
            var playback = new CapturingRhythmPlaybackPort();
            var args = new ArrayList<>(List.of("3", "99", "0", "98", "99", "--deviation-min", "0", "--seed", "42"));
            if (dryRun) args.add("--dry-run");
            var result = runInfo(freshCatalog(), playback, args.toArray(String[]::new));
            assertEquals(1, result.code());
            assertEquals("", result.output());
            assertTrue(result.error().contains("position 2 (info=99)"));
            assertTrue(result.error().contains("position 4 (info=98)"));
            assertTrue(result.error().contains("position 5 (info=99)"));
            assertEquals(0, playback.calls);
        }
    }

    @Test
    void seedAcceptsFullSignedLongRangeAndRejectsInvalidValuesBeforeQuery() {
        for (String seed : List.of("-9223372036854775808", "9223372036854775807", "-1", "0")) {
            var result = runInfo(freshCatalog(), new CapturingRhythmPlaybackPort(), "3", "--seed", seed, "--dry-run");
            assertEquals(0, result.code());
            assertTrue(result.output().contains("Seed=" + seed + " |"));
        }
        for (String seed : List.of("not-a-long", "9223372036854775808")) {
            var repo = freshCatalog();
            var playback = new CapturingRhythmPlaybackPort();
            var result = runInfo(repo, playback, "3", "--seed", seed, "--dry-run");
            assertTrue(result.code() != 0);
            assertTrue(!result.error().isBlank());
            assertEquals(0, repo.queries);
            assertEquals(0, playback.calls);
        }
    }

    @Test
    void negativePlaybackGradeAndRepositoryFailureDoNotPrintOrPlaySelection() {
        var repo = freshCatalog();
        var playback = new CapturingRhythmPlaybackPort();
        var negative = runInfo(repo, playback, "3", "-1", "--seed", "42", "--dry-run");
        assertEquals(1, negative.code());
        assertTrue(negative.error().contains("position 2 must be non-negative"));
        assertEquals(0, repo.queries);
        var failure = new FilteringRhythmRepository(List.of()) {
            @Override public List<HuffmanRhythm> getAllByInformationAndMinDeviation(Integer info, Double minimum) {
                throw new IllegalStateException("Database unavailable");
            }
        };
        var result = runInfo(failure, playback, "3", "--seed", "42", "--dry-run");
        assertEquals(1, result.code());
        assertEquals("", result.output());
        assertTrue(result.error().contains("Database unavailable"));
        assertTrue(!result.error().contains("\tat syrincs."));
        assertEquals(0, playback.calls);
    }

    @Test
    void playbackHelpDocumentsSeedDryRunAndRootHelpIncludesBothOptions() {
        var command = new CommandLine(new RootCmd(null, (MidiDeviceQueryPort) null));
        var help = new StringWriter();
        command.setOut(new PrintWriter(help));
        assertEquals(0, command.execute("play", "rhythm", "info", "--help"));
        assertTrue(help.toString().contains("--seed"));
        assertTrue(help.toString().contains("java.util.Random"));
        assertTrue(help.toString().contains("--dry-run"));
        var extended = new ByteArrayOutputStream();
        var previous = System.out;
        try {
            System.setOut(new PrintStream(extended));
            RootCmd.printExtendedHelp(command);
        } finally {
            System.setOut(previous);
        }
        assertTrue(extended.toString(StandardCharsets.UTF_8).contains("--seed"));
        assertTrue(extended.toString(StandardCharsets.UTF_8).contains("--dry-run"));
    }

    private record InfoRun(int code, String output, String error) {}

    private InfoRun runInfo(RhythmRepository repo, CapturingRhythmPlaybackPort playback, String... arguments) {
        var output = new StringWriter();
        var error = new StringWriter();
        var args = new ArrayList<>(List.of("play", "rhythm", "info"));
        args.addAll(List.of(arguments));
        int code = new CommandLine(withForbiddenDeviceQueries(buildRoot(playback, repo)))
                .setOut(new PrintWriter(output)).setErr(new PrintWriter(error)).execute(args.toArray(String[]::new));
        return new InfoRun(code, output.toString(), error.toString());
    }

    private static RootCmd withForbiddenDeviceQueries(RootCmd root) {
        return new RootCmd(root.interactor, root.midiInteractor, new MidiDeviceQueryPort() {
            @Override public List<syrincs.b_application.ports.dto.MidiEndpoint> listOutputs() { throw new AssertionError("MIDI query"); }
            @Override public syrincs.b_application.ports.dto.MidiEndpoint findOutput(String name) { throw new AssertionError("MIDI query"); }
        }, null);
    }

    @Test
    void searchRhythmsShowsUniqueSortedProfilesAndFiltersWithoutMidiAccess() {
        var low = new HuffmanRhythm(4, 4, 120, "xooo xoxo xooo xoxo");
        var high = new HuffmanRhythm(4, 4, 120, "xoxo xooo xoxo xooo");
        var repo = new FilteringRhythmRepository(List.of(high, low, high,
                new HuffmanRhythm(4, 4, 120, "XOOO XOXO XOOO XOXO")));
        var playback = new CapturingRhythmPlaybackPort();
        var root = buildRoot(playback, repo);
        var noDevices = new RootCmd(root.interactor, root.midiInteractor, new MidiDeviceQueryPort() {
            @Override public List<syrincs.b_application.ports.dto.MidiEndpoint> listOutputs() { throw new AssertionError("MIDI query"); }
            @Override public syrincs.b_application.ports.dto.MidiEndpoint findOutput(String name) { throw new AssertionError("MIDI query"); }
        }, null);
        var output = new StringWriter();
        var command = new CommandLine(noDevices).setOut(new PrintWriter(output));

        assertEquals(0, command.execute("search", "rhythms", "--info", "3"));
        assertEquals("[SEARCH] Info=3 | DeviationMin=none | DeviationMax=none | Limit=20 | Total=2 | Shown=2" + System.lineSeparator()
                + "[CANDIDATE] Time=4/4 | Rhythm=xoooxoxoxoooxoxo | BeatInformation=[1, 1, 0, 1] | Info=3 | Deviation=0.433013" + System.lineSeparator()
                + "[CANDIDATE] Time=4/4 | Rhythm=xoxoxoooxoxoxooo | BeatInformation=[2, 0, 1, 0] | Info=3 | Deviation=0.829156" + System.lineSeparator(), output.toString());
        assertEquals(new DeviationRange(null, null), repo.lastRange);
        assertEquals(0, repo.strictQueries);
        assertEquals(0, playback.calls);
    }

    @Test
    void searchRhythmsLimitRestrictsOutputNotTotalMatches() {
        var repo = freshCatalog();
        var playback = new CapturingRhythmPlaybackPort();
        var output = new StringWriter();
        int code = new CommandLine(buildRoot(playback, repo)).setOut(new PrintWriter(output))
                .execute("search", "rhythms", "--info", "3", "--limit", "1");
        assertEquals(0, code);
        assertTrue(output.toString().contains("Limit=1 | Total=2 | Shown=1"));
        assertEquals(1, output.toString().lines().filter(line -> line.startsWith("[CANDIDATE]")).count());
        assertTrue(output.toString().contains("Rhythm=xoooxoxoxoooxoxo"));
        assertEquals(0, playback.calls);
    }

    @Test
    void searchRhythmsDefaultLimitIsTwentyEvenForLargerCatalog() {
        var fixtures = new ArrayList<HuffmanRhythm>();
        for (int mask = 0; fixtures.size() < 25; mask++) {
            String bits = String.format("%16s", Integer.toBinaryString(mask)).replace(' ', '0');
            var rhythm = new HuffmanRhythm(4, 4, 120, bits.replace('0', 'o').replace('1', 'x'));
            if (rhythm.getInformation() == 3) fixtures.add(rhythm);
        }
        java.util.Collections.reverse(fixtures);
        fixtures.add(fixtures.getFirst());
        var playback = new CapturingRhythmPlaybackPort();
        var output = new StringWriter();
        int code = new CommandLine(buildRoot(playback, new FilteringRhythmRepository(fixtures))).setOut(new PrintWriter(output))
                .execute("search", "rhythms", "--info", "3");
        assertEquals(0, code);
        assertTrue(output.toString().contains("Limit=20 | Total=25 | Shown=20"));
        assertEquals(20, output.toString().lines().filter(line -> line.startsWith("[CANDIDATE]")).count());
        assertEquals(0, playback.calls);
    }

    @ParameterizedTest
    @MethodSource("inclusivePlaybackRequests")
    void searchRhythmsUsesSameInclusiveBounds(String[] arguments, DeviationRange expectedRange, String expectedOnsets) {
        // Reuse the actual-bound fixtures, but --info is an option rather than a positional grade.
        var args = new ArrayList<>(List.of("search", "rhythms"));
        for (int i = 0; i < arguments.length; i++) {
            if (arguments[i].startsWith("--")) {
                args.add(arguments[i]);
                args.add(arguments[++i]);
            } else {
                args.add("--info");
                args.add(arguments[i]);
            }
        }
        var repo = freshCatalog();
        var playback = new CapturingRhythmPlaybackPort();
        var output = new StringWriter();
        assertEquals(0, new CommandLine(buildRoot(playback, repo)).setOut(new PrintWriter(output)).execute(args.toArray(String[]::new)));
        assertEquals(expectedRange, repo.lastRange);
        assertTrue(output.toString().contains("Rhythm=" + expectedOnsets));
        assertEquals(0, repo.strictQueries);
        assertEquals(0, playback.calls);
    }

    @ParameterizedTest
    @MethodSource("invalidSearchRequests")
    void searchRhythmsRejectsInvalidRequestsBeforeQuery(String[] arguments) {
        var repo = freshCatalog();
        var playback = new CapturingRhythmPlaybackPort();
        var output = new StringWriter();
        var error = new StringWriter();
        var args = new ArrayList<>(List.of("search", "rhythms"));
        args.addAll(List.of(arguments));
        int code = new CommandLine(buildRoot(playback, repo)).setOut(new PrintWriter(output)).setErr(new PrintWriter(error))
                .execute(args.toArray(String[]::new));
        assertTrue(code != 0);
        assertTrue(!error.toString().isBlank());
        assertEquals("", output.toString());
        assertEquals(0, repo.queries);
        assertEquals(0, playback.calls);
    }

    static Stream<Arguments> invalidSearchRequests() {
        return Stream.of(new String[]{}, new String[]{"--info", "-1"}, new String[]{"--info", "3", "4"},
                new String[]{"--info", "3.5"}, new String[]{"--info", "3", "--limit", "0"},
                new String[]{"--info", "3", "--limit", "-2"},
                new String[]{"--info", "3", "--deviation-min", "-0.1"},
                new String[]{"--info", "3", "--deviation-max", "-0.1"},
                new String[]{"--info", "3", "--deviation-min", "NaN"},
                new String[]{"--info", "3", "--deviation-max", "Infinity"},
                new String[]{"--info", "3", "--deviation-min", "Infinity"},
                new String[]{"--info", "3", "--deviation-max", "NaN"},
                new String[]{"--info", "3", "--deviation-min", "0.9", "--deviation-max", "0.1"},
                new String[]{"--info", "3", "--beat-profile", ""},
                new String[]{"--info", "3", "--beat-profile="},
                new String[]{"--info", "3", "--beat-profile", "1,1,1"},
                new String[]{"--info", "3", "--beat-profile", "1,1,0,1,0"},
                new String[]{"--info", "3", "--beat-profile", "1,-1,2,1"},
                new String[]{"--info", "3", "--beat-profile", "1,no,0,1"},
                new String[]{"--info", "3", "--beat-profile", "1,1.0,0,1"},
                new String[]{"--info", "3", "--beat-profile", "1,,1,1"},
                new String[]{"--info", "3", "--beat-profile", "1,1,0,1,"},
                new String[]{"--info", "3", "--beat-profile", "2147483648,0,0,0"},
                new String[]{"--info", "3", "--beat-profile", "2147483647,2147483647,4,1"},
                new String[]{"--info", "4", "--beat-profile", "1,1,0,1"},
                new String[]{"--info", "3", "--peak-beat", "0"},
                new String[]{"--info", "3", "--peak-beat", "5"},
                new String[]{"--info", "3", "--peak-beat", "-1"},
                new String[]{"--info", "3", "--peak-beat", "two"},
                new String[]{"--info", "3", "--beat-profile", "1,1,0,1", "--peak-beat", "3"},
                new String[]{"--info", "3", "--beat-profile", "2,0,1,0", "--peak-beat", "2"})
                .map(args -> Arguments.of((Object) args));
    }

    @Test
    void searchRhythmsExactProfileAndPeakAreAndedWithDeviationBeforeLimit() {
        var low = new HuffmanRhythm(4, 4, 120, "xooo xoxo xooo xoxo");
        var high = new HuffmanRhythm(4, 4, 120, "xoxo xooo xoxo xooo");
        var repo = new FilteringRhythmRepository(List.of(high, low, high, low));
        var playback = new CapturingRhythmPlaybackPort();
        var output = new StringWriter();
        var error = new StringWriter();
        int code = new CommandLine(withForbiddenDeviceQueries(buildRoot(playback, repo)))
                .setOut(new PrintWriter(output)).setErr(new PrintWriter(error))
                .execute("search", "rhythms", "--info", "3", "--beat-profile", "2, 0, 1, 0",
                        "--peak-beat", "1", "--deviation-min", Double.toString(high.getStandardDeviation()),
                        "--deviation-max", Double.toString(high.getStandardDeviation()), "--limit", "1");
        assertEquals(0, code, error.toString());
        assertTrue(output.toString().contains("Limit=1 | Total=1 | Shown=1 | BeatProfile=[2, 0, 1, 0] | PeakBeat=1"));
        assertTrue(output.toString().contains("Rhythm=xoxoxoooxoxoxooo"));
        assertFalse(output.toString().contains("Rhythm=xoooxoxoxoooxoxo"));
        assertEquals(new DeviationRange(high.getStandardDeviation(), high.getStandardDeviation()), repo.lastRange);
        assertEquals(1, repo.rangeQueries);
        assertEquals(0, repo.strictQueries);
        assertEquals(0, playback.calls);

        output = new StringWriter();
        assertEquals(0, new CommandLine(buildRoot(playback, repo)).setOut(new PrintWriter(output))
                .execute("search", "rhythms", "--info", "3", "--beat-profile", "2,0,1,0", "--deviation-max", "0.5"));
        assertTrue(output.toString().contains("Total=0 | Shown=0"));
        assertEquals(1, output.toString().lines().count());

        // The first candidate in canonical order is LOW and must not consume the limit before profile filtering.
        output = new StringWriter();
        assertEquals(0, new CommandLine(buildRoot(playback, repo)).setOut(new PrintWriter(output))
                .execute("search", "rhythms", "--info", "3", "--beat-profile", "2,0,1,0", "--limit", "1"));
        assertTrue(output.toString().contains("Total=1 | Shown=1"));
        assertTrue(output.toString().contains("Rhythm=xoxoxoooxoxoxooo"));
    }

    @Test
    void searchRhythmsPeakCountsTiesIncludingSilenceAndDoesNotLimitTheCount() {
        var playback = new CapturingRhythmPlaybackPort();
        for (int beat = 1; beat <= 4; beat++) {
            var output = new StringWriter();
            assertEquals(0, new CommandLine(withForbiddenDeviceQueries(buildRoot(playback, freshCatalog())))
                    .setOut(new PrintWriter(output)).execute("search", "rhythms", "--info", "3",
                            "--peak-beat", Integer.toString(beat), "--limit", "1"));
            int total = beat == 1 ? 2 : beat == 3 ? 0 : 1;
            assertTrue(output.toString().contains("Total=" + total + " | Shown=" + Math.min(total, 1)));
            assertTrue(output.toString().contains("BeatProfile=none | PeakBeat=" + beat));
            if (beat != 3) assertTrue(output.toString().contains("Rhythm=xoooxoxoxoooxoxo"));

            output = new StringWriter();
            assertEquals(0, new CommandLine(buildRoot(playback, freshCatalog())).setOut(new PrintWriter(output))
                    .execute("search", "rhythms", "--info", "0", "--beat-profile", "0,0,0,0", "--peak-beat", Integer.toString(beat)));
            assertTrue(output.toString().contains("Total=1 | Shown=1"));
            assertTrue(output.toString().contains("BeatInformation=[0, 0, 0, 0]"));
        }
        assertEquals(0, playback.calls);
    }

    @Test
    void searchRhythmsValidAbsentProfileSucceedsAndContradictionsHaveUsefulErrors() {
        var repo = freshCatalog();
        var playback = new CapturingRhythmPlaybackPort();
        var output = new StringWriter();
        assertEquals(0, new CommandLine(buildRoot(playback, repo)).setOut(new PrintWriter(output))
                .execute("search", "rhythms", "--info", "3", "--beat-profile", "0,3,0,0"));
        assertTrue(output.toString().contains("Total=0 | Shown=0"));
        assertEquals(1, output.toString().lines().count());

        for (var options : List.of(new String[]{"--beat-profile", "1,1,0,1", "--peak-beat", "3"},
                new String[]{"--beat-profile", "1,1,1,1"})) {
            var empty = new FilteringRhythmRepository(List.of());
            var error = new StringWriter();
            output = new StringWriter();
            var args = new ArrayList<>(List.of("search", "rhythms", "--info", "3"));
            args.addAll(List.of(options));
            assertEquals(1, new CommandLine(buildRoot(playback, empty)).setOut(new PrintWriter(output))
                    .setErr(new PrintWriter(error)).execute(args.toArray(String[]::new)));
            assertTrue(error.toString().contains("--beat-profile"));
            assertTrue(error.toString().contains(options.length == 4 ? "--peak-beat" : "--info"));
            assertEquals("", output.toString());
            assertEquals(0, empty.queries);
        }
        assertEquals(0, playback.calls);
    }

    @Test
    void searchRhythmsDistinguishesEmptySearchFromRepositoryError() {
        var playback = new CapturingRhythmPlaybackPort();
        var output = new StringWriter();
        assertEquals(0, new CommandLine(buildRoot(playback, new EmptyRhythmRepository())).setOut(new PrintWriter(output))
                .execute("search", "rhythms", "--info", "3"));
        assertTrue(output.toString().contains("Total=0 | Shown=0"));
        assertEquals(1, output.toString().lines().count());

        var failingRepo = new FilteringRhythmRepository(List.of()) {
            @Override public List<HuffmanRhythm> getAllByInformationAndDeviationRange(Integer info, DeviationRange range) {
                throw new RuntimeException("Database unavailable");
            }
        };
        output = new StringWriter();
        var error = new StringWriter();
        assertEquals(1, new CommandLine(buildRoot(playback, failingRepo)).setOut(new PrintWriter(output)).setErr(new PrintWriter(error))
                .execute("search", "rhythms", "--info", "3"));
        assertEquals("", output.toString());
        assertTrue(error.toString().contains("[ERROR] Database unavailable"));
        assertTrue(!error.toString().contains("\tat syrincs."));
        assertEquals(0, playback.calls);
    }

    @Test
    void searchRhythmsHelpAndRootHelpDocumentCommandAndOptions() {
        var output = new StringWriter();
        var command = new CommandLine(new RootCmd(null, (MidiDeviceQueryPort) null)).setOut(new PrintWriter(output));
        assertEquals(0, command.execute("search", "rhythms", "--help"));
        for (String option : List.of("--info", "--limit", "--deviation-min", "--deviation-max", "--beat-profile", "--peak-beat")) {
            assertTrue(output.toString().contains(option));
        }
        assertTrue(output.toString().contains("default: 20"));
        assertTrue(output.toString().contains("4/4"));
        assertTrue(output.toString().contains("ties count"));
        var extended = new ByteArrayOutputStream();
        var previous = System.out;
        try {
            System.setOut(new PrintStream(extended));
            RootCmd.printExtendedHelp(command);
        } finally {
            System.setOut(previous);
        }
        assertTrue(extended.toString(StandardCharsets.UTF_8).contains("Subcommand 'search rhythms' usage:"));
        assertTrue(extended.toString(StandardCharsets.UTF_8).contains("--limit"));
        assertTrue(extended.toString(StandardCharsets.UTF_8).contains("--beat-profile"));
        assertTrue(extended.toString(StandardCharsets.UTF_8).contains("--peak-beat"));
    }

    private static void assertPlayedOnsets(CapturingRhythmPlaybackPort playback, String expected) {
        assertEquals(expected.length(), playback.pattern.voices().get("kick").length);
        for (int i = 0; i < expected.length(); i++) {
            assertEquals(expected.charAt(i) == 'x', playback.pattern.voices().get("kick")[i]
                    || playback.pattern.voices().get("snare")[i], "Onset " + i);
        }
    }

    private Path writeRhythmFile() throws Exception {
        Path file = tempDir.resolve("beat.rdl");
        Files.writeString(file, RDL, StandardCharsets.UTF_8);
        return file;
    }

    private RootCmd buildRoot(CapturingRhythmPlaybackPort playback, RhythmRepository rhythmRepository) {
        var fakeMidi = new FakeMidiOutputPort();
        var repo = new DummyChordRepository();
        var validate = new ValidatePatternsUseCase();
        var rhythmPlayback = new PlaybackRhythmUseCase(playback);
        var generate = new GenerateChordsUseCase(
                new syrincs.a_domain.chord.NoteCombinator(),
                new syrincs.a_domain.hindemith.ChordAnalysis(),
                3
        );
        var interactor = new UseCaseInteractor(
                new SendToMidiUseCase(fakeMidi),
                validate,
                rhythmPlayback,
                new AnalyseRhythmUseCase(),
                repo,
                generate,
                new AnalyseChordByHindemithUseCase(),
                new GetHindemithChordsFromDbUseCase(repo),
                new PersistHindemithChordUseCase(repo),
                null,
                new PlayHuffmanRhythmsUseCase(rhythmPlayback, validate),
                rhythmRepository
        );
        return new RootCmd(interactor, fakeMidi);
    }

    private static class CapturingRhythmPlaybackPort implements RhythmPlaybackPort {
        int calls;
        Pattern pattern;
        RhythmSpec spec;
        String deviceNameSubstring;

        @Override
        public void play(Pattern pattern, RhythmSpec spec, List<VoiceSpec> voices) {
            calls++;
            this.pattern = pattern;
            this.spec = spec;
        }

        @Override
        public void play(Pattern pattern, RhythmSpec spec, List<VoiceSpec> voices, String deviceNameSubstring) {
            calls++;
            this.pattern = pattern;
            this.spec = spec;
            this.deviceNameSubstring = deviceNameSubstring;
        }
    }

    private static class StubRhythmRepository extends FilteringRhythmRepository {
        StubRhythmRepository() {
            super(List.of(new HuffmanRhythm(4, 4, 120, "xoxo xooo xoxo xooo"),
                    new HuffmanRhythm(4, 4, 120, "xoxo xooo xooo xooo")));
        }
    }

    private static class EmptyRhythmRepository extends FilteringRhythmRepository {
        EmptyRhythmRepository() {
            super(List.of());
        }
    }

    private static class DummyChordRepository implements HindemithChordRepositoryPort {
        @Override public long save(HindemithChord chord) { return 0; }
        @Override public List<Long> saveAll(List<HindemithChord> chords) { return List.of(); }
        @Override public Optional<HindemithChord> findById(long id) { return Optional.empty(); }
        @Override public List<HindemithChord> findAll() { return List.of(); }
        @Override public void deleteById(long id) { }
        @Override public void truncate() { }
        @Override public List<HindemithChord> getAllOf(Integer group) { return List.of(); }
        @Override public List<HindemithChord> getAllOfRootNote(Integer rootNote) { return List.of(); }
        @Override public List<HindemithChord> getAllOfRootNoteAndGroup(Integer rootNote, Integer group) { return List.of(); }
        @Override public List<HindemithChord> getAllOfRootNoteAndMaxGroup(Integer rootNote, Integer maxGroup) { return List.of(); }
        @Override public List<HindemithChord> findByRootNoteAndGroupsAndNumNotes(
                int rootNote,
                Collection<Integer> groups,
                Collection<Integer> numNotes
        ) { return List.of(); }
        @Override public List<HindemithChord> findByRootNoteAndGroupsAndNumNotesAndRange(
                int rootNote,
                Collection<Integer> groups,
                Collection<Integer> numNotes,
                int range
        ) { return List.of(); }
    }
}
