// src/main/java/syrincs/Main.java
package syrincs;

import picocli.CommandLine;
import syrincs.b_application.UseCaseInteractor;
import syrincs.c_adapters.midi.JdkMidiOutputAdapter;
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
        var interactor = new UseCaseInteractor(midiAdapter, repo);

        // Filter out DB-related CLI flags before passing to PicoCli so they don't appear in help
        String[] filtered = filterDbArgs(args);


        // If root-level help requested, print extended help including subcommand usages and exit
        if (isRootHelpRequest(filtered)) {
            var root = new CommandLine(new RootCmd(interactor));
            printExtendedHelp(root);
            return;
        }

        var cmd = new CommandLine(new RootCmd(interactor));
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
            if (lower.startsWith("--db-url=") || lower.startsWith("--db-user=") || lower.startsWith("--db-pass=")) {
                // skip
            } else {
                out.add(a);
            }
        }
        return out.toArray(String[]::new);
    }

    private static boolean isRootHelpRequest(String[] args) {
        if (args == null) return false;
        if (args.length != 1) return false;
        String a0 = args[0];
        return "-h".equals(a0) || "--help".equals(a0);
    }

    private static void printExtendedHelp(CommandLine root) {
        // Print root usage
        root.usage(System.out);
        // Also show usage for 'play', 'play note' and 'play chords'
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
            CommandLine rhythm = play.getSubcommands().get("rhythm");
            if (rhythm != null) {
                System.out.println();
                System.out.println("Subcommand 'play rhythm' usage:");
                rhythm.usage(System.out);
            }
        }
    }
}
