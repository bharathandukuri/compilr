package com.bharathandukuri.compilr.environment;

import com.bharathandukuri.compilr.compiler.dto.ExecuteResponse;
import com.bharathandukuri.compilr.compiler.enums.ExecutionStatus;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("PostgreSQL 16 Execution Environment Tests")
class PostgreSqlExecutionEnvironmentTest extends BaseEnvironmentTest {

    @Test
    @DisplayName("Verifies PostgreSQL version is 16")
    void verifyPostgreSqlVersion() {
        String sql = "SELECT version();";
        ExecuteResponse response = execute("postgresql-16", sql);
        assertThat(response.status()).isEqualTo(ExecutionStatus.SUCCESS);
        assertThat(response.stdout()).contains("PostgreSQL 16");
    }

    @Test
    @DisplayName("Executes simple SQL expression")
    void executeSimpleSql() {
        String sql = "SELECT 100 * 5 AS product, 'ready' AS status;";
        ExecuteResponse response = execute("postgresql-16", sql);
        assertThat(response.status()).isEqualTo(ExecutionStatus.SUCCESS);
        assertThat(response.stdout()).contains("500", "ready");
    }

    @Test
    @DisplayName("Captures SQL syntax error as RUNTIME_ERROR with diagnostic")
    void syntaxError() {
        String sql = "SELECT * FORM non_existent_table;";
        ExecuteResponse response = execute("postgresql-16", sql);
        assertThat(response.status()).isEqualTo(ExecutionStatus.RUNTIME_ERROR);
        assertThat(response.stderr()).contains("ERROR:", "syntax error");
    }

    @Test
    @DisplayName("Executes DDL, DML, Joins, Aggregations, and NULL handling")
    void fullRelationalWorkflow() {
        String sql = """
                CREATE TABLE departments (
                    id INT PRIMARY KEY,
                    name VARCHAR(50) NOT NULL
                );

                CREATE TABLE employees (
                    id INT PRIMARY KEY,
                    name VARCHAR(50) NOT NULL,
                    department_id INT,
                    salary NUMERIC(10, 2),
                    bonus NUMERIC(10, 2)
                );

                INSERT INTO departments VALUES
                    (1, 'Engineering'),
                    (2, 'Design'),
                    (3, 'Marketing');

                INSERT INTO employees VALUES
                    (101, 'Alice', 1, 95000.00, 5000.00),
                    (102, 'Bob', 1, 85000.00, NULL),
                    (103, 'Charlie', 2, 75000.00, 3000.00),
                    (104, 'Diana', NULL, 60000.00, NULL);

                -- Aggregate with JOIN, COALESCE, and GROUP BY
                SELECT 
                    COALESCE(d.name, 'Unassigned') AS dept_name,
                    COUNT(e.id) AS emp_count,
                    ROUND(AVG(e.salary), 2) AS avg_salary,
                    SUM(COALESCE(e.bonus, 0)) AS total_bonus
                FROM employees e
                LEFT JOIN departments d ON e.department_id = d.id
                GROUP BY d.name
                ORDER BY dept_name;
                """;

        ExecuteResponse response = execute("postgresql-16", sql);

        assertThat(response.status()).isEqualTo(ExecutionStatus.SUCCESS);
        assertThat(response.exitCode()).isEqualTo(0L);
        assertThat(response.stdout())
                .contains("Engineering", "Design", "Unassigned")
                .contains("90000.00")
                .contains("5000.00");
    }

    @Test
    @DisplayName("Common Table Expressions (WITH clause / CTE) and Window Functions")
    void cteAndWindowFunctions() {
        String sql = """
                WITH numbers AS (
                    SELECT generate_series(1, 5) AS n
                )
                SELECT 
                    n, 
                    n * n AS square,
                    SUM(n) OVER (ORDER BY n) AS running_total
                FROM numbers;
                """;

        ExecuteResponse response = execute("postgresql-16", sql);

        assertThat(response.status()).isEqualTo(ExecutionStatus.SUCCESS);
        assertThat(response.stdout()).contains("15");
    }

    @Test
    @DisplayName("Ephemeral Isolation: tables created in one run do NOT exist in the next run")
    void ephemeralIsolation() {
        String createSql = """
                CREATE TABLE ephemeral_probe (id INT);
                INSERT INTO ephemeral_probe VALUES (12345);
                SELECT * FROM ephemeral_probe;
                """;
        ExecuteResponse firstResponse = execute("postgresql-16", createSql);
        assertThat(firstResponse.status()).isEqualTo(ExecutionStatus.SUCCESS);
        assertThat(firstResponse.stdout()).contains("12345");

        // In second execution, ephemeral_probe must not exist
        String checkSql = "SELECT * FROM ephemeral_probe;";
        ExecuteResponse secondResponse = execute("postgresql-16", checkSql);
        assertThat(secondResponse.status()).isEqualTo(ExecutionStatus.RUNTIME_ERROR);
        assertThat(secondResponse.stderr()).contains("ephemeral_probe", "does not exist");
    }
}
