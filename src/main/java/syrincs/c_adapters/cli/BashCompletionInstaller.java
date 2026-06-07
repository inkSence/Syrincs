package syrincs.c_adapters.cli;

import picocli.AutoComplete;
import picocli.CommandLine;
import syrincs.b_application.ports.MidiDeviceQueryPort;

import java.io.IOException;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

public final class BashCompletionInstaller {
    private BashCompletionInstaller() {
    }

    public static void main(String[] args) {
        Path target = args != null && args.length > 0
                ? Path.of(args[0])
                : defaultBashCompletionPath();
        int exitCode = install(target, System.out, System.err);
        if (exitCode != 0) {
            System.exit(exitCode);
        }
    }

    static int install(Path target, PrintStream out, PrintStream err) {
        try {
            Path parentDir = target.getParent();
            if (parentDir != null) {
                Files.createDirectories(parentDir);
            }
            Files.writeString(target, bashCompletionScript(), StandardCharsets.UTF_8);
            out.printf("[COMPLETION] Installed bash completion to %s%n", target);
            out.println("[COMPLETION] Open a new shell or run: exec bash");
            return 0;
        } catch (IOException e) {
            err.println("[COMPLETION] Failed to install bash completion: " + e.getMessage());
            return 1;
        }
    }

    static String bashCompletionScript() {
        RootCmd root = new RootCmd(null, (MidiDeviceQueryPort) null);
        return AutoComplete.bash("syrincs", new CommandLine(root));
    }

    static Path defaultBashCompletionPath() {
        return Path.of(
                System.getProperty("user.home"),
                ".local",
                "share",
                "bash-completion",
                "completions",
                "syrincs"
        );
    }
}
