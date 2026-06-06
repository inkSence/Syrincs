package syrincs.c_adapters.cli;

import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;
import picocli.CommandLine.ParentCommand;
import syrincs.a_domain.Tone;
import syrincs.a_domain.hindemith.HindemithChord;
import syrincs.b_application.UseCaseInteractor;
import syrincs.c_adapters.osc.SuperColliderOscOutputAdapter;
import syrincs.c_adapters.runtime.LocalRuntime;

import javax.sound.midi.InvalidMidiDataException;
import javax.sound.midi.MidiUnavailableException;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.Callable;

/**
 * PicoCli-based command tree for Syrincs. This coexists with the legacy CliController for now.
 */
@Command(
        name = "syrincs",
        mixinStandardHelpOptions = true,
        version = "syrincs 1.0",
        description = "MIDI Utilities and Hindemith chords",
        subcommands = {
                RootCmd.ListCmd.class,
                RootCmd.StartCmd.class,
                RootCmd.StatusCmd.class,
                RootCmd.PlayCmd.class,
                RootCmd.CalculateCmd.class,
                RootCmd.AnalyzeCmd.class,
                RootCmd.DeleteCmd.class
        }
)
public class RootCmd implements Runnable {
    final UseCaseInteractor interactor;
    final UseCaseInteractor midiInteractor;
    final LocalRuntime runtime;

    public RootCmd(UseCaseInteractor interactor) {
        this(interactor, interactor, null);
    }

    public RootCmd(UseCaseInteractor interactor, UseCaseInteractor midiInteractor, LocalRuntime runtime) {
        this.interactor = interactor;
        this.midiInteractor = midiInteractor;
        this.runtime = runtime;
    }

    @Override
    public void run() {
        // Show root usage and also explicitly show help for 'play' and its common subcommands
        CommandLine root = new CommandLine(this);
        root.usage(System.out);
        CommandLine play = root.getSubcommands().get("play");
        if (play != null) {
            System.out.println();
            System.out.println("Subcommand 'play' usage:");
            play.usage(System.out);
            CommandLine note = play.getSubcommands().get("note");
            if (note != null) {
                System.out.println();
                System.out.println("Subcommand 'play note' usage:");
                note.usage(System.out);
            }
            CommandLine chords = play.getSubcommands().get("chords");
            if (chords != null) {
                System.out.println();
                System.out.println("Subcommand 'play chords' usage:");
                chords.usage(System.out);
            }
            CommandLine sc = play.getSubcommands().get("sc");
            if (sc != null) {
                System.out.println();
                System.out.println("Subcommand 'play sc' usage:");
                sc.usage(System.out);
            }
        }
    }

    @Command(name = "list", description = "List MIDI outputs")
    public static class ListCmd implements Callable<Integer> {
        @ParentCommand RootCmd parent;

        @Override
        public Integer call() {
            var interactor = parent.midiInteractor;
            for (var i : interactor.listMidiOutputs()) {
                System.out.printf("[MIDI] %s | %s | %s%n", i.getName(), i.getDescription(), i.getVendor());
            }
            return 0;
        }
    }

    @Command(name = "start", description = "Start local runtime dependencies. Default: db then SuperCollider in foreground")
    public static class StartCmd implements Callable<Integer> {
        @ParentCommand RootCmd parent;

        @Parameters(index = "0", arity = "0..1", description = "Runtime target: all, db, sc", defaultValue = "all")
        String target;

        @Override
        public Integer call() throws Exception {
            if (parent.runtime == null) {
                System.err.println("[START] Runtime management is not configured.");
                return 1;
            }
            return parent.runtime.start(target, System.out, System.err);
        }
    }

    @Command(name = "status", description = "Check local database and SuperCollider consumer status")
    public static class StatusCmd implements Callable<Integer> {
        @ParentCommand RootCmd parent;

        @Override
        public Integer call() {
            if (parent.runtime == null) {
                System.err.println("[STATUS] Runtime management is not configured.");
                return 1;
            }
            return parent.runtime.printStatus(System.out);
        }
    }

    @Command(name = "play", description = "Play through the default SuperCollider output or an explicit MIDI output", subcommands = { PlayCmd.NoteCmd.class, PlayChordsCmd.class, PlayCmd.SuperColliderCmd.class })
    public static class PlayCmd implements Callable<Integer> {
        @ParentCommand RootCmd parent;

        @Override
        public Integer call() throws MidiUnavailableException, InvalidMidiDataException, InterruptedException {
            parent.interactor.sendToneToDevice(new Tone(500L, 60, 0.7), null);
            return 0;
        }

        @Command(name = "note", description = "Play a single note through SuperCollider by default")
        public static class NoteCmd implements Callable<Integer> {
            @ParentCommand PlayCmd parentPlay;

            @Option(names = "note", description = "MIDI note (0-127)", defaultValue = "60")
            int note;

            @Option(names = "vel", description = "Velocity 0..1", defaultValue = "0.5")
            double vel;

            @Option(names = {"dur", "--duration", "--dur"}, description = "Duration in milliseconds", defaultValue = "500")
            long durationMs;

            @Option(names = "--output", description = "Output target: sc or midi", defaultValue = "sc")
            String output;

            @Override
            public Integer call() throws MidiUnavailableException, InvalidMidiDataException, InterruptedException {
                var interactor = parentPlay.parent.interactorFor(output);
                interactor.sendToneToDevice(new Tone(durationMs, note, vel), null);
                return 0;
            }
        }

        @Command(name = "sc", aliases = {"supercollider"}, description = "Send notes as OSC /note messages to SuperCollider")
        public static class SuperColliderCmd implements Callable<Integer> {
            @Parameters(arity = "0..*", description = "MIDI notes. Default: 60 64 67")
            int[] notes;

            @Option(names = "--host", description = "OSC host", defaultValue = SuperColliderOscOutputAdapter.DEFAULT_HOST)
            String host;

            @Option(names = "--port", description = "OSC UDP port", defaultValue = "57120")
            int port;

            @Option(names = {"--synth", "--preset"}, description = "Synth or preset name", defaultValue = SuperColliderOscOutputAdapter.DEFAULT_SYNTH)
            String synth;

            @Option(names = {"--velocity", "--vel"}, description = "Velocity 0..1", defaultValue = "0.7")
            double velocity;

            @Option(names = {"--duration", "--dur"}, description = "Duration in seconds", defaultValue = "0.5")
            double durationSeconds;

            @Option(names = "--pan", description = "Pan -1..1", defaultValue = "0.0")
            double pan;

            @Override
            public Integer call() throws Exception {
                int[] notesToSend = (notes == null || notes.length == 0) ? new int[]{60, 64, 67} : notes;
                var adapter = new SuperColliderOscOutputAdapter(host, port, synth, pan);

                for (int note : notesToSend) {
                    adapter.sendNote(synth, note, velocity, durationSeconds, pan);
                }

                System.out.printf(
                        "[OSC] Sent /note to %s:%d synth=%s notes=%s velocity=%.3f duration=%.3fs pan=%.3f%n",
                        host,
                        port,
                        synth,
                        Arrays.toString(notesToSend),
                        velocity,
                        durationSeconds,
                        pan
                );
                return 0;
            }
        }
    }

    @Command(name = "chords", description = "Play chords from DB through SuperCollider by default")
    public static class PlayChordsCmd implements Callable<Integer> {
        @ParentCommand PlayCmd parentPlay; // Access via parentPlay.parent.interactor

        // Accept tokenized options like today: "numnotes 3 4"
        @Option(names = {"numnotes", "num", "notes"}, arity = "1..*", description = "Chord sizes (e.g. 3 4 5)", split = " ")
        int[] numNotes;

        @Option(names = {"group", "groups"}, arity = "1..*", description = "Hindemith groups (1..9)", split = " ")
        int[] groups;

        @Option(names = {"rootnote", "root"}, description = "Root note (default: 60)", defaultValue = "60")
        int rootNote;

        @Option(names = "range", description = "Max chord span (maxNote - minNote), default: 24", defaultValue = "24")
        int range;

        @Option(names = {"duration", "dur", "--duration", "--dur"}, description = "Duration in milliseconds", defaultValue = "200")
        long durationMs;

        @Option(names = "--output", description = "Output target: sc or midi", defaultValue = "sc")
        String output;

        @Override
        public Integer call() throws Exception {
            var interactor = parentPlay.parent.interactorFor(output);
            List<Integer> nn = (numNotes == null || numNotes.length == 0) ? List.of(3,4,5) : Arrays.stream(numNotes).boxed().toList();
            List<Integer> gr = (groups   == null || groups.length   == 0) ? List.of(1,2,3,4,5,6,7,8,9) : Arrays.stream(groups).boxed().toList();
            interactor.playChords(nn, gr, rootNote, range, durationMs, null);
            return 0;
        }
    }

    @Command(name = "calculate", aliases = {"calc"}, description = "Generate chords and persist")
    public static class CalculateCmd implements Callable<Integer> {
        @ParentCommand RootCmd parent;

        @Parameters(index = "0", description = "minLowerNote")
        int minLowerNote;

        @Parameters(index = "1", description = "maxUpperNote")
        int maxUpperNote;

        @Override
        public Integer call() {
            var interactor = parent.interactor;
            var ids = interactor.calculateAndPersistAllChordsToFiveNotes(minLowerNote, maxUpperNote);
            System.out.printf("[DB] Persisted %d chords for range [%d, %d].%n", ids.size(), minLowerNote, maxUpperNote);
            return 0;
        }
    }

    @Command(name = "analyze", aliases = {"analyse"}, description = "Analyze chord by Hindemith")
    public static class AnalyzeCmd implements Callable<Integer> {
        @ParentCommand RootCmd parent;

        @Parameters(arity = "3..*", description = "MIDI notes")
        int[] notes;

        @Override
        public Integer call() {
            var interactor = parent.interactor;
            var list = Arrays.stream(notes).boxed().toList();
            var res = interactor.analyzeChordByHindemith(list);
            System.out.println("[ANALYZE] Notes=" + res.notes + " | Column=" + res.column + " | Root=" + res.rootNote + " | Group=" + res.group + " | Frame=" + res.frameInterval);
            return 0;
        }
    }

    @Command(name = "delete", description = "Truncate Hindemith chords table")
    public static class DeleteCmd implements Callable<Integer> {
        @ParentCommand RootCmd parent;
        @Override public Integer call() {
            parent.interactor.deleteHindemithChords();
            return 0;
        }
    }

    private UseCaseInteractor interactorFor(String output) {
        OutputTarget target = OutputTarget.parse(output);
        return target == OutputTarget.MIDI ? midiInteractor : interactor;
    }

    private enum OutputTarget {
        SC,
        MIDI;

        static OutputTarget parse(String value) {
            if (value == null || value.isBlank()) {
                return SC;
            }
            String normalized = value.toLowerCase(Locale.ROOT);
            return switch (normalized) {
                case "sc", "supercollider", "osc" -> SC;
                case "midi", "jdk-midi", "jdk" -> MIDI;
                default -> throw new IllegalArgumentException("Unknown output target: " + value);
            };
        }
    }
}
