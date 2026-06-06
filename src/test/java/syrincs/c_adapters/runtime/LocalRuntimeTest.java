package syrincs.c_adapters.runtime;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class LocalRuntimeTest {

    @Test
    void schemaStatementsContainBothApplicationTables() {
        List<String> statements = LocalRuntime.schemaStatements();

        assertEquals(2, statements.size());
        assertTrue(statements.get(0).contains("hindemithChords"));
        assertTrue(statements.get(1).contains("huffmanRhythms"));
    }
}
