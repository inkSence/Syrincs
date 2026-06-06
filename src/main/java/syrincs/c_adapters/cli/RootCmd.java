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

import javax.sound.midi.InvalidMidiDataException;
import javax.sound.midi.MidiUnavailableException;
import java.util.Arrays;
import java.util.List;
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
                RootCmd.PlayCmd.class,
                RootCmd.CalculateCmd.class,
                RootCmd.AnalyzeCmd.class,
                RootCmd.DeleteCmd.class
        }
)
public class RootCmd implements Runnable {
    final UseCaseInteractor interactor;

    public RootCmd(UseCaseInteractor interactor) {
        this.interactor = interactor;
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
            var interactor = parent.interactor;
            for (var i : interactor.listMidiOutputs()) {
                System.out.printf("[MIDI] %s | %s | %s%n", i.getName(), i.getDescription(), i.getVendor());
            }
            return 0;
        }
    }

    @Command(name = "play", description = "Play a single note (default) or use subcommands 'note', 'chords' and 'sc'", subcommands = { PlayCmd.NoteCmd.class, PlayChordsCmd.class, PlayCmd.SuperColliderCmd.class })
    public static class PlayCmd implements Callable<Integer> {
        @ParentCommand RootCmd parent;

        @Override
        public Integer call() throws MidiUnavailableException, InvalidMidiDataException, InterruptedException {
            // Default behavior for 'syrincs play' -> same as 'syrincs play note' with defaults
            var interactor = parent.interactor;
            interactor.sendToneToDevice(new Tone(100L, 60, 0.5), null);
            return 0;
        }

        @Command(name = "note", description = "Play a single note (defaults: note=60, vel=0.5, ms=100)")
        public static class NoteCmd implements Callable<Integer> {
            @ParentCommand PlayCmd parentPlay;

            @Option(names = "note", description = "MIDI note (0-127)", defaultValue = "60")
            int note;

            @Option(names = "vel", description = "Velocity 0..1", defaultValue = "0.5")
            double vel;

            @Override
            public Integer call() throws MidiUnavailableException, InvalidMidiDataException, InterruptedException {
                var interactor = parentPlay.parent.interactor;
                interactor.sendToneToDevice(new Tone(100L, note, vel), null);
                return 0;
            }
        }

        @Command(
                name = "sc",
                aliases = {"supercollider"},
                mixinStandardHelpOptions = true,
                description = "Send preset-based OSC messages to SuperCollider",
                subcommands = {
                        SuperColliderCmd.ChordCmd.class,
                        SuperColliderCmd.DrumCmd.class,
                        SuperColliderCmd.DemoCmd.class
                }
        )
        public static class SuperColliderCmd implements Callable<Integer> {
            @Parameters(arity = "0..*", description = "MIDI notes. Default: 60 64 67")
            int[] notes;

            @Option(names = "--host", description = "OSC host", defaultValue = SuperColliderOscOutputAdapter.DEFAULT_HOST)
            String host;

            @Option(names = "--port", description = "OSC UDP port", defaultValue = "57120")
            int port;

            @Option(names = {"--preset", "--synth"}, description = "SuperCollider preset name", defaultValue = SuperColliderOscOutputAdapter.DEFAULT_PRESET)
            String preset;

            @Option(names = {"--velocity", "--vel"}, description = "Velocity 0..1", defaultValue = "0.7")
            double velocity;

            @Option(names = {"--duration", "--dur"}, description = "Duration in seconds", defaultValue = "0.5")
            double durationSeconds;

            @Option(names = "--pan", description = "Pan -1..1", defaultValue = "0.0")
            double pan;

            @Override
            public Integer call() throws Exception {
                int[] notesToSend = (notes == null || notes.length == 0) ? new int[]{60, 64, 67} : notes;
                var adapter = new SuperColliderOscOutputAdapter(host, port, preset, pan);

                for (int note : notesToSend) {
                    adapter.sendNote(preset, note, velocity, durationSeconds, pan);
                }

                System.out.printf(
                        "[OSC] Sent /note to %s:%d preset=%s notes=%s velocity=%.3f duration=%.3fs pan=%.3f%n",
                        host,
                        port,
                        preset,
                        Arrays.toString(notesToSend),
                        velocity,
                        durationSeconds,
                        pan
                );
                return 0;
            }

            @Command(name = "chord", mixinStandardHelpOptions = true, description = "Send one OSC /chord message to SuperCollider")
            public static class ChordCmd implements Callable<Integer> {
                @Parameters(arity = "1..*", description = "MIDI notes")
                int[] notes;

                @Option(names = "--host", description = "OSC host", defaultValue = SuperColliderOscOutputAdapter.DEFAULT_HOST)
                String host;

                @Option(names = "--port", description = "OSC UDP port", defaultValue = "57120")
                int port;

                @Option(names = {"--preset", "--synth"}, description = "SuperCollider preset name", defaultValue = "organ.full")
                String preset;

                @Option(names = {"--velocity", "--vel"}, description = "Velocity 0..1", defaultValue = "0.7")
                double velocity;

                @Option(names = {"--duration", "--dur"}, description = "Duration in seconds", defaultValue = "1.0")
                double durationSeconds;

                @Option(names = "--pan", description = "Pan -1..1", defaultValue = "0.0")
                double pan;

                @Override
                public Integer call() throws Exception {
                    var adapter = new SuperColliderOscOutputAdapter(host, port, preset, pan);
                    adapter.sendChord(preset, notes, velocity, durationSeconds, pan);
                    System.out.printf(
                            "[OSC] Sent /chord to %s:%d preset=%s notes=%s velocity=%.3f duration=%.3fs pan=%.3f%n",
                            host,
                            port,
                            preset,
                            Arrays.toString(notes),
                            velocity,
                            durationSeconds,
                            pan
                    );
                    return 0;
                }
            }

            @Command(name = "drum", mixinStandardHelpOptions = true, description = "Send one OSC /drum message to SuperCollider")
            public static class DrumCmd implements Callable<Integer> {
                @Parameters(index = "0", description = "Drum preset, e.g. drum.kick")
                String preset;

                @Option(names = "--host", description = "OSC host", defaultValue = SuperColliderOscOutputAdapter.DEFAULT_HOST)
                String host;

                @Option(names = "--port", description = "OSC UDP port", defaultValue = "57120")
                int port;

                @Option(names = {"--velocity", "--vel"}, description = "Velocity 0..1", defaultValue = "0.8")
                double velocity;

                @Option(names = "--pan", description = "Pan -1..1", defaultValue = "0.0")
                double pan;

                @Override
                public Integer call() throws Exception {
                    var adapter = new SuperColliderOscOutputAdapter(host, port, SuperColliderOscOutputAdapter.DEFAULT_PRESET, pan);
                    adapter.sendDrum(preset, velocity, pan);
                    System.out.printf(
                            "[OSC] Sent /drum to %s:%d preset=%s velocity=%.3f pan=%.3f%n",
                            host,
                            port,
                            preset,
                            velocity,
                            pan
                    );
                    return 0;
                }
            }

            @Command(name = "demo", mixinStandardHelpOptions = true, description = "Send a short SuperCollider preset smoke-test sequence")
            public static class DemoCmd implements Callable<Integer> {
                @Option(names = "--host", description = "OSC host", defaultValue = SuperColliderOscOutputAdapter.DEFAULT_HOST)
                String host;

                @Option(names = "--port", description = "OSC UDP port", defaultValue = "57120")
                int port;

                @Override
                public Integer call() throws Exception {
                    var adapter = new SuperColliderOscOutputAdapter(host, port, SuperColliderOscOutputAdapter.DEFAULT_PRESET, 0.0);

                    adapter.sendNote("test.sine", 60, 0.75, 0.45, -0.15);
                    Thread.sleep(500);

                    adapter.sendChord("organ.full", new int[]{60, 64, 67}, 0.55, 1.0, 0.0);
                    Thread.sleep(900);

                    adapter.sendChord("pad.warm", new int[]{57, 60, 64, 69}, 0.45, 1.8, 0.0);
                    Thread.sleep(1_200);

                    int[] pluckFigure = {72, 76, 79, 84, 79, 76};
                    for (int note : pluckFigure) {
                        adapter.sendNote("pluck.harplike", note, 0.65, 0.35, 0.12);
                        Thread.sleep(170);
                    }

                    for (int step = 0; step < 8; step++) {
                        if (step == 0 || step == 4) {
                            adapter.sendDrum("drum.kick", 0.95, 0.0);
                        }
                        if (step == 2 || step == 6) {
                            adapter.sendDrum("drum.snare", 0.75, 0.02);
                        }
                        adapter.sendDrum("drum.hat.closed", step % 2 == 0 ? 0.45 : 0.28, -0.18);
                        Thread.sleep(150);
                    }

                    System.out.printf("[OSC] Sent SuperCollider preset demo to %s:%d%n", host, port);
                    return 0;
                }
            }
        }
    }

    @Command(name = "chords", description = "Play chords from DB. Options: numnotes|num|notes (multi), group|groups (multi), rootnote|root (default=60), range (default=24). Duration is fixed to 200 ms; output device is auto-selected.")
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

        @Override
        public Integer call() throws Exception {
            var interactor = parentPlay.parent.interactor;
            List<Integer> nn = (numNotes == null || numNotes.length == 0) ? List.of(3,4,5) : Arrays.stream(numNotes).boxed().toList();
            List<Integer> gr = (groups   == null || groups.length   == 0) ? List.of(1,2,3,4,5,6,7,8,9) : Arrays.stream(groups).boxed().toList();
            interactor.playChords(nn, gr, rootNote, range, 200L, null);
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
}
