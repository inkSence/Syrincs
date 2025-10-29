package syrincs.a_domain.rhythm;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import picocli.CommandLine;
import syrincs.b_application.UseCaseInteractor;
import syrincs.b_application.ports.HindemithChordRepositoryPort;
import syrincs.b_application.PlaybackRhythmUseCase;
import syrincs.c_adapters.midi.*;
import syrincs.c_adapters.cli.RootCmd;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileWriter;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

public class RhythmE2ETest {

    // Minimal dummy repo to satisfy UseCaseInteractor; rhythm tests don't touch DB
    private static class DummyRepo implements HindemithChordRepositoryPort {
        @Override public long save(syrincs.a_domain.hindemith.HindemithChord chord) { return 0; }
        @Override public List<Long> saveAll(List<syrincs.a_domain.hindemith.HindemithChord> chords) { return List.of(); }
        @Override public Optional<syrincs.a_domain.hindemith.HindemithChord> findById(long id) { return Optional.empty(); }
        @Override public List<syrincs.a_domain.hindemith.HindemithChord> findAll() { return List.of(); }
        @Override public void deleteById(long id) { }
        @Override public void truncate() { }
        @Override public List<syrincs.a_domain.hindemith.HindemithChord> getAllOf(Integer group) { return List.of(); }
        @Override public List<syrincs.a_domain.hindemith.HindemithChord> getAllOfRootNote(Integer rootNote) { return List.of(); }
        @Override public List<syrincs.a_domain.hindemith.HindemithChord> getAllOfRootNoteAndGroup(Integer rootNote, Integer group) { return List.of(); }
        @Override public List<syrincs.a_domain.hindemith.HindemithChord> getAllOfRootNoteAndMaxGroup(Integer rootNote, Integer maxGroup) { return List.of(); }
        @Override public List<syrincs.a_domain.hindemith.HindemithChord> findByRootNoteAndGroupsAndNumNotes(int rootNote, Collection<Integer> groups, Collection<Integer> numNotes) { return List.of(); }
        @Override public List<syrincs.a_domain.hindemith.HindemithChord> findByRootNoteAndGroupsAndNumNotesAndRange(int rootNote, Collection<Integer> groups, Collection<Integer> numNotes, int range) { return List.of(); }
    }

    private RootCmd buildRootWithFake(FakeMidiOutputPort fakeMidi, FakeSequencePlayer seqFake) {
        var send = new syrincs.b_application.SendToMidiUseCase(fakeMidi);
        var validate = new syrincs.b_application.ValidatePatternsUseCase();
        var rhythmUC = new PlaybackRhythmUseCase(new RhythmPlaybackService(new SequenceBuilder(), seqFake));
        var repo = new DummyRepo();
        var generate = new syrincs.b_application.GenerateChordsUseCase(new syrincs.a_domain.chord.NoteCombinator(), new syrincs.a_domain.hindemith.ChordAnalysis(), 3);
        var analyze = new syrincs.b_application.AnalyseChordByHindemithUseCase();
        var get = new syrincs.b_application.GetHindemithChordsFromDbUseCase(repo);
        var persist = new syrincs.b_application.PersistHindemithChordUseCase(repo);
        var interactor = new UseCaseInteractor(send, validate, rhythmUC, new syrincs.b_application.AnalyseRhythmUseCase(), repo, generate, analyze, get, persist);
        return new RootCmd(interactor, fakeMidi);
        }

    private RootCmd buildRootWithRealAdapter() {
        var real = new JdkMidiOutputAdapter();
        var send = new syrincs.b_application.SendToMidiUseCase(real);
        var validate = new syrincs.b_application.ValidatePatternsUseCase();
        var rhythmUC = new PlaybackRhythmUseCase(new RhythmPlaybackService(new SequenceBuilder(), new syrincs.c_adapters.midi.JdkSequencePlayer()));
        var repo = new DummyRepo();
        var generate = new syrincs.b_application.GenerateChordsUseCase(new syrincs.a_domain.chord.NoteCombinator(), new syrincs.a_domain.hindemith.ChordAnalysis(), 3);
        var analyze = new syrincs.b_application.AnalyseChordByHindemithUseCase();
        var get = new syrincs.b_application.GetHindemithChordsFromDbUseCase(repo);
        var persist = new syrincs.b_application.PersistHindemithChordUseCase(repo);
        var interactor = new UseCaseInteractor(send, validate, rhythmUC, new syrincs.b_application.AnalyseRhythmUseCase(), repo, generate, analyze, get, persist);
        return new RootCmd(interactor, real);
    }

    private File writeTemp(String content) throws Exception {
        File f = File.createTempFile("rdl0", ".txt");
        try (var w = new FileWriter(f, StandardCharsets.UTF_8)) {
            w.write(content);
        }
        return f;
    }

    private static final String RDL = "" +
            "time: 4/4\n" +
            "tempo: 120\n" +
            "res-per-beat: 4\n" +
            "bars: 1\n\n" +
            "voice kick  note=36 channel=10 vel=90 gate=50\n" +
            "voice snare note=38 channel=10 vel=90 gate=50\n\n" +
            "pattern kick:  | x - - - | x - - - | x - - - | x - - - |\n" +
            "pattern snare: | - - - - | x - - - | - - - - | x - - - |\n";

    @BeforeAll
    public static void setupMidi(){
        DeviceService.loadStandardMidiDevice();
    }

    @Test
    public void play_happyPath_buildsCorrectEvents() throws Exception {
        File f = writeTemp(RDL);
        FakeMidiOutputPort fakeMidi = new FakeMidiOutputPort();
        FakeSequencePlayer seqFake = new FakeSequencePlayer();

        int code = new CommandLine(buildRootWithFake(fakeMidi, seqFake)).execute("play", "rhythm",
                "--in", f.getAbsolutePath()
        );
        assertEquals(0, code);
        List<FakeSequencePlayer.EventRec> events = seqFake.getEvents();
        long hits = 4+2; // kick 4 + snare 2
        assertEquals(hits*2, events.size());
        long last = -1;
        for (var e : events) { assertTrue(e.tick >= last); last = e.tick; }
        for (var e : events) {
            if (e.on) {
                assertTrue(e.tick % 120 == 0, "On tick multiple of 120");
                if (e.note == 36) {
                    assertEquals(10, e.channel); assertEquals(90, e.velocity);
                } else if (e.note == 38) {
                    assertEquals(10, e.channel); assertEquals(90, e.velocity);
                } else {
                    fail("Unexpected note: "+e.note);
                }
            }
        }
        java.util.Map<String, FakeSequencePlayer.EventRec> lastOn = new java.util.HashMap<>();
        for (var e : events) {
            String key = e.channel+":"+e.note;
            if (e.on) lastOn.put(key, e); else {
                var on = lastOn.get(key);
                if (on != null) {
                    assertEquals(on.tick + 60, e.tick);
                    lastOn.remove(key);
                }
            }
        }
        assertTrue(lastOn.isEmpty(), "All ons should be matched by offs");
        long maxTick = 16*120 + 60;
        assertTrue(events.getLast().tick <= maxTick);
    }

    @Test
    public void error_missingVoice_nonZeroExit_onPlay() throws Exception {
        String bad = "time: 4/4\nres-per-beat: 4\nbars: 1\npattern kick: x x x x x x x x x x x x x x x x\n";
        File f = writeTemp(bad);
        ByteArrayOutputStream berr = new ByteArrayOutputStream();
        PrintStream oldErr = System.err; System.setErr(new PrintStream(berr));
        try {
            int code = new CommandLine(buildRootWithFake(new FakeMidiOutputPort(), new FakeSequencePlayer())).execute("play", "rhythm", "--in", f.getAbsolutePath());
            assertNotEquals(0, code);
            assertTrue(berr.toString().toLowerCase().contains("missing voice"));
        } finally { System.setErr(oldErr); }
    }

    @Test
    public void error_unknownDevice_nonZeroExit() throws Exception {
        File f = writeTemp(RDL);
        ByteArrayOutputStream berr = new ByteArrayOutputStream();
        PrintStream oldErr = System.err; System.setErr(new PrintStream(berr));
        try {
            int code = new CommandLine(buildRootWithRealAdapter()).execute("play", "rhythm",
                    "--in", f.getAbsolutePath(), "--device", "__THIS_DEVICE_WILL_NOT_EXIST__");
            assertNotEquals(0, code);
        } finally { System.setErr(oldErr); }
    }

    @Test
    public void play_withoutDevice_usesDefaultWithFakePort() throws Exception {
        File f = writeTemp(RDL);
        FakeMidiOutputPort fakeMidi = new FakeMidiOutputPort();
        FakeSequencePlayer seqFake = new FakeSequencePlayer();
        int code = new CommandLine(buildRootWithFake(fakeMidi, seqFake)).execute("play", "rhythm",
                "--in", f.getAbsolutePath()
        );
        assertEquals(0, code);
        List<FakeSequencePlayer.EventRec> events = seqFake.getEvents();
        assertFalse(events.isEmpty());
    }

}
