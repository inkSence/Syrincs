package syrincs.c_adapters.runtime;

import syrincs.d_frameworksAndDrivers.AppConfig;

import java.io.IOException;
import java.io.PrintStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

public class LocalRuntime {
    private final Path projectRoot;
    private final AppConfig.DbConfig dbConfig;

    public LocalRuntime(Path projectRoot, AppConfig.DbConfig dbConfig) {
        this.projectRoot = projectRoot.toAbsolutePath().normalize();
        this.dbConfig = dbConfig;
    }

    public static Path resolveProjectRoot() {
        List<Path> candidates = new ArrayList<>();

        String envHome = System.getenv("SYRINCS_HOME");
        if (envHome != null && !envHome.isBlank()) {
            candidates.add(Path.of(envHome));
        }

        String appHome = System.getProperty("app.home");
        if (appHome != null && !appHome.isBlank()) {
            Path appHomePath = Path.of(appHome);
            candidates.add(appHomePath);
            Path parent = appHomePath.getParent();
            if (parent != null && parent.getParent() != null) {
                candidates.add(parent.getParent());
            }
        }

        candidates.add(Path.of("").toAbsolutePath());

        for (Path candidate : candidates) {
            Path normalized = candidate.toAbsolutePath().normalize();
            if (Files.isRegularFile(normalized.resolve("supercollider/syrincs_osc_consumer.scd"))) {
                return normalized;
            }
        }

        return Path.of("").toAbsolutePath().normalize();
    }

    public int start(String target, PrintStream out, PrintStream err) throws IOException, InterruptedException {
        RuntimeTarget runtimeTarget = RuntimeTarget.parse(target);
        return switch (runtimeTarget) {
            case ALL -> startAll(out, err);
            case DB -> startDatabase(out, err);
            case SC -> startSuperCollider(out, err);
        };
    }

    public int initializeDatabaseSchema(PrintStream out, PrintStream err) {
        DatabaseStatus dbStatus = databaseStatus();
        if (!dbStatus.reachable()) {
            err.printf("[INIT] %s%n", dbStatus.label());
            if (!dbStatus.message().isBlank()) {
                err.printf("[INIT] %s%n", dbStatus.message());
            }
            err.println("[INIT] Start PostgreSQL and create the configured database first.");
            err.println("[INIT] See: scripts/init-postgres.sh");
            return 1;
        }

        try (var con = DriverManager.getConnection(dbConfig.url, dbConfig.user, dbConfig.password);
             var st = con.createStatement()) {
            con.setAutoCommit(false);
            for (String sql : schemaStatements()) {
                st.execute(sql);
            }
            con.commit();
            out.println("[INIT] Database schema is ready.");
            return 0;
        } catch (SQLException e) {
            err.printf("[INIT] Failed to initialize database schema: %s%n", e.getMessage());
            return 1;
        }
    }

    public int printStatus(PrintStream out) {
        DatabaseStatus dbStatus = databaseStatus();
        boolean scRunning = isSuperColliderConsumerRunning();

        out.printf("[DB] %s %s user=%s%n",
                dbStatus.label(),
                dbConfig.url,
                dbConfig.user);
        if (!dbStatus.reachable()) {
            out.printf("[DB] %s%n", dbStatus.message());
        }
        out.printf("[SC] %s SuperCollider consumer process%n", scRunning ? "RUNNING" : "NOT_RUNNING");
        out.printf("[HOME] %s%n", projectRoot);

        return dbStatus.reachable() && scRunning ? 0 : 1;
    }

    public boolean isDatabaseReachable() {
        return databaseStatus().reachable();
    }

    public DatabaseStatus databaseStatus() {
        int oldTimeout = DriverManager.getLoginTimeout();
        DriverManager.setLoginTimeout(2);
        try {
            try (var ignored = DriverManager.getConnection(dbConfig.url, dbConfig.user, dbConfig.password)) {
                return new DatabaseStatus(true, "OK", "Database is reachable.");
            }
        } catch (SQLException e) {
            return classifyDatabaseFailure(e);
        } finally {
            DriverManager.setLoginTimeout(oldTimeout);
        }
    }

    public boolean isSuperColliderConsumerRunning() {
        String scriptName = "syrincs_osc_consumer.scd";
        return ProcessHandle.allProcesses()
                .map(ProcessHandle::info)
                .map(ProcessHandle.Info::commandLine)
                .flatMap(commandLine -> commandLine.stream())
                .anyMatch(commandLine -> commandLine.contains(scriptName));
    }

    private int startAll(PrintStream out, PrintStream err) throws IOException, InterruptedException {
        int dbExit = startDatabase(out, err);
        if (dbExit != 0) {
            err.println("[DB] Continuing with SuperCollider. Database-backed commands may still fail.");
        }
        return startSuperCollider(out, err);
    }

    private int startDatabase(PrintStream out, PrintStream err) {
        DatabaseStatus dbStatus = databaseStatus();
        if (dbStatus.reachable()) {
            out.printf("[DB] Already reachable: %s user=%s%n", dbConfig.url, dbConfig.user);
            return 0;
        }

        printDatabaseHint(err, dbStatus);
        return 1;
    }

    private int startSuperCollider(PrintStream out, PrintStream err) throws IOException, InterruptedException {
        if (isSuperColliderConsumerRunning()) {
            out.println("[SC] SuperCollider consumer is already running.");
            return 0;
        }

        Path script = projectRoot.resolve("scripts/start-supercollider-consumer.sh");
        if (!Files.isRegularFile(script)) {
            err.printf("[SC] Cannot find startup script: %s%n", script);
            err.println("[SC] Set SYRINCS_HOME to the repository root if you run Syrincs from another location.");
            return 1;
        }

        out.println("[SC] Starting SuperCollider consumer in foreground.");
        out.println("[SC] Stop with Ctrl+C.");
        Process process = new ProcessBuilder("bash", script.toString())
                .directory(projectRoot.toFile())
                .inheritIO()
                .start();
        return process.waitFor();
    }

    private DatabaseStatus classifyDatabaseFailure(SQLException e) {
        String message = e.getMessage() == null ? "" : e.getMessage();
        String lower = message.toLowerCase(Locale.ROOT);

        if (lower.contains("database") && lower.contains("does not exist")) {
            return new DatabaseStatus(false, "DATABASE_MISSING", message);
        }
        if (lower.contains("password authentication failed") || lower.contains("authentication failed")) {
            return new DatabaseStatus(false, "AUTH_FAILED", message);
        }
        if (lower.contains("connection to") && lower.contains("refused")
                || lower.contains("connection attempt failed")) {
            return new DatabaseStatus(false, "SERVER_NOT_RUNNING", message);
        }
        return new DatabaseStatus(false, "NOT_REACHABLE", message);
    }

    private void printDatabaseHint(PrintStream err, DatabaseStatus dbStatus) {
        err.printf("[DB] %s%n", dbStatus.label());
        if (!dbStatus.message().isBlank()) {
            err.printf("[DB] %s%n", dbStatus.message());
        }
        err.println("[DB] Syrincs does not run privileged database start commands automatically.");
        err.println("[DB] Check clusters: pg_lsclusters");
        err.println("[DB] Try: sudo pg_ctlcluster <version> <cluster> start");
        err.println("[DB] Fallback: sudo systemctl start postgresql");
        err.println("[DB] Check: pg_isready -h localhost -p 5432");
        err.println("[DB] Current config: " + dbConfig.url + " user=" + dbConfig.user);
    }

    public record DatabaseStatus(boolean reachable, String label, String message) {
    }

    public static List<String> schemaStatements() {
        return Arrays.asList(
                """
                CREATE TABLE IF NOT EXISTS public.hindemithChords (
                    id SERIAL PRIMARY KEY,
                    notes INT[] NOT NULL,
                    numNotes INT NOT NULL,
                    minNote INT NOT NULL,
                    maxNote INT NOT NULL,
                    rootNote INT NOT NULL,
                    chordGroup INT NOT NULL
                )
                """,
                """
                CREATE TABLE IF NOT EXISTS public.huffmanRhythms (
                    id BIGSERIAL PRIMARY KEY,
                    rhythmstring VARCHAR(100),
                    numerator SMALLINT NOT NULL,
                    denominator SMALLINT NOT NULL,
                    info SMALLINT NOT NULL,
                    deviation DOUBLE PRECISION
                )
                """,
                """
                ALTER TABLE public.huffmanRhythms
                    ADD COLUMN IF NOT EXISTS rhythmstring VARCHAR(100)
                """,
                """
                ALTER TABLE public.huffmanRhythms
                    ADD COLUMN IF NOT EXISTS deviation DOUBLE PRECISION
                """,
                """
                DO $$
                BEGIN
                    IF NOT EXISTS (
                        SELECT 1
                        FROM pg_attrdef d
                        JOIN pg_attribute a
                          ON a.attrelid = d.adrelid
                         AND a.attnum = d.adnum
                        WHERE d.adrelid = 'public.huffmanRhythms'::regclass
                          AND a.attname = 'id'
                    ) THEN
                        ALTER TABLE public.huffmanRhythms
                            ALTER COLUMN id ADD GENERATED BY DEFAULT AS IDENTITY;
                    END IF;
                END $$;
                """
        );
    }

    private enum RuntimeTarget {
        ALL,
        DB,
        SC;

        static RuntimeTarget parse(String value) {
            if (value == null || value.isBlank() || "all".equalsIgnoreCase(value)) {
                return ALL;
            }
            String normalized = value.toLowerCase(Locale.ROOT);
            return switch (normalized) {
                case "db", "database", "postgres", "postgresql" -> DB;
                case "sc", "supercollider" -> SC;
                default -> throw new IllegalArgumentException("Unknown runtime target: " + value);
            };
        }
    }
}
