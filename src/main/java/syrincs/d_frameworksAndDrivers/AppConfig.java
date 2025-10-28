package syrincs.d_frameworksAndDrivers;

import java.util.Objects;

/**
 * Frameworks & Drivers configuration utilities.
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
     * Additionally:
     * - Treat blank env as unset (so we don't fall back to OS user).
     * - Log effective values (URL and user) for transparency.
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

//        String url  = (cliUrl  != null && !cliUrl.isBlank())  ? cliUrl  : envOr("HINDEMITH_DB_URL",  "jdbc:postgresql://localhost:5432/syrincsDb");
//        String user = (cliUser != null && !cliUser.isBlank()) ? cliUser : envOr("HINDEMITH_DB_USER", "syrincs");
//        String pass = (cliPass != null && !cliPass.isBlank()) ? cliPass : envOr("HINDEMITH_DB_PASSWORD", "syrincs");

        String url = "jdbc:postgresql://localhost:5432/syrincsdb";
        String user = "syrincs";
        String pass = "syrincs";

//        if (user == null || user.isBlank()) {
//            throw new IllegalStateException("DB user is blank after resolution. Set HINDEMITH_DB_USER or --db-user.");
//        }
//        // Guardrail to prevent accidental OS user or unwanted account
//        if ("philipp".equalsIgnoreCase(user)) {
//            throw new IllegalStateException("Refusing to run with DB user 'philipp'. Set HINDEMITH_DB_USER or --db-user.");
//        }
        return new DbConfig(url, user, pass);
    }
}
