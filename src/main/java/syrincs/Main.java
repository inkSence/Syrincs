// src/main/java/syrincs/Main.java
package syrincs;

import picocli.CommandLine;
import syrincs.b_application.UseCaseInteractor;
import syrincs.c_adapters.midi.JdkMidiOutputAdapter;
import syrincs.b_application.PlaybackRhythmUseCase;
import syrincs.c_adapters.midi.SequenceBuilder;
import syrincs.c_adapters.midi.RhythmPlaybackService;
import syrincs.c_adapters.midi.JdkSequencePlayer;
import syrincs.c_adapters.cli.RootCmd;
import syrincs.c_adapters.postgres.PostgresHindemithChordRepository;

import java.util.ArrayList;
import java.util.List;

public class Main {
    public static void main(String[] args) throws Exception {
        // Bootstrap interactor with MIDI and DB repository
        var midiAdapter = new JdkMidiOutputAdapter();
        var dbCfg = syrincs.d_frameworksAndDrivers.AppConfig.loadDbConfig(args);
        var repo = new PostgresHindemithChordRepository(dbCfg.url, dbCfg.user, dbCfg.password);
        var rhythmPlayback = new PlaybackRhythmUseCase(new RhythmPlaybackService(new SequenceBuilder(), new JdkSequencePlayer()));
        var interactor = new UseCaseInteractor(midiAdapter, repo, rhythmPlayback);

        // Filter out DB-related CLI flags before passing to PicoCli so they don't appear in help
        String[] filtered = filterDbArgs(args);


        // If root-level help requested, print extended help including subcommand usages and exit
        if (isRootHelpRequest(filtered)) {
            // Let PicoCLI handle standard help uniformly
            new CommandLine(new RootCmd(interactor, midiAdapter)).usage(System.out);
            return;
        }

        var cmd = new CommandLine(new RootCmd(interactor, midiAdapter));
        int exitCode = cmd.execute(filtered);
        if (exitCode != 0) {
            System.exit(exitCode);
        }
    }

    private static String[] filterDbArgs(String[] args) {
        if (args == null || args.length == 0) return args;
        List<String> out = new ArrayList<>(args.length);
        for (String a : args) {
            if (a == null) continue;
            String lower = a.toLowerCase();
            boolean startsWith = lower.startsWith("--db-url=") || lower.startsWith("--db-user=") || lower.startsWith("--db-pass=");
            if (!startsWith) out.add(a);
        }
        return out.toArray(String[]::new);
    }

    private static boolean isRootHelpRequest(String[] args) {
        if (args == null) return false;
        if (args.length != 1) return false;
        String a0 = args[0];
        return "-h".equals(a0) || "--help".equals(a0);
    }

}
