package syrincs.c_adapters.cli;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

class RhythmInfoBootstrapTest {
    @TempDir Path temporary;

    record Run(int code, String out, String err) {}

    private Run run(String... args) throws Exception {
        List<String> command =
                new ArrayList<>(
                        List.of(
                                Path.of(System.getProperty("java.home"), "bin", "java").toString(),
                                "-cp",
                                System.getProperty("java.class.path"),
                                "syrincs.Main"));
        command.addAll(List.of(args));
        Path out = Files.createTempFile(temporary, "out", ".txt"),
                err = Files.createTempFile(temporary, "err", ".txt");
        var builder =
                new ProcessBuilder(command)
                        .redirectOutput(out.toFile())
                        .redirectError(err.toFile());
        builder.environment().put("SYRINCS_DB_USER", "philipp");
        builder.environment().put("SYRINCS_DB_URL", "not-a-jdbc-url");
        builder.environment().put("SYRINCS_MIDI_DEVICE", "deliberately absent device");
        var process = builder.start();
        try {
            assertTrue(process.waitFor(30, TimeUnit.SECONDS), "CLI timed out");
        } finally {
            if (process.isAlive()) process.destroyForcibly();
        }
        return new Run(process.exitValue(), Files.readString(out), Files.readString(err));
    }

    @Test
    void newCommandsAndHelpDoNotUseLegacyConfiguration() throws Exception {
        assertEquals(0, run("rhythm-info", "analyze", "xooo", "--format", "json").code);
        assertEquals(0, run("rhythm-info", "decode", "--slots", "4", "--code", "000").code);
        assertEquals(
                0,
                run(
                                "rhythm-info",
                                "generate",
                                "--slots",
                                "4",
                                "--info",
                                "3",
                                "--strategy",
                                "exhaustive")
                        .code);
        assertEquals(0, run("rhythm-info", "--help").code);
        assertEquals(0, run("rhythm-info").code);
        var unknown = run("rhythm-info", "missing");
        assertEquals(2, unknown.code, unknown.err);
        var invalid = run("rhythm-info", "analyze", "xooo", "--no-such-option");
        assertEquals(2, invalid.code, invalid.err);
    }

    @Test
    void oldPathStillAppliesExistingDbUserGuard() throws Exception {
        var old = run("analyze", "rhythm", "xooo xooo xooo xooo");
        assertNotEquals(0, old.code);
        assertTrue(old.err.contains("philipp"), old.err);
        var filename = run("play", "rhythm", "--in", "rhythm-info");
        assertNotEquals(0, filename.code);
        assertTrue(filename.err.contains("philipp"), filename.err);
    }
}
