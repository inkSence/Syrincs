package syrincs.c_adapters.cli;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
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
import syrincs.b_application.GetHindemithChordsFromDbUseCase;
import syrincs.b_application.PersistHindemithChordUseCase;
import syrincs.b_application.PlayHuffmanRhythmsUseCase;
import syrincs.b_application.PlaybackRhythmUseCase;
import syrincs.b_application.SendToMidiUseCase;
import syrincs.b_application.UseCaseInteractor;
import syrincs.b_application.ValidatePatternsUseCase;
import syrincs.b_application.ports.HindemithChordRepositoryPort;
import syrincs.b_application.ports.RhythmPlaybackPort;
import syrincs.b_application.ports.RhythmRepository;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

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
    void playRhythmDb_acceptsDeviceAfterDbSubcommand() {
        CapturingRhythmPlaybackPort playback = new CapturingRhythmPlaybackPort();
        RootCmd root = buildRoot(playback, new StubRhythmRepository());

        int code = new CommandLine(root).execute("play", "rhythm", "db", "--device", "Virtual Out", "1");

        assertEquals(0, code);
        assertEquals(1, playback.calls);
        assertEquals("Virtual Out", playback.deviceNameSubstring);
    }

    @Test
    void playRhythmDb_reportsMissingCandidates() {
        CapturingRhythmPlaybackPort playback = new CapturingRhythmPlaybackPort();
        RootCmd root = buildRoot(playback, new EmptyRhythmRepository());
        ByteArrayOutputStream error = new ByteArrayOutputStream();
        PrintStream originalErr = System.err;
        try {
            System.setErr(new PrintStream(error));

            int code = new CommandLine(root).execute("play", "rhythm", "db", "3");

            assertEquals(1, code);
            assertEquals(0, playback.calls);
        } finally {
            System.setErr(originalErr);
        }

        String text = error.toString(StandardCharsets.UTF_8);
        assertTrue(text.contains("No Huffman rhythms found for information grades [3]"));
        assertTrue(text.contains("syrincs calculate rhythms"));
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
        assertTrue(text.contains("[ANALYZE] Rhythm=xoooxoxoxoooxoxo"));
        assertTrue(text.contains("Info=3"));
        assertTrue(text.contains("Deviation="));
        assertTrue(text.contains("Beats=[xooo, xoxo, xooo, xoxo]"));
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
        String deviceNameSubstring;

        @Override
        public void play(Pattern pattern, RhythmSpec spec, List<VoiceSpec> voices) {
            calls++;
        }

        @Override
        public void play(Pattern pattern, RhythmSpec spec, List<VoiceSpec> voices, String deviceNameSubstring) {
            calls++;
            this.deviceNameSubstring = deviceNameSubstring;
        }
    }

    private static class StubRhythmRepository implements RhythmRepository {
        @Override public List<Long> saveAll(List<HuffmanRhythm> rhythms) { return List.of(); }
        @Override public List<HuffmanRhythm> getTwoRhythms(Integer id1, Integer id2) { return List.of(); }
        @Override public List<HuffmanRhythm> getAllByInformation(Integer information) { return List.of(); }
        @Override public List<HuffmanRhythm> getAllByInformationAndMinDeviation(Integer information, Double minDeviation) {
            return List.of(new HuffmanRhythm(4, 4, 120, "xooo xooo xooo xooo"));
        }
    }

    private static class EmptyRhythmRepository implements RhythmRepository {
        @Override public List<Long> saveAll(List<HuffmanRhythm> rhythms) { return List.of(); }
        @Override public List<HuffmanRhythm> getTwoRhythms(Integer id1, Integer id2) { return List.of(); }
        @Override public List<HuffmanRhythm> getAllByInformation(Integer information) { return List.of(); }
        @Override public List<HuffmanRhythm> getAllByInformationAndMinDeviation(Integer information, Double minDeviation) {
            return List.of();
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
