package syrincs.c_adapters.cli;

import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;
import picocli.CommandLine.ParentCommand;
import syrincs.a_domain.Tone;
import syrincs.b_application.UseCaseInteractor;
import syrincs.b_application.ports.MidiDeviceQueryPort;
import syrincs.c_adapters.RhythmFileParser;
import syrincs.c_adapters.osc.SuperColliderOscOutputAdapter;
import syrincs.c_adapters.runtime.LocalRuntime;

import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.Callable;

@Command(
        name = "syrincs",
        mixinStandardHelpOptions = true,
        version = "syrincs 1.0",
        description = "MIDI Utilities and Hindemith chords",
        subcommands = {
                RootCmd.ListCmd.class,
                RootCmd.InitCmd.class,
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
    final MidiDeviceQueryPort deviceQuery;
    final LocalRuntime runtime;

    public RootCmd(UseCaseInteractor interactor, MidiDeviceQueryPort deviceQuery) {
        this(interactor, interactor, deviceQuery, null);
    }

    public RootCmd(UseCaseInteractor interactor, UseCaseInteractor midiInteractor, LocalRuntime runtime) {
        this(interactor, midiInteractor, null, runtime);
    }

    public RootCmd(UseCaseInteractor interactor,
                   UseCaseInteractor midiInteractor,
                   MidiDeviceQueryPort deviceQuery,
                   LocalRuntime runtime) {
        this.interactor = interactor;
        this.midiInteractor = midiInteractor;
        this.deviceQuery = deviceQuery;
        this.runtime = runtime;
    }

    @Override
    public void run() {
        CommandLine root = new CommandLine(this);
        root.usage(System.out);
        CommandLine play = root.getSubcommands().get("play");
        if (play != null) {
            System.out.println();
            System.out.println("Subcommand 'play' usage:");
            play.usage(System.out);
            printSubcommandUsage(play, "note");
            printSubcommandUsage(play, "chords");
            printSubcommandUsage(play, "rhythm");
            printSubcommandUsage(play, "sc");
        }
    }

    private void printSubcommandUsage(CommandLine parent, String name) {
        CommandLine subcommand = parent.getSubcommands().get(name);
        if (subcommand != null) {
            System.out.println();
            System.out.println("Subcommand 'play " + name + "' usage:");
            subcommand.usage(System.out);
        }
    }

    @Command(name = "list", description = "List MIDI outputs")
    public static class ListCmd implements Callable<Integer> {
        @ParentCommand RootCmd parent;

        @Override
        public Integer call() {
            if (parent.deviceQuery == null) {
                System.err.println("[MIDI] Device query is not configured.");
                return 1;
            }
            for (var ep : parent.deviceQuery.listOutputs()) {
                System.out.printf("[MIDI] %s | %s | %s%n", ep.name(), ep.description(), ep.vendor());
            }
            return 0;
        }
    }

    @Command(name = "init", description = "Initialize the configured PostgreSQL database schema")
    public static class InitCmd implements Callable<Integer> {
        @ParentCommand RootCmd parent;

        @Override
        public Integer call() {
            if (parent.runtime == null) {
                System.err.println("[INIT] Runtime management is not configured.");
                return 1;
            }
            return parent.runtime.initializeDatabaseSchema(System.out, System.err);
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

    @Command(name = "play", description = "Play through the default SuperCollider output, explicit MIDI, or rhythm playback", subcommands = {
            PlayCmd.NoteCmd.class,
            PlayCmd.ChordsCmd.class,
            PlayCmd.RhythmCmd.class,
            PlayCmd.SuperColliderCmd.class
    })
    public static class PlayCmd implements Callable<Integer> {
        @ParentCommand RootCmd parent;

        @Override
        public Integer call() {
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

            @Option(names = {"duration", "dur", "--duration", "--dur"}, description = "Duration in milliseconds", defaultValue = "500")
            long durationMs;

            @Option(names = "--output", description = "Output target: sc or midi", defaultValue = "sc")
            String output;

            @Override
            public Integer call() {
                var interactor = parentPlay.parent.interactorFor(output);
                interactor.sendToneToDevice(new Tone(durationMs, note, vel), null);
                return 0;
            }
        }

        @Command(name = "chords", description = "Play chords from DB through SuperCollider by default")
        public static class ChordsCmd implements Callable<Integer> {
            @ParentCommand PlayCmd parentPlay;

            @Option(names = {"numnotes", "numNotes", "num", "notes"}, arity = "1..*", description = "Chord sizes (e.g. 3 4 5)", split = " ")
            int[] numNotes;

            @Option(names = {"group", "groups"}, arity = "1..*", description = "Hindemith groups (1..9)", split = " ")
            int[] groups;

            @Option(names = {"rootnote", "rootNote", "root"}, description = "Root note (default: 60)", defaultValue = "60")
            int rootNote;

            @Option(names = "range", description = "Max chord span (maxNote - minNote), default: 24", defaultValue = "24")
            int range;

            @Option(names = "channel", description = "MIDI channel, default 0", defaultValue = "0")
            Integer channel;

            @Option(names = {"duration", "dur", "--duration", "--dur"}, description = "Duration in milliseconds", defaultValue = "200")
            long durationMs;

            @Option(names = "--output", description = "Output target: sc or midi", defaultValue = "sc")
            String output;

            @Override
            public Integer call() {
                OutputTarget target = OutputTarget.parse(output);
                var interactor = parentPlay.parent.interactorFor(target);
                List<Integer> nn = (numNotes == null || numNotes.length == 0)
                        ? List.of(3, 4, 5)
                        : Arrays.stream(numNotes).boxed().toList();
                List<Integer> gr = (groups == null || groups.length == 0)
                        ? List.of(1, 2, 3, 4, 5, 6, 7, 8, 9)
                        : Arrays.stream(groups).boxed().toList();
                Integer channelForOutput = target == OutputTarget.MIDI ? channel : null;
                interactor.playChords(nn, gr, rootNote, range, durationMs, channelForOutput);
                return 0;
            }
        }

        @Command(name = "rhythm", description = "Parse RDL-0, validate, build MIDI sequence, and play it on the configured MIDI device", subcommands = {RhythmCmd.DbCmd.class})
        public static class RhythmCmd implements Callable<Integer> {
            @ParentCommand PlayCmd parentPlay;

            @Option(names = "--in", description = "RDL-0 input file", defaultValue = "data/beat.rdl")
            String inFile;

            @Override
            public Integer call() {
                try {
                    RhythmFileParser.MidiData res = new RhythmFileParser().parse(inFile);
                    parentPlay.parent.midiInteractor.playRhythm(res.pattern, res.spec, res.voices);
                    return 0;
                } catch (Exception e) {
                    System.err.println("[ERROR] " + e.getMessage());
                    return 1;
                }
            }

            @Command(name = "db", description = "Play rhythms from database by information grades (one random per grade)")
            public static class DbCmd implements Callable<Integer> {
                @ParentCommand RhythmCmd parent;

                @Parameters(arity = "1..*", description = "Information grades to play (e.g. 1 2 3 4)")
                int[] infoGrades;

                @Override
                public Integer call() {
                    try {
                        List<Integer> infos = Arrays.stream(infoGrades).boxed().toList();
                        parent.parentPlay.parent.midiInteractor.playRhythmsByInformationGrades(infos);
                        return 0;
                    } catch (Exception e) {
                        System.err.println("[ERROR] " + e.getMessage());
                        return 1;
                    }
                }
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

    @Command(name = "calculate", aliases = {"calc", "generate"}, description = "Generate chords or rhythms", subcommands = {
            CalculateCmd.ChordsCmd.class,
            CalculateCmd.RhythmsCmd.class
    })
    public static class CalculateCmd implements Callable<Integer> {
        @ParentCommand RootCmd parent;

        @Parameters(arity = "0..2", description = "Optional chord range: minLowerNote maxUpperNote")
        int[] chordRange;

        @Override
        public Integer call() {
            if (chordRange != null && chordRange.length == 2) {
                return persistChords(parent.interactor, chordRange[0], chordRange[1]);
            }
            new CommandLine(this).usage(System.out);
            return 0;
        }

        @Command(name = "chords", aliases = {"chord"}, description = "Generate chords and persist")
        public static class ChordsCmd implements Callable<Integer> {
            @ParentCommand CalculateCmd parent;

            @Parameters(index = "0", description = "minLowerNote")
            int minLowerNote;

            @Parameters(index = "1", description = "maxUpperNote")
            int maxUpperNote;

            @Override
            public Integer call() {
                return persistChords(parent.parent.interactor, minLowerNote, maxUpperNote);
            }
        }

        @Command(name = "rhythms", aliases = {"rhythm"}, description = "Generate all 4/4 Huffman rhythms and persist them")
        public static class RhythmsCmd implements Callable<Integer> {
            @ParentCommand CalculateCmd parent;

            @Override
            public Integer call() {
                parent.parent.interactor.generateAllRhythmsOfFourQuarters();
                System.out.println("All rhythms of four quarters generated.");
                return 0;
            }
        }

        private static Integer persistChords(UseCaseInteractor interactor, int minLowerNote, int maxUpperNote) {
            var ids = interactor.calculateAndPersistAllChordsToFiveNotes(minLowerNote, maxUpperNote);
            System.out.printf("[DB] Persisted %d chords for range [%d, %d].%n", ids.size(), minLowerNote, maxUpperNote);
            return 0;
        }
    }

    @Command(name = "analyze", aliases = {"analyse"}, description = "Analyze chord by Hindemith or rhythm by Huffman complexity", subcommands = {
            AnalyzeCmd.ChordCmd.class,
            AnalyzeCmd.RhythmCmd.class
    })
    public static class AnalyzeCmd implements Callable<Integer> {
        @ParentCommand RootCmd parent;

        @Parameters(arity = "0..*", description = "MIDI notes for direct chord analysis")
        int[] notes;

        @Override
        public Integer call() {
            if (notes != null && notes.length >= 3) {
                analyzeChord(parent.interactor, notes);
                return 0;
            }
            new CommandLine(this).usage(System.out);
            return 0;
        }

        @Command(name = "chord", description = "Analyze chord by Hindemith")
        public static class ChordCmd implements Callable<Integer> {
            @ParentCommand AnalyzeCmd parent;

            @Parameters(arity = "3..*", description = "MIDI notes")
            int[] notes;

            @Override
            public Integer call() {
                analyzeChord(parent.parent.interactor, notes);
                return 0;
            }
        }

        @Command(name = "rhythm", description = "Analyze simple onset rhythm by Huffman complexity")
        public static class RhythmCmd implements Callable<Integer> {
            @ParentCommand AnalyzeCmd parent;

            @Parameters(index = "0", description = "Rhythm in simple onset format")
            String rhythm;

            @Override
            public Integer call() {
                System.out.println("Information: " + parent.parent.interactor.analyzeRhythm(rhythm));
                return 0;
            }
        }

        private static void analyzeChord(UseCaseInteractor interactor, int[] notes) {
            var list = Arrays.stream(notes).boxed().toList();
            var res = interactor.analyzeChordByHindemith(list);
            System.out.println("[ANALYZE] Notes=" + res.notes + " | Column=" + res.column + " | Root=" + res.rootNote + " | Group=" + res.group + " | Frame=" + res.frameInterval);
        }
    }

    @Command(name = "delete", description = "Truncate Hindemith chords table")
    public static class DeleteCmd implements Callable<Integer> {
        @ParentCommand RootCmd parent;

        @Override
        public Integer call() {
            parent.interactor.deleteHindemithChords();
            return 0;
        }
    }

    private UseCaseInteractor interactorFor(String output) {
        return interactorFor(OutputTarget.parse(output));
    }

    private UseCaseInteractor interactorFor(OutputTarget target) {
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
