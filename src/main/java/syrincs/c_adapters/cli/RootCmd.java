package syrincs.c_adapters.cli;

import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Mixin;
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
                RootCmd.DevicesCmd.class,
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
        printExtendedHelp(new CommandLine(this));
    }

    public static void printExtendedHelp(CommandLine root) {
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

    private static void printSubcommandUsage(CommandLine parent, String name) {
        CommandLine subcommand = parent.getSubcommands().get(name);
        if (subcommand != null) {
            System.out.println();
            System.out.println("Subcommand 'play " + name + "' usage:");
            subcommand.usage(System.out);
        }
    }

    @Command(name = "devices", description = "List MIDI outputs")
    public static class DevicesCmd implements Callable<Integer> {
        @ParentCommand RootCmd parent;

        @Override
        public Integer call() {
            return listMidiOutputs(parent);
        }
    }

    @Command(name = "list", hidden = true, description = "Legacy alias for devices")
    public static class ListCmd implements Callable<Integer> {
        @ParentCommand RootCmd parent;

        @Override
        public Integer call() {
            return listMidiOutputs(parent);
        }
    }

    private static Integer listMidiOutputs(RootCmd parent) {
        if (parent.deviceQuery == null) {
            System.err.println("[MIDI] Device query is not configured.");
            return 1;
        }
        for (var ep : parent.deviceQuery.listOutputs()) {
            System.out.printf("[MIDI] %s | %s | %s%n", ep.name(), ep.description(), ep.vendor());
        }
        return 0;
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

    @Command(name = "start", description = "Check database and start SuperCollider. PostgreSQL is expected as a system service")
    public static class StartCmd implements Callable<Integer> {
        @ParentCommand RootCmd parent;

        @Parameters(index = "0", arity = "0..1", description = "Runtime target: all, db (check only), sc", defaultValue = "all")
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

            @Option(names = "--device", description = "MIDI output device name substring. Default: SYRINCS_MIDI_DEVICE, Roland Digital Piano/DP603, then first MIDI out")
            String device;

            @Override
            public Integer call() {
                try {
                    RhythmFileParser.MidiData res = new RhythmFileParser().parse(inFile);
                    parentPlay.parent.midiInteractor.playRhythm(res.pattern, res.spec, res.voices, device);
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

                @Option(names = "--device", description = "MIDI output device name substring. Overrides parent --device when set after db")
                String device;

                @Override
                public Integer call() {
                    try {
                        List<Integer> infos = Arrays.stream(infoGrades).boxed().toList();
                        parent.parentPlay.parent.midiInteractor.playRhythmsByInformationGrades(infos, effectiveDevice());
                        return 0;
                    } catch (Exception e) {
                        System.err.println("[ERROR] " + e.getMessage());
                        return 1;
                    }
                }

                private String effectiveDevice() {
                    return device != null && !device.isBlank() ? device : parent.device;
                }
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
                        SuperColliderCmd.FxCmd.class,
                        SuperColliderCmd.SetCmd.class,
                        SuperColliderCmd.RampCmd.class,
                        SuperColliderCmd.SceneCmd.class,
                        SuperColliderCmd.RoleCmd.class,
                        SuperColliderCmd.SceneDemoCmd.class,
                        SuperColliderCmd.DemoCmd.class
                }
        )
        public static class SuperColliderCmd implements Callable<Integer> {
            @Mixin
            OscOptions osc = new OscOptions();

            @Parameters(arity = "0..*", description = "MIDI notes. Default: 60 64 67")
            int[] notes;

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
                var adapter = osc.adapter(preset, pan);

                for (int note : notesToSend) {
                    adapter.sendNote(preset, note, velocity, durationSeconds, pan);
                }

                System.out.printf(
                        "[OSC] Sent /note to %s:%d preset=%s notes=%s velocity=%.3f duration=%.3fs pan=%.3f%n",
                        osc.host,
                        osc.port,
                        preset,
                        Arrays.toString(notesToSend),
                        velocity,
                        durationSeconds,
                        pan
                );
                return 0;
            }

            static class OscOptions {
                @Option(names = "--host", description = "OSC host", defaultValue = SuperColliderOscOutputAdapter.DEFAULT_HOST)
                String host;

                @Option(names = "--port", description = "OSC UDP port", defaultValue = "57120")
                int port;

                SuperColliderOscOutputAdapter adapter() {
                    return adapter(SuperColliderOscOutputAdapter.DEFAULT_PRESET, SuperColliderOscOutputAdapter.DEFAULT_PAN);
                }

                SuperColliderOscOutputAdapter adapter(String preset, double pan) {
                    return new SuperColliderOscOutputAdapter(host, port, preset, pan);
                }
            }

            @Command(name = "chord", mixinStandardHelpOptions = true, description = "Send one OSC /chord message to SuperCollider")
            public static class ChordCmd implements Callable<Integer> {
                @Mixin
                OscOptions osc = new OscOptions();

                @Parameters(arity = "1..*", description = "MIDI notes")
                int[] notes;

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
                    var adapter = osc.adapter(preset, pan);
                    adapter.sendChord(preset, notes, velocity, durationSeconds, pan);
                    System.out.printf(
                            "[OSC] Sent /chord to %s:%d preset=%s notes=%s velocity=%.3f duration=%.3fs pan=%.3f%n",
                            osc.host,
                            osc.port,
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
                @Mixin
                OscOptions osc = new OscOptions();

                @Parameters(index = "0", description = "Drum preset, e.g. drum.kick")
                String preset;

                @Option(names = {"--velocity", "--vel"}, description = "Velocity 0..1", defaultValue = "0.8")
                double velocity;

                @Option(names = "--pan", description = "Pan -1..1", defaultValue = "0.0")
                double pan;

                @Override
                public Integer call() throws Exception {
                    var adapter = osc.adapter(SuperColliderOscOutputAdapter.DEFAULT_PRESET, pan);
                    adapter.sendDrum(preset, velocity, pan);
                    System.out.printf(
                            "[OSC] Sent /drum to %s:%d preset=%s velocity=%.3f pan=%.3f%n",
                            osc.host,
                            osc.port,
                            preset,
                            velocity,
                            pan
                    );
                    return 0;
                }
            }

            @Command(name = "fx", mixinStandardHelpOptions = true, description = "Send one OSC /fx message to SuperCollider")
            public static class FxCmd implements Callable<Integer> {
                @Mixin
                OscOptions osc = new OscOptions();

                @Parameters(index = "0", description = "Effect name: reverb, delay, chorus or master")
                String effectName;

                @Parameters(index = "1", description = "Parameter name, e.g. mix, room, time, feedback, depth, volume")
                String paramName;

                @Parameters(index = "2", description = "Parameter value")
                double value;

                @Option(names = "--off", description = "Send enabled=0 and disable/reset the selected effect")
                boolean off;

                @Override
                public Integer call() throws Exception {
                    var adapter = osc.adapter();
                    boolean enabled = !off;
                    adapter.sendFx(effectName, enabled, paramName, value);
                    System.out.printf(
                            "[OSC] Sent /fx to %s:%d effect=%s enabled=%d param=%s value=%.3f%n",
                            osc.host,
                            osc.port,
                            effectName,
                            enabled ? 1 : 0,
                            paramName,
                            value
                    );
                    return 0;
                }
            }

            @Command(name = "set", mixinStandardHelpOptions = true, description = "Send one OSC /set automation message to SuperCollider")
            public static class SetCmd implements Callable<Integer> {
                @Mixin
                OscOptions osc = new OscOptions();

                @Parameters(index = "0", description = "Target: master, reverb, delay, chorus, preset:<name> or family:<name>")
                String target;

                @Parameters(index = "1", description = "Parameter name, e.g. volume, mix, feedback, cutoff, reverbSend")
                String paramName;

                @Parameters(index = "2", description = "Parameter value")
                double value;

                @Override
                public Integer call() throws Exception {
                    var adapter = osc.adapter();
                    adapter.sendSet(target, paramName, value);
                    System.out.printf(
                            "[OSC] Sent /set to %s:%d target=%s param=%s value=%.3f%n",
                            osc.host,
                            osc.port,
                            target,
                            paramName,
                            value
                    );
                    return 0;
                }
            }

            @Command(name = "ramp", mixinStandardHelpOptions = true, description = "Send one OSC /ramp automation message to SuperCollider")
            public static class RampCmd implements Callable<Integer> {
                @Mixin
                OscOptions osc = new OscOptions();

                @Parameters(index = "0", description = "Target: master, reverb, delay, chorus, preset:<name> or family:<name>")
                String target;

                @Parameters(index = "1", description = "Parameter name, e.g. volume, mix, feedback, cutoff, reverbSend")
                String paramName;

                @Parameters(index = "2", description = "Target parameter value")
                double value;

                @Parameters(index = "3", description = "Ramp duration in seconds")
                double seconds;

                @Override
                public Integer call() throws Exception {
                    var adapter = osc.adapter();
                    adapter.sendRamp(target, paramName, value, seconds);
                    System.out.printf(
                            "[OSC] Sent /ramp to %s:%d target=%s param=%s value=%.3f seconds=%.3f%n",
                            osc.host,
                            osc.port,
                            target,
                            paramName,
                            value,
                            seconds
                    );
                    return 0;
                }
            }

            @Command(name = "scene", mixinStandardHelpOptions = true, description = "Send one OSC /scene message to SuperCollider")
            public static class SceneCmd implements Callable<Integer> {
                @Mixin
                OscOptions osc = new OscOptions();

                @Parameters(index = "0", description = "Scene name, e.g. scene.chorale")
                String sceneName;

                @Override
                public Integer call() throws Exception {
                    var adapter = osc.adapter();
                    adapter.sendScene(sceneName);
                    System.out.printf(
                            "[OSC] Sent /scene to %s:%d scene=%s%n",
                            osc.host,
                            osc.port,
                            sceneName
                    );
                    return 0;
                }
            }

            @Command(name = "role", mixinStandardHelpOptions = true, description = "Send one OSC /role session override to SuperCollider")
            public static class RoleCmd implements Callable<Integer> {
                @Mixin
                OscOptions osc = new OscOptions();

                @Parameters(index = "0", description = "Role name, e.g. harmony or role:harmony")
                String roleName;

                @Parameters(index = "1", description = "Preset name, e.g. pad.warm")
                String presetName;

                @Override
                public Integer call() throws Exception {
                    var adapter = osc.adapter();
                    adapter.sendRole(roleName, presetName);
                    System.out.printf(
                            "[OSC] Sent /role to %s:%d role=%s preset=%s%n",
                            osc.host,
                            osc.port,
                            roleName,
                            presetName
                    );
                    return 0;
                }
            }

            @Command(name = "scene-demo", mixinStandardHelpOptions = true, description = "Send a short role/scene SuperCollider smoke-test sequence")
            public static class SceneDemoCmd implements Callable<Integer> {
                @Mixin
                OscOptions osc = new OscOptions();

                @Override
                public Integer call() throws Exception {
                    var adapter = osc.adapter();
                    int[] chord = {48, 55, 60, 64};
                    int[] melody = {72, 74, 76};
                    int[] counter = {67, 65, 64};

                    adapter.sendScene("scene.chorale");
                    Thread.sleep(180);
                    adapter.sendChord("role:harmony", chord, 0.58, 1.4, 0.0);
                    Thread.sleep(520);
                    for (int i = 0; i < melody.length; i++) {
                        adapter.sendNote("role:melody", melody[i], 0.55, 0.45, 0.12);
                        adapter.sendNote("role:counter", counter[i], 0.42, 0.40, -0.12);
                        Thread.sleep(320);
                    }

                    Thread.sleep(420);
                    adapter.sendScene("scene.electronic");
                    Thread.sleep(180);
                    adapter.sendChord("role:harmony", chord, 0.58, 1.2, 0.0);
                    Thread.sleep(420);
                    adapter.sendNote("role:bass", 36, 0.68, 0.45, -0.10);
                    for (int i = 0; i < melody.length; i++) {
                        adapter.sendNote("role:melody", melody[i], 0.58, 0.32, 0.12);
                        adapter.sendNote("role:counter", counter[i] + 12, 0.46, 0.28, -0.12);
                        if (i == 0) {
                            adapter.sendDrum("role:drums", 0.88, 0.0);
                        }
                        if (i == 1) {
                            adapter.sendDrum("drum.snare", 0.62, 0.04);
                        }
                        if (i == 2) {
                            adapter.sendDrum("drum.hat.open", 0.36, -0.18);
                        }
                        Thread.sleep(300);
                    }

                    adapter.sendScene("scene.hindemith_lab");
                    System.out.printf("[OSC] Sent SuperCollider scene demo to %s:%d%n", osc.host, osc.port);
                    return 0;
                }
            }

            @Command(name = "demo", mixinStandardHelpOptions = true, description = "Send a short SuperCollider preset smoke-test sequence")
            public static class DemoCmd implements Callable<Integer> {
                @Mixin
                OscOptions osc = new OscOptions();

                @Override
                public Integer call() throws Exception {
                    var adapter = osc.adapter();

                    int[] chord = {48, 55, 60, 64};

                    adapter.sendFx("reverb", false, "mix", 0.0);
                    adapter.sendFx("delay", false, "mix", 0.0);
                    adapter.sendFx("chorus", false, "mix", 0.0);
                    adapter.sendFx("master", true, "volume", 0.82);

                    adapter.sendChord("organ.full", chord, 0.48, 1.0, 0.0);
                    Thread.sleep(900);

                    adapter.sendFx("reverb", true, "mix", 0.24);
                    adapter.sendFx("reverb", true, "room", 0.62);
                    adapter.sendChord("organ.full", chord, 0.48, 1.2, 0.0);
                    Thread.sleep(980);

                    adapter.sendFx("chorus", true, "mix", 0.18);
                    adapter.sendFx("chorus", true, "depth", 0.014);
                    adapter.sendSet("preset:pad.warm", "cutoff", 850);
                    adapter.sendRamp("preset:pad.warm", "cutoff", 5_000, 3.0);
                    adapter.sendRamp("reverb", "mix", 0.28, 2.0);
                    for (int i = 0; i < 3; i++) {
                        adapter.sendChord("pad.warm", chord, 0.46, 1.15, 0.0);
                        Thread.sleep(880);
                    }

                    adapter.sendChord("strings.pad", chord, 0.43, 1.35, 0.0);
                    Thread.sleep(920);

                    adapter.sendFx("delay", true, "mix", 0.22);
                    adapter.sendFx("delay", true, "time", 0.33);
                    adapter.sendFx("delay", true, "feedback", 0.32);

                    int[] pluckFigure = {72, 76, 79, 84, 79, 76, 72};
                    for (int note : pluckFigure) {
                        adapter.sendNote("pluck.harplike", note, 0.65, 0.35, 0.12);
                        Thread.sleep(170);
                    }

                    int[] keysFigure = {60, 64, 67, 72};
                    for (int note : keysFigure) {
                        adapter.sendNote("keys.fm_epiano", note, 0.58, 0.45, -0.08);
                        Thread.sleep(210);
                    }
                    for (int note : new int[]{79, 76, 72}) {
                        adapter.sendNote("keys.bell", note, 0.52, 0.70, 0.14);
                        Thread.sleep(260);
                    }

                    int[] bassLine = {36, 36, 43, 41, 36, 43, 48, 43};
                    for (int i = 0; i < bassLine.length; i++) {
                        adapter.sendNote(i < 4 ? "bass.round" : "bass.sub", bassLine[i], 0.68, 0.22, -0.10);
                        Thread.sleep(180);
                    }

                    adapter.sendFx("reverb", true, "mix", 0.18);
                    for (int step = 0; step < 16; step++) {
                        if (step == 0 || step == 8) {
                            adapter.sendDrum("drum.kick", 0.95, 0.0);
                        }
                        if (step == 6 || step == 14) {
                            adapter.sendDrum("drum.kick.short", 0.48, 0.0);
                        }
                        if (step == 4 || step == 12) {
                            adapter.sendDrum("drum.snare", 0.75, 0.02);
                        }
                        if (step == 12) {
                            adapter.sendDrum("drum.clap", 0.58, 0.08);
                        }
                        if (step == 3 || step == 11) {
                            adapter.sendDrum("drum.rim", 0.36, 0.10);
                        }
                        if (step == 7 || step == 15) {
                            adapter.sendDrum("drum.hat.open", 0.38, -0.18);
                        }
                        if (step == 13) {
                            adapter.sendDrum("drum.tom.low", 0.64, -0.14);
                        }
                        if (step == 14) {
                            adapter.sendDrum("drum.tom.mid", 0.58, 0.02);
                        }
                        if (step == 15) {
                            adapter.sendDrum("drum.tom.high", 0.54, 0.16);
                        }
                        if (step % 2 == 0) {
                            adapter.sendDrum("drum.hat.closed", step % 4 == 0 ? 0.44 : 0.32, -0.18);
                        }
                        Thread.sleep(150);
                    }

                    adapter.sendFx("delay", false, "mix", 0.0);
                    adapter.sendFx("chorus", false, "mix", 0.0);
                    adapter.sendFx("reverb", true, "mix", 0.16);
                    adapter.sendFx("master", true, "volume", 0.82);
                    adapter.sendSet("preset:pad.warm", "cutoff", 2_100);

                    System.out.printf("[OSC] Sent SuperCollider preset, FX and automation demo to %s:%d%n", osc.host, osc.port);
                    return 0;
                }
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
                try {
                    var ids = parent.parent.interactor.generateAllRhythmsOfFourQuarters();
                    System.out.printf("[DB] Persisted %d Huffman rhythms for 4/4 sixteenth grid.%n", ids.size());
                    return 0;
                } catch (Exception e) {
                    return reportError(e);
                }
            }
        }

        private static Integer persistChords(UseCaseInteractor interactor, int minLowerNote, int maxUpperNote) {
            try {
                var ids = interactor.calculateAndPersistAllChordsToFiveNotes(minLowerNote, maxUpperNote);
                System.out.printf("[DB] Persisted %d chords for range [%d, %d].%n", ids.size(), minLowerNote, maxUpperNote);
                return 0;
            } catch (Exception e) {
                return reportError(e);
            }
        }

        private static Integer reportError(Exception e) {
            System.err.println("[ERROR] " + e.getMessage());
            return 1;
        }
    }

    @Command(name = "analyze", description = "Analyze chord by Hindemith or rhythm by Huffman complexity", subcommands = {
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
                var result = parent.parent.interactor.analyzeHuffmanRhythm(rhythm);
                System.out.printf(
                        "[ANALYZE] Rhythm=%s | Info=%d | Deviation=%.6f | Beats=%s%n",
                        result.getOnsetList(),
                        result.getInformation(),
                        result.getStandardDeviation(),
                        result.getOnsetListPerBeat()
                );
                return 0;
            }
        }

        private static void analyzeChord(UseCaseInteractor interactor, int[] notes) {
            var list = Arrays.stream(notes).boxed().toList();
            var res = interactor.analyzeChordByHindemith(list);
            System.out.println("[ANALYZE] Notes=" + res.notes + " | Column=" + res.column + " | Root=" + res.rootNote + " | Group=" + res.group + " | Frame=" + res.frameInterval);
        }
    }

    @Command(name = "delete", hidden = true, description = "Truncate Hindemith chords table")
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
