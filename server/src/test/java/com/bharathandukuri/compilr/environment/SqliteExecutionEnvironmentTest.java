package com.bharathandukuri.compilr.environment;

import com.bharathandukuri.compilr.compiler.dto.ExecuteResponse;
import com.bharathandukuri.compilr.compiler.enums.ExecutionStatus;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("SQLite 3 Execution Environment Tests")
class SqliteExecutionEnvironmentTest extends BaseEnvironmentTest {

    @Test
    @DisplayName("Executes DDL, DML, and queries in SQLite 3")
    void fullWorkflow() {
        String sql = """
                CREATE TABLE products (id INTEGER PRIMARY KEY, name TEXT, price REAL);
                INSERT INTO products (name, price) VALUES ('Widget', 19.99), ('Gadget', 29.99);
                SELECT name, price FROM products ORDER BY price DESC;
                """;
        ExecuteResponse response = execute("sqlite-3", sql);
        assertThat(response.status()).isEqualTo(ExecutionStatus.SUCCESS);
        assertThat(response.stdout())
                .contains("Gadget")
                .contains("29.99")
                .contains("Widget")
                .contains("19.99");
    }
}
