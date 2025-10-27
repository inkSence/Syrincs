package syrincs.c_adapters.cli;

import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;
import picocli.CommandLine.ParentCommand;
import syrincs.a_domain.Tone;
import syrincs.b_application.UseCaseInteractor;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.Callable;

import syrincs.c_adapters.RhythmFileParser;

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
                RootCmd.AnalyseCmd.class,
                RootCmd.DeleteCmd.class
        }
)
public class RootCmd implements Runnable {
    final UseCaseInteractor interactor;
    final syrincs.b_application.ports.MidiDeviceQueryPort deviceQuery;

    public RootCmd(UseCaseInteractor interactor, syrincs.b_application.ports.MidiDeviceQueryPort deviceQuery) {
        this.interactor = interactor;
        this.deviceQuery = deviceQuery;
    }

    @Override
    public void run() {
        // Keep help simple; PicoCLI standard help is enough
        new CommandLine(this).usage(System.out);
    }

    @Command(name = "list", description = "List MIDI outputs")
    public static class ListCmd implements Callable<Integer> {
        @ParentCommand RootCmd parent;

        @Override
        public Integer call() {
            var query = parent.deviceQuery;
            for (var ep : query.listOutputs()) {
                System.out.printf("%s | %s | %s%n", ep.name(), ep.in() ? "In" : "-", ep.out() ? "Out" : "-");
            }
            return 0;
        }
    }

    @Command(name = "play", description = "play something", subcommands = { PlayCmd.NoteCmd.class, PlayCmd.PlayChordsCmd.class, PlayCmd.RhythmCmd.class })
    public static class PlayCmd implements Callable<Integer> {
        @ParentCommand RootCmd parent;

//        @Command(name = "note", description = "Play a single note")

        @Override
        public Integer call() {
            new CommandLine(this).usage(System.out);
            return 0;
        }

        @Command(name = "note", description = "Play a single note (defaults: note=60, vel=0.5, ms=100)")
        public static class NoteCmd implements Callable<Integer> {
            @ParentCommand PlayCmd parentPlay;

            @Option(names = "note", description = "MIDI note (0-127)", defaultValue = "60")
            int note;

            @Option(names = "vel", description = "Velocity 0..1", defaultValue = "0.5")
            double vel;

            @Option(names = {"duration", "dur"}, description = "Duration in ms", defaultValue = "100")
            long dur;

            @Override
            public Integer call() {
                try {
                    var interactor = parentPlay.parent.interactor;
                    interactor.sendToneToDevice(new Tone(dur, note, vel), null);
                    return 0;
                } catch (Exception e) {
                    System.err.println("[ERROR] " + e.getMessage());
                    return 1;
                }
            }
        }

        @Command(name = "chords", description = "Play chords from DB. Output device is auto-selected.")
        public static class PlayChordsCmd implements Callable<Integer> {
            @ParentCommand PlayCmd parentPlay; // Access via parentPlay.parent.interactor

            // Accept tokenized options like today: "numnotes 3 4"
            @Option(names = {"numNotes", "notes"}, arity = "1..*", description = "Chord sizes (default: 3 4 5)", split = " ", defaultValue = "3 4 5")
            int[] numNotes;

            @Option(names = {"group", "groups"}, arity = "1..*", description = "Hindemith groups (1..9)", split = " ", defaultValue = "1 2 3 4 5 6 7 8 9")
            int[] groups;

            @Option(names = {"rootNote", "root"}, description = "Root note (default: 60)", defaultValue = "60")
            int rootNote;

            @Option(names = "range", description = "Max chord span (maxNote - minNote), default: 24", defaultValue = "24")
            int range;

            @Option(names = "channel", description = "MIDI channel, default 0", defaultValue = "0")
            Integer channel;

            @Option(names = {"duration", "dur"}, description = "Duration, default 200ms", defaultValue = "200")
            Long dur;

            @Override
            public Integer call() {
                try {
                    var interactor = parentPlay.parent.interactor;
                    List<Integer> nn = Arrays.stream(numNotes).boxed().toList();
                    List<Integer> gr = Arrays.stream(groups).boxed().toList();
                    interactor.playChords(nn, gr, rootNote, range, dur, channel);
                    return 0;
                } catch (Exception e) {
                    System.err.println("[ERROR] " + e.getMessage());
                    return 1;
                }
            }
        }

        @Command(name = "rhythm", description = "Parse RDL-0, validate, build MIDI sequence, and play it on device")
        public static class RhythmCmd implements Callable<Integer> {
            @ParentCommand PlayCmd parentPlay;




            @Option(names = "--in", description = "RDL-0 input file", defaultValue = "data/beat.rdl")
            String inFile;

            @Override public Integer call() {
                try {
                    RhythmFileParser.MidiData res = new RhythmFileParser().parse(inFile);
                    var interactor = parentPlay.parent.interactor;
                    interactor.playRhythm(res.pattern, res.spec, res.voices);
                    return 0;
                } catch (Exception e) {
                    System.err.println("[ERROR] " + e.getMessage());
                    return 1;
                }
            }




        }
    }



    @Command(name = "calculate", aliases = {"calc", "generate"}, description = "generate something",  subcommands = {CalculateCmd.ChordsCmd.class, CalculateCmd.RhythmCmd.class })
    public static class CalculateCmd implements Callable<Integer> {
        @ParentCommand RootCmd parent;

        public Integer call(){
            new CommandLine(this).usage(System.out);
            return 0;
        }

        @Command(name = "chords", aliases = {"chord" }, description = "Generate chords and persist")
        public static class ChordsCmd implements Callable<Integer> {
            @ParentCommand CalculateCmd parent;

            @Parameters(index = "0", description = "minLowerNote")
            int minLowerNote;

            @Parameters(index = "1", description = "maxUpperNote")
            int maxUpperNote;

            @Override
            public Integer call() {
                var interactor = parent.parent.interactor;
                var ids = interactor.calculateAndPersistAllChordsToFiveNotes(minLowerNote, maxUpperNote);
                System.out.printf("[DB] Persisted %d chords for range [%d, %d].%n", ids.size(), minLowerNote, maxUpperNote);
                return 0;
            }
        }

        @Command(name = "rhythm", description = "Generate rhythm")
        public static class RhythmCmd implements Callable<Integer>{
            @ParentCommand CalculateCmd parent;


            @Option(names = "--tempo", description = "tempo BPM", defaultValue = "120")
            int tempo;
            @Option(names = "--ppq", description = "pulses per quarter note", defaultValue = "480")
            int ppq;
            @Option(names = "--res-per-beat", description = "resolution per beat (e.g. 4 => 16ths)", defaultValue = "4")
            int resPerBeat;

            @Option(names = "--sig", description = "time signature, e.g. 4/4", defaultValue = "4/4")
            String sig;

            @Option(names = "--bars", description = "number of bars", defaultValue = "1")
            int bars;



            @Option(names = "channel", description = "MIDI channel for drum-instrument")
            Integer channel;
            @Option(names = "--vel-kick", description = "velocity for kick (0-127)")
            Integer velKick;
            @Option(names = "--vel-snare", description = "velocity for snare (0-127)")
            Integer velSnare;
            @Option(names = "--gate", description = "gate percent (0-100) for both voices", defaultValue = "50")
            int gate;
            int[] parseSig() {
                String[] xy = sig.split("/");
                if (xy.length != 2) throw new IllegalArgumentException("Invalid --sig: '"+sig+"'");
                int n = Integer.parseInt(xy[0].trim());
                int d = Integer.parseInt(xy[1].trim());
                return new int[]{n,d};
            }
            @Override
            public Integer call(){
            System.out.println("No Rhythms generated.");
            return 0;
            }
        }
    }

    @Command(name = "analyse", aliases = {"analyze"}, description = "analyse something", subcommands = { AnalyseCmd.ChordCmd.class, AnalyseCmd.RhythmCmd.class })
    public static class AnalyseCmd implements Callable<Integer> {
        @ParentCommand RootCmd parent;

        @Override
        public Integer call(){
            new CommandLine(this).usage(System.out);
            return 0;
        }

        @Command(name = "chord", description = "Analyse chord by Hindemith")
        public static class ChordCmd implements Callable<Integer> {
            @ParentCommand AnalyseCmd parent;
            @Parameters(arity = "3..*", description = "MIDI notes")
            int[] notes;

            @Override
            public Integer call() {
                var interactor = parent.parent.interactor;
                var list = Arrays.stream(notes).boxed().toList();
                var res = interactor.analyzeChordByHindemith(list);
                System.out.println("[ANALYZE] Notes=" + res.notes + " | Column=" + res.column + " | Root=" + res.rootNote + " | Group=" + res.group + " | Frame=" + res.frameInterval);
                return 0;
            }


        }


        @Command(name="rhythm", description="Hufman Code Based Validation")
        public static class RhythmCmd implements Callable<Integer> {
            @ParentCommand AnalyseCmd parent;

            @Parameters(index="0", description = "Rhythm in simple onset Format")
            String rhythm;



            @Option(names = "--in", description = "Rhythm File", defaultValue = "data/beat.rdl")
            String inFile;



            @Override
            public Integer call() {
                var interactor = parent.parent.interactor;
                interactor.printRhythmFileContent();
                System.out.println(rhythm);

                return 0;
            }
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
