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
        assertTrue(text.contains("No stored Huffman rhythms found for information grades [3]"));
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
