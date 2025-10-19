package syrincs.c_adapters.cli;

import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;
import picocli.CommandLine.ParentCommand;
import syrincs.a_domain.Tone;
import syrincs.b_application.UseCaseInteractor;

import java.io.FileReader;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;

import syrincs.a_domain.rhythm.PatternHeader;
import syrincs.a_domain.rhythm.RhythmSpec;
import syrincs.a_domain.rhythm.VoiceSpec;
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
                RootCmd.AnalyzeCmd.class,
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

    @Command(name = "play", description = "Play a single note (default) or use subcommands 'note', 'chords' and 'rhythm'", subcommands = { PlayCmd.NoteCmd.class, PlayChordsCmd.class, PlayCmd.RhythmCmd.class })
    public static class PlayCmd implements Callable<Integer> {
        @ParentCommand RootCmd parent;

        @Override
        public Integer call() {
            try {
                // Default behavior for 'syrincs play' -> same as 'syrincs play note' with defaults
                var interactor = parent.interactor;
                interactor.sendToneToDevice(new Tone(100L, 60, 0.5), null);
                return 0;
            } catch (Exception e) {
                System.err.println("[ERROR] " + e.getMessage());
                return 1;
            }
        }

        @Command(name = "note", description = "Play a single note (defaults: note=60, vel=0.5, ms=100)")
        public static class NoteCmd implements Callable<Integer> {
            @ParentCommand PlayCmd parentPlay;

            @Option(names = "note", description = "MIDI note (0-127)", defaultValue = "60")
            int note;

            @Option(names = "vel", description = "Velocity 0..1", defaultValue = "0.5")
            double vel;

            @Override
            public Integer call() {
                try {
                    var interactor = parentPlay.parent.interactor;
                    interactor.sendToneToDevice(new Tone(100L, note, vel), null);
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

            @Option(names = "--sig", description = "time signature, e.g. 4/4", defaultValue = "4/4")
            String sig;
            @Option(names = "--tempo", description = "tempo BPM", defaultValue = "120")
            int tempo;
            @Option(names = "--ppq", description = "pulses per quarter note", defaultValue = "480")
            int ppq;
            @Option(names = "--res-per-beat", description = "resolution per beat (e.g. 4 => 16ths)", defaultValue = "4")
            int resPerBeat;
            @Option(names = "--bars", description = "number of bars", defaultValue = "1")
            int bars;
            @Option(names = "--in", description = "RDL-0 input file. If omitted: use default (syrincs.rhythm.file | SYRINCS_RHYTHM_FILE | ~/.syrincs/config.properties), else read from STDIN if piped; otherwise try data/beat.rdl.")
            String inFile;

            // Voice overrides
            @Option(names = "--channel-kick", description = "MIDI channel for kick (1-16)")
            Integer channelKick;
            @Option(names = "--note-kick", description = "MIDI note for kick")
            Integer noteKick;
            @Option(names = "--vel-kick", description = "velocity for kick (0-127)")
            Integer velKick;
            @Option(names = "--channel-snare", description = "MIDI channel for snare (1-16)")
            Integer channelSnare;
            @Option(names = "--note-snare", description = "MIDI note for snare")
            Integer noteSnare;
            @Option(names = "--vel-snare", description = "velocity for snare (0-127)")
            Integer velSnare;

            @Option(names = "--gate", description = "gate percent (0-100) for both voices", defaultValue = "50")
            int gate;

            @Option(names = "--device", description = "MIDI device name substring (optional; falls back to default from env/config)")
            String device;

            Reader openReader() throws Exception {
                // 1) Explicit file flag
                if (inFile != null && !inFile.isBlank()) return new FileReader(inFile);

                // 2) Configured default file (system property, env, or user config)
                String def = syrincs.d_frameworksAndDrivers.AppConfig.loadDefaultRhythmFilePath();
                if (def != null && !def.isBlank()) {
                    java.nio.file.Path p = java.nio.file.Path.of(def);
                    if (java.nio.file.Files.isRegularFile(p)) return new FileReader(p.toFile());
                    throw new IllegalArgumentException("Configured default rhythm file not found: " + def);
                }

                // 3) If input is piped (non-interactive), read from STDIN
                if (System.console() == null) {
                    return new InputStreamReader(System.in);
                }

                // 4) Fallback to repository default file if present
                java.nio.file.Path repoDefault = java.nio.file.Path.of("data", "beat.rdl");
                if (java.nio.file.Files.isRegularFile(repoDefault)) {
                    return new FileReader(repoDefault.toFile());
                }

                // 5) No input source available -> instruct user
                throw new IllegalArgumentException("No RDL-0 input provided. Use --in <file>, pipe via STDIN, or set default via SYRINCS_RHYTHM_FILE / -Dsyrincs.rhythm.file or ~/.syrincs/config.properties (rhythm.file).");
            }

            int[] parseSig() {
                String[] xy = sig.split("/");
                if (xy.length != 2) throw new IllegalArgumentException("Invalid --sig: '"+sig+"'");
                int n = Integer.parseInt(xy[0].trim());
                int d = Integer.parseInt(xy[1].trim());
                return new int[]{n,d};
            }

            @Override public Integer call() {
                try (Reader r = openReader()) {
                    var parser = new RhythmFileParser();
                    var res = parser.parse(r);

                    PatternHeader h = res.header;
                    int[] nm = parseSig();
                    int timeNum = h.timeNum() != null ? h.timeNum() : nm[0];
                    int timeDen = h.timeDen() != null ? h.timeDen() : nm[1];
                    int tempoBpm = h.tempo() != null ? h.tempo() : this.tempo;
                    int ppqV = h.ppq() != null ? h.ppq() : this.ppq;
                    int rpb = h.resPerBeat() != null ? h.resPerBeat() : this.resPerBeat;
                    int barsV = h.bars() != null ? h.bars() : this.bars;
                    RhythmSpec spec = new RhythmSpec(timeNum, timeDen, tempoBpm, ppqV, rpb, barsV);

                    int g = Math.max(0, Math.min(100, gate));
                    VoiceSpec kick = new VoiceSpec("kick", 10, 36, 90, g);
                    VoiceSpec snare = new VoiceSpec("snare", 10, 38, 90, g);
                    Map<String, RhythmFileParser.VoiceDecl> pvoices = res.voices;
                    RhythmFileParser.VoiceDecl kd = pvoices.get("kick");
                    RhythmFileParser.VoiceDecl sd = pvoices.get("snare");
                    if (kd != null) kick = new VoiceSpec("kick", kd.channel, kd.note, kd.vel, g);
                    if (sd != null) snare = new VoiceSpec("snare", sd.channel, sd.note, sd.vel, g);
                    int chKick = syrincs.c_adapters.cli.ChannelMapper.toZeroBased(channelKick, kick.channel);
                    int chSnare = syrincs.c_adapters.cli.ChannelMapper.toZeroBased(channelSnare, snare.channel);
                    kick = new VoiceSpec("kick", chKick, kick.note, kick.velocity, g);
                    snare = new VoiceSpec("snare", chSnare, snare.note, snare.velocity, g);
                    // apply note/vel overrides if present
                    if (noteKick != null || velKick != null) {
                        kick = new VoiceSpec("kick", kick.channel, noteKick != null ? noteKick : kick.note, velKick != null ? velKick : kick.velocity, g);
                    }
                    if (noteSnare != null || velSnare != null) {
                        snare = new VoiceSpec("snare", snare.channel, noteSnare != null ? noteSnare : snare.note, velSnare != null ? velSnare : snare.velocity, g);
                    }

                    var voices = new ArrayList<VoiceSpec>();
                    voices.add(kick); voices.add(snare);

                    var interactor = parentPlay.parent.interactor;
                    interactor.playRhythm(res.pattern, spec, voices, device);
                    return 0;
                } catch (Exception e) {
                    System.err.println("[ERROR] " + e.getMessage());
                    return 1;
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

        @Option(names = "--channel", description = "MIDI channel (1-16)")
        Integer channel;

        @Override
        public Integer call() {
            try {
                var interactor = parentPlay.parent.interactor;
                List<Integer> nn = (numNotes == null || numNotes.length == 0) ? List.of(3,4,5) : Arrays.stream(numNotes).boxed().toList();
                List<Integer> gr = (groups   == null || groups.length   == 0) ? List.of(1,2,3,4,5,6,7,8,9) : Arrays.stream(groups).boxed().toList();
                Integer chZero = (channel == null ? null : syrincs.c_adapters.cli.ChannelMapper.toZeroBased(channel, 0));
                interactor.playChords(nn, gr, rootNote, range, 200L, null, chZero);
                return 0;
            } catch (Exception e) {
                System.err.println("[ERROR] " + e.getMessage());
                return 1;
            }
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
