package com.bharathandukuri.compilr.environment;

import com.bharathandukuri.compilr.compiler.dto.ExecuteResponse;
import com.bharathandukuri.compilr.compiler.enums.ExecutionStatus;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("MySQL 8.0 Execution Environment Tests")
class MySqlExecutionEnvironmentTest extends BaseEnvironmentTest {

    @Test
    @DisplayName("Verifies MySQL version is 8.0")
    void verifyMySqlVersion() {
        String sql = "SELECT VERSION();";
        ExecuteResponse response = execute("mysql-8.0", sql);
        assertThat(response.status()).isEqualTo(ExecutionStatus.SUCCESS);
        assertThat(response.stdout()).contains("8.0.");
    }

    @Test
    @DisplayName("Executes simple SQL expression")
    void executeSimpleSql() {
        String sql = "SELECT CONCAT('Hello, ', 'MySQL!') AS greeting, 1 + 1 AS two;";
        ExecuteResponse response = execute("mysql-8.0", sql);
        assertThat(response.status()).isEqualTo(ExecutionStatus.SUCCESS);
        assertThat(response.stdout()).contains("Hello, MySQL!", "2");
    }

    @Test
    @DisplayName("Captures MySQL syntax error as RUNTIME_ERROR with diagnostic")
    void syntaxError() {
        String sql = "SELCT 1 FROM missing_table;";
        ExecuteResponse response = execute("mysql-8.0", sql);
        assertThat(response.status()).isEqualTo(ExecutionStatus.RUNTIME_ERROR);
        assertThat(response.stderr()).contains("ERROR 1064");
    }

    @Test
    @DisplayName("Executes DDL, DML, Joins, Aggregations, and JSON functions")
    void fullRelationalWorkflow() {
        String sql = """
                CREATE TABLE customers (
                    id INT PRIMARY KEY AUTO_INCREMENT,
                    name VARCHAR(50) NOT NULL,
                    meta JSON
                );

                CREATE TABLE orders (
                    id INT PRIMARY KEY AUTO_INCREMENT,
                    customer_id INT NOT NULL,
                    amount DECIMAL(10, 2) NOT NULL,
                    status VARCHAR(20) NOT NULL
                );

                INSERT INTO customers (name, meta) VALUES
                    ('Alice', '{"tier": "gold"}'),
                    ('Bob', '{"tier": "silver"}');

                INSERT INTO orders (customer_id, amount, status) VALUES
                    (1, 150.50, 'COMPLETED'),
                    (1, 49.50, 'COMPLETED'),
                    (2, 200.00, 'PENDING');

                SELECT 
                    c.name,
                    JSON_UNQUOTE(JSON_EXTRACT(c.meta, '$.tier')) AS tier,
                    COUNT(o.id) AS total_orders,
                    SUM(o.amount) AS total_spent
                FROM customers c
                INNER JOIN orders o ON c.id = o.customer_id
                WHERE o.status = 'COMPLETED'
                GROUP BY c.id, c.name, c.meta;
                """;

        ExecuteResponse response = execute("mysql-8.0", sql);

        assertThat(response.status()).isEqualTo(ExecutionStatus.SUCCESS);
        assertThat(response.exitCode()).isEqualTo(0L);
        assertThat(response.stdout())
                .contains("Alice")
                .contains("gold")
                .contains("200.00");
    }

    @Test
    @DisplayName("Ephemeral Isolation: tables created in one run do NOT exist in the next run")
    void ephemeralIsolation() {
        String createSql = """
                CREATE TABLE mysql_isolated_tbl (id INT);
                INSERT INTO mysql_isolated_tbl VALUES (9876);
                SELECT * FROM mysql_isolated_tbl;
                """;
        ExecuteResponse firstResponse = execute("mysql-8.0", createSql);
        assertThat(firstResponse.status()).isEqualTo(ExecutionStatus.SUCCESS);
        assertThat(firstResponse.stdout()).contains("9876");

        String checkSql = "SELECT * FROM mysql_isolated_tbl;";
        ExecuteResponse secondResponse = execute("mysql-8.0", checkSql);
        assertThat(secondResponse.status()).isEqualTo(ExecutionStatus.RUNTIME_ERROR);
        assertThat(secondResponse.stderr()).contains("Table 'stacked_judge_db.mysql_isolated_tbl' doesn't exist");
    }
}
