package syrincs.c_adapters.cli;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import picocli.CommandLine;
import syrincs.b_application.ports.MidiDeviceQueryPort;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class RootCmdCompletionCliTest {

    @TempDir
    Path tempDir;

    @Test
    void internalInstaller_generatesBashCompletionForSyrincs() {
        String text = BashCompletionInstaller.bashCompletionScript();
        List<String> rootCommands = rootCommandTokens(text);

        assertTrue(text.contains("complete -F"));
        assertTrue(text.contains("syrincs"));
        assertTrue(rootCommands.contains("devices"));
        assertTrue(rootCommands.contains("play"));
        assertTrue(rootCommands.contains("calculate"));
        assertTrue(rootCommands.contains("status"));
        assertTrue(rootCommands.contains("analyze"));
        assertTrue(text.contains("--device"));
        assertTrue(text.contains("--output"));
        assertTrue(text.contains("--host"));
        assertTrue(text.contains("--port"));
    }

    @Test
    void completionCommand_isNotPartOfThePublicCli() {
        RootCmd root = new RootCmd(null, (MidiDeviceQueryPort) null);
        ByteArrayOutputStream error = new ByteArrayOutputStream();
        PrintStream originalErr = System.err;
        try {
            System.setErr(new PrintStream(error));

            int code = new CommandLine(root).execute("completion");

            assertNotEquals(0, code);
        } finally {
            System.setErr(originalErr);
        }

        String text = error.toString(StandardCharsets.UTF_8);
        assertTrue(text.contains("Unmatched argument"));
    }

    @Test
    void internalInstaller_writesCompletionScriptToPath() throws Exception {
        Path target = tempDir.resolve("syrincs");
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        ByteArrayOutputStream error = new ByteArrayOutputStream();

        int code = BashCompletionInstaller.install(
                target,
                new PrintStream(output),
                new PrintStream(error)
        );

        assertEquals(0, code);

        String text = output.toString(StandardCharsets.UTF_8);
        assertTrue(text.contains("Installed bash completion"));
        assertTrue(text.contains("exec bash"));
        String script = Files.readString(target, StandardCharsets.UTF_8);
        assertTrue(script.contains("complete -F"));
        assertTrue(script.contains("syrincs"));
    }

    private static List<String> rootCommandTokens(String script) {
        String commandsLine = script.lines()
                .filter(line -> line.startsWith("  local commands=\"devices "))
                .findFirst()
                .orElseThrow();
        return List.of(commandsLine.substring("  local commands=\"".length(), commandsLine.length() - 1).split(" "));
    }
}
