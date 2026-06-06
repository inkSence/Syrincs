// src/main/java/syrincs/Main.java
package syrincs;

import picocli.CommandLine;
import syrincs.b_application.*;
import syrincs.b_application.ports.MidiOutputPort;
import syrincs.c_adapters.midi.*;
import syrincs.c_adapters.cli.RootCmd;
import syrincs.c_adapters.osc.SuperColliderOscOutputAdapter;
import syrincs.c_adapters.postgres.PostgresHindemithChordRepository;
import syrincs.c_adapters.postgres.PostgresRhythmRepository;
import syrincs.c_adapters.runtime.LocalRuntime;

import java.util.ArrayList;
import java.util.List;

public class Main {
    public static void main(String[] args) throws Exception {
        var midiAdapter = new JdkMidiOutputAdapter();
        var scAdapter = new SuperColliderOscOutputAdapter();
        var dbCfg = syrincs.d_frameworksAndDrivers.AppConfig.loadDbConfig(args);
        var repo = new PostgresHindemithChordRepository(dbCfg.url, dbCfg.user, dbCfg.password);
        var rhythmRepo = new PostgresRhythmRepository(dbCfg.url, dbCfg.user, dbCfg.password);
        var rhythmPlayback = new PlaybackRhythmUseCase(new RhythmPlaybackService(new SequenceBuilder(), new JdkSequencePlayer()));
        var runtime = new LocalRuntime(LocalRuntime.resolveProjectRoot(), dbCfg);

        var defaultInteractor = buildInteractor(scAdapter, repo, rhythmRepo, rhythmPlayback);
        var midiInteractor = buildInteractor(midiAdapter, repo, rhythmRepo, rhythmPlayback);

        // Filter out DB-related CLI flags before passing to PicoCli so they don't appear in help
        String[] filtered = filterDbArgs(args);

        // If root-level help requested, print extended help including subcommand usages and exit
        if (isRootHelpRequest(filtered)) {
            var root = new CommandLine(new RootCmd(defaultInteractor, midiInteractor, midiAdapter, runtime));
            RootCmd.printExtendedHelp(root);
            return;
        }

        var cmd = new CommandLine(new RootCmd(defaultInteractor, midiInteractor, midiAdapter, runtime));
        int exitCode = cmd.execute(filtered);
        if (exitCode != 0) {
            System.exit(exitCode);
        }
    }

    private static UseCaseInteractor buildInteractor(MidiOutputPort output,
                                                     PostgresHindemithChordRepository repo,
                                                     PostgresRhythmRepository rhythmRepo,
                                                     PlaybackRhythmUseCase rhythmPlayback) {
        var send = new SendToMidiUseCase(output);
        var validate = new ValidatePatternsUseCase();
        var generate = new GenerateChordsUseCase(new syrincs.a_domain.chord.NoteCombinator(), new syrincs.a_domain.hindemith.ChordAnalysis(), 3);
        var analyze = new AnalyseChordByHindemithUseCase();
        var get = new GetHindemithChordsFromDbUseCase(repo);
        var persist = new PersistHindemithChordUseCase(repo);
        var genPersistRhythm = new GenerateAndPersistRhythmUseCase(rhythmRepo);
        var playHuffman = new PlayHuffmanRhythmsUseCase(rhythmPlayback, validate);
        return new UseCaseInteractor(send, validate, rhythmPlayback, new AnalyseRhythmUseCase(), repo,
                generate, analyze, get, persist, genPersistRhythm, playHuffman, rhythmRepo);
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
