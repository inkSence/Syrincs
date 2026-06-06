package syrincs.d_frameworksAndDrivers;

import java.util.Objects;

/**
 * Frameworks & Drivers configuration utilities.
 *
 * Centralizes robust resolution of configuration coming from CLI, environment variables,
 * and safe defaults. Keeps Main thin and prevents accidental usage of wrong DB users
 * (e.g., OS user fallback like "philipp").
 */
public final class AppConfig {

    private AppConfig() { }

    /** Simple DTO for database configuration. */
    public static final class DbConfig {
        public final String url;
        public final String user;
        public final String password;
        public DbConfig(String url, String user, String password) {
            this.url = Objects.requireNonNull(url, "url");
            this.user = Objects.requireNonNull(user, "user");
            this.password = Objects.requireNonNull(password, "password");
        }
    }

    private static String envOr(String key, String def) {
        String v = System.getenv(key);
        return (v == null || v.isBlank()) ? def : v;
    }

    /**
     * Resolve DB configuration from, in order of precedence:
     * 1) CLI flags: --db-url=, --db-user=, --db-pass=
     * 2) Environment variables: HINDEMITH_DB_URL, HINDEMITH_DB_USER, HINDEMITH_DB_PASSWORD
     * 3) Safe defaults
     *
     * Additionally:
     * - Treat blank env as unset (so we don't fall back to OS user).
     * - Log effective values (URL and user) for transparency.
     * - Fail fast if the user resolves to a disallowed value (e.g., "philipp").
     */
    public static DbConfig loadDbConfig(String[] args) {
        String cliUrl = null, cliUser = null, cliPass = null;
        if (args != null) {
            for (String a : args) {
                if (a == null) continue;
                if (a.startsWith("--db-url="))  cliUrl  = a.substring("--db-url=".length());
                else if (a.startsWith("--db-user=")) cliUser = a.substring("--db-user=".length());
                else if (a.startsWith("--db-pass=")) cliPass = a.substring("--db-pass=".length());
            }
        }

        String url  = (cliUrl  != null && !cliUrl.isBlank())  ? cliUrl  : envOr("HINDEMITH_DB_URL",  "jdbc:postgresql://localhost:5432/hindemith");
        String user = (cliUser != null && !cliUser.isBlank()) ? cliUser : envOr("HINDEMITH_DB_USER", "syrincs");
        String pass = (cliPass != null && !cliPass.isBlank()) ? cliPass : envOr("HINDEMITH_DB_PASSWORD", "syrincs");

        if (user == null || user.isBlank()) {
            throw new IllegalStateException("DB user is blank after resolution. Set HINDEMITH_DB_USER or --db-user.");
        }
        // Guardrail to prevent accidental OS user or unwanted account
        if ("philipp".equalsIgnoreCase(user)) {
            throw new IllegalStateException("Refusing to run with DB user 'philipp'. Set HINDEMITH_DB_USER or --db-user.");
        }
        return new DbConfig(url, user, pass);
    }
}
