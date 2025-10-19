package syrincs.c_adapters.cli;

import org.junit.jupiter.api.Test;
import picocli.CommandLine;
import syrincs.b_application.PlaybackRhythmUseCase;
import syrincs.b_application.UseCaseInteractor;
import syrincs.b_application.ports.HindemithChordRepositoryPort;
import syrincs.b_application.ports.MidiDeviceQueryPort;
import syrincs.b_application.ports.MidiOutputPort;
import syrincs.rhythm.FakeMidiOutputPort;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests originally written for the legacy CliController were reworked to exercise the PicoCLI RootCmd.
 * We assert that `syrincs play chords` parses suboptions the same way (defaults and partial overrides)
 * by verifying that UseCaseInteractor.playChords(...) is invoked with the expected arguments.
 */
public class RootCmdChordsCliTest {

    /** Minimal fake repo: not used by the capturing interactor override but required for construction. */
    static class DummyRepo implements HindemithChordRepositoryPort {
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

    /**
     * Capturing interactor that overrides playChords(range overload) to record parsed values.
     */
    static class CapturingInteractor extends UseCaseInteractor {
        List<Integer> numNotes; List<Integer> groups; Integer root; Integer range; Long duration; String device; Integer channel; int calls;
        public CapturingInteractor(MidiOutputPort midi, HindemithChordRepositoryPort repo) {
            super(
                new syrincs.b_application.SendToMidiUseCase(midi),
                new syrincs.b_application.ValidatePatternsUseCase(),
                new PlaybackRhythmUseCase((p,s,v,d)->{}),
                repo,
                new syrincs.b_application.GenerateChordsUseCase(new syrincs.a_domain.chord.NoteCombinator(), new syrincs.a_domain.hindemith.ChordAnalysis(), 3),
                new syrincs.b_application.AnalyseChordByHindemithUseCase(),
                new syrincs.b_application.GetHindemithChordsFromDbUseCase(repo),
                new syrincs.b_application.PersistHindemithChordUseCase(repo)
            );
        }
        @Override
        public void playChords(List<Integer> numNotes, List<Integer> groups, Integer rootNote, Integer range,
                               Long durationMs, String deviceNameSubstring, Integer channelZeroBased) {
            this.numNotes = List.copyOf(numNotes);
            this.groups = List.copyOf(groups);
            this.root = rootNote;
            this.range = range;
            this.duration = durationMs;
            this.device = deviceNameSubstring;
            this.channel = channelZeroBased;
            this.calls++;
        }
    }

    private RootCmd buildRoot(CapturingInteractor interactor, MidiDeviceQueryPort query) {
        return new RootCmd(interactor, query);
    }

    @Test
    void playChords_defaults_are_applied_when_no_suboptions_given() {
        // Given
        var midiFake = new FakeMidiOutputPort();
        var interactor = new CapturingInteractor(midiFake, new DummyRepo());
        var root = buildRoot(interactor, midiFake);

        // When
        int code = new CommandLine(root).execute("play", "chords");
        assertEquals(0, code);
        assertEquals(1, interactor.calls);

        // Then: defaults
        assertEquals(List.of(3,4,5), interactor.numNotes, "default numNotes should be [3,4,5]");
        assertEquals(List.of(1,2,3,4,5,6,7,8,9), interactor.groups, "default groups should be [1..9]");
        assertEquals(60, interactor.root);
        assertEquals(24, interactor.range);
        assertEquals(200L, interactor.duration);
        assertNull(interactor.device);
    }

    @Test
    void playChords_allows_partial_suboptions_and_keeps_defaults_for_missing_ones_case1() {
        // Given: only numNotes and group provided
        var midiFake = new FakeMidiOutputPort();
        var interactor = new CapturingInteractor(midiFake, new DummyRepo());
        var root = buildRoot(interactor, midiFake);

        // When
        int code = new CommandLine(root).execute("play", "chords", "numnotes", "3", "4", "group", "4", "5", "6");
        assertEquals(0, code);

        // Then
        assertEquals(List.of(3,4), interactor.numNotes);
        assertEquals(List.of(4,5,6), interactor.groups);
        assertEquals(60, interactor.root, "rootNote should default to 60");
        assertEquals(24, interactor.range, "range should default to 24");
    }

    @Test
    void playChords_allows_partial_suboptions_and_keeps_defaults_for_missing_ones_case2() {
        // Given: group and rootNote provided, numNotes missing
        var midiFake = new FakeMidiOutputPort();
        var interactor = new CapturingInteractor(midiFake, new DummyRepo());
        var root = buildRoot(interactor, midiFake);

        // When
        int code = new CommandLine(root).execute("play", "chords", "group", "1", "2", "rootnote", "72");
        assertEquals(0, code);

        // Then
        assertEquals(List.of(3,4,5), interactor.numNotes);
        assertEquals(List.of(1,2), interactor.groups);
        assertEquals(72, interactor.root);
        assertEquals(24, interactor.range);
    }

    @Test
    void playChords_channel_option_maps_1_to_16_to_zero_based() {
        var midiFake = new FakeMidiOutputPort();
        var interactor = new CapturingInteractor(midiFake, new DummyRepo());
        var root = buildRoot(interactor, midiFake);

        int code = new CommandLine(root).execute("play", "chords", "--channel", "10");
        assertEquals(0, code);
        assertEquals(1, interactor.calls);
        assertNotNull(interactor.channel);
        assertEquals(9, interactor.channel); // 10 (user) -> 9 (zero-based)
    }
}
