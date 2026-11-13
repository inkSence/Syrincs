package syrincs.c_adapters.postgres;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import syrincs.b_application.AppDefaults;
import syrincs.b_application.SearchRhythmsUseCase;
import syrincs.b_application.ports.dto.DeviationRange;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Proxy;
import java.sql.*;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.UUID;
import java.util.logging.Logger;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

/** Verifies the actual adapter's SQL, typed bindings, mapping and resource handling. */
class PostgresRhythmRepositoryTest {
    private static final String SELECT = "SELECT rhythmstring, numerator, denominator FROM public.huffmanRhythms WHERE info = ?";

    @ParameterizedTest
    @MethodSource("rangeQueries")
    void rangeQueryUsesOnlyPresentInclusivePredicates(DeviationRange range, String predicates,
                                                     Map<Integer, Object> parameters) throws Exception {
        try (var jdbc = new CapturingJdbc()) {
            var result = jdbc.repository().getAllByInformationAndDeviationRange(3, range);

            assertEquals(SELECT + predicates + " ORDER BY id", jdbc.sql);
            assertEquals(parameters, jdbc.parameters);
            assertTrue(result.isEmpty());
            assertTrue(jdbc.connectionClosed);
            assertTrue(jdbc.statementClosed);
            assertTrue(jdbc.resultClosed);
        }
    }

    static Stream<Arguments> rangeQueries() {
        return Stream.of(
                Arguments.of(new DeviationRange(null, null), "", Map.of(1, 3)),
                Arguments.of(new DeviationRange(0.2, null), " AND deviation >= ?", Map.of(1, 3, 2, 0.2)),
                Arguments.of(new DeviationRange(null, 0.8), " AND deviation <= ?", Map.of(1, 3, 2, 0.8)),
                Arguments.of(new DeviationRange(0.2, 0.8), " AND deviation >= ? AND deviation <= ?", Map.of(1, 3, 2, 0.2, 3, 0.8)),
                Arguments.of(new DeviationRange(0.0, 0.0), " AND deviation >= ? AND deviation <= ?", Map.of(1, 3, 2, 0.0, 3, 0.0)));
    }

    @Test
    void legacyMinimumRemainsStrictAndUnfilteredGradeQueryStaysUnchanged() throws Exception {
        try (var jdbc = new CapturingJdbc()) {
            var repository = jdbc.repository();
            repository.getAllByInformationAndMinDeviation(3, 0.7);
            assertEquals(SELECT + " AND deviation > ? ORDER BY id", jdbc.sql);
            assertEquals(Map.of(1, 3, 2, 0.7), jdbc.parameters);

            jdbc.parameters.clear();
            repository.getAllByInformation(3);
            assertEquals(SELECT + " ORDER BY id", jdbc.sql);
            assertEquals(Map.of(1, 3), jdbc.parameters);
        }
    }

    @Test
    void returnedRowsUseDefaultTempoAndAreNotRefilteredByReconstructedMetadata() throws Exception {
        try (var jdbc = new CapturingJdbc()) {
            // Simulate SQL-selected legacy rows. The adapter must not re-filter current
            // aggregates: stored info/deviation, not reconstructed values, drive SQL.
            jdbc.rows = List.of(
                    new Row("xoxo xooo xoxo xooo", 4, 4),
                    new Row("xooo xoxo xooo xoxo", 4, 4));
            var result = jdbc.repository().getAllByInformationAndDeviationRange(3, new DeviationRange(0.2, 0.5));

            assertEquals(List.of("xoxoxoooxoxoxooo", "xoooxoxoxoooxoxo"),
                    result.stream().map(r -> r.getOnsetList()).toList());
            assertTrue(result.getFirst().getStandardDeviation() > 0.5);
            assertEquals(AppDefaults.DEFAULT_TEMPO_BPM, result.getFirst().getTempo());
            assertEquals(List.of(2, 0, 1, 0), result.getFirst().getBeatInformation());
        }
    }

    @Test
    void databaseFailuresPropagateWithCauseAndExistingHint() throws Exception {
        try (var jdbc = new CapturingJdbc()) {
            jdbc.failure = new SQLException("relation huffmanRhythms does not exist", "42P01");
            var error = assertThrows(RuntimeException.class, () ->
                    jdbc.repository().getAllByInformationAndDeviationRange(3, new DeviationRange(0.0, null)));
            assertSame(jdbc.failure, error.getCause());
            assertTrue(error.getMessage().contains("SQLState=42P01"));
            assertTrue(error.getMessage().contains("syrincs init"));
            assertTrue(jdbc.connectionClosed);
            assertTrue(jdbc.statementClosed);
        }
    }

    @Test
    void searchUsesUnboundedSqlThenDeduplicatesCountsAndLimitsReconstructedRows() throws Exception {
        try (var jdbc = new CapturingJdbc()) {
            jdbc.rows = List.of(new Row("xoxo xooo xoxo xooo", 4, 4),
                    new Row("XOOO XOXO XOOO XOXO", 4, 4),
                    new Row("xooo xoxo xooo xoxo", 4, 4));
            var result = new SearchRhythmsUseCase(jdbc.repository()).search(3, new DeviationRange(null, null), 1);
            assertEquals(SELECT + " ORDER BY id", jdbc.sql);
            assertEquals(Map.of(1, 3), jdbc.parameters);
            assertEquals(2, result.totalMatches());
            assertEquals(1, result.candidates().size());
            assertEquals("xoooxoxoxoooxoxo", result.candidates().getFirst().onsets());
            assertEquals(List.of(1, 1, 0, 1), result.candidates().getFirst().beatInformation());
            assertTrue(jdbc.connectionClosed && jdbc.statementClosed && jdbc.resultClosed);
        }
    }

    @Test
    void missingArgumentsFailBeforeOpeningConnection() throws Exception {
        try (var jdbc = new CapturingJdbc()) {
            assertThrows(NullPointerException.class, () ->
                    jdbc.repository().getAllByInformationAndDeviationRange(null, new DeviationRange(null, null)));
            assertThrows(NullPointerException.class, () ->
                    jdbc.repository().getAllByInformationAndDeviationRange(3, null));
            assertEquals(0, jdbc.connections);
        }
    }

    private record Row(String onsets, int numerator, int denominator) {}

    /** No network or database: only this unique test URL is intercepted. */
    private static final class CapturingJdbc implements Driver, AutoCloseable {
        final String url = "jdbc:vr4-test:" + UUID.randomUUID();
        final Map<Integer, Object> parameters = new LinkedHashMap<>();
        List<Row> rows = List.of();
        SQLException failure;
        String sql;
        int connections;
        boolean connectionClosed, statementClosed, resultClosed;

        CapturingJdbc() throws SQLException { DriverManager.registerDriver(this); }

        PostgresRhythmRepository repository() { return new PostgresRhythmRepository(url, "test", "test"); }

        @Override public Connection connect(String candidateUrl, Properties properties) {
            if (!acceptsURL(candidateUrl)) return null;
            connections++;
            return proxy(Connection.class, (proxy, method, args) -> switch (method.getName()) {
                case "prepareStatement" -> { sql = (String) args[0]; yield statement(); }
                case "close" -> { connectionClosed = true; yield null; }
                default -> throw new UnsupportedOperationException(method.getName());
            });
        }

        private PreparedStatement statement() {
            return proxy(PreparedStatement.class, (proxy, method, args) -> switch (method.getName()) {
                case "setInt", "setDouble" -> { parameters.put((Integer) args[0], args[1]); yield null; }
                case "executeQuery" -> {
                    if (failure != null) throw failure;
                    yield resultSet();
                }
                case "close" -> { statementClosed = true; yield null; }
                default -> throw new UnsupportedOperationException(method.getName());
            });
        }

        private ResultSet resultSet() {
            int[] position = {-1};
            return proxy(ResultSet.class, (proxy, method, args) -> switch (method.getName()) {
                case "next" -> ++position[0] < rows.size();
                case "getString" -> {
                    assertEquals("rhythmstring", args[0]); yield rows.get(position[0]).onsets();
                }
                case "getInt" -> switch ((String) args[0]) {
                    case "numerator" -> rows.get(position[0]).numerator();
                    case "denominator" -> rows.get(position[0]).denominator();
                    default -> throw new UnsupportedOperationException((String) args[0]);
                };
                case "close" -> { resultClosed = true; yield null; }
                default -> throw new UnsupportedOperationException(method.getName());
            });
        }

        private static <T> T proxy(Class<T> type, InvocationHandler handler) {
            return type.cast(Proxy.newProxyInstance(type.getClassLoader(), new Class<?>[]{type}, handler));
        }

        @Override public boolean acceptsURL(String candidate) { return url.equals(candidate); }
        @Override public DriverPropertyInfo[] getPropertyInfo(String url, Properties properties) { return new DriverPropertyInfo[0]; }
        @Override public int getMajorVersion() { return 1; }
        @Override public int getMinorVersion() { return 0; }
        @Override public boolean jdbcCompliant() { return false; }
        @Override public Logger getParentLogger() { return Logger.getLogger(getClass().getName()); }
        @Override public void close() throws SQLException { DriverManager.deregisterDriver(this); }
    }
}
