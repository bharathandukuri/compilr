package com.bharathandukuri.compilr.environment;

import com.bharathandukuri.compilr.compiler.dto.CompilerOptionsDto;
import com.bharathandukuri.compilr.compiler.dto.ExecuteRequest;
import com.bharathandukuri.compilr.compiler.dto.ExecuteResponse;
import com.bharathandukuri.compilr.compiler.enums.ExecutionStatus;
import com.bharathandukuri.compilr.compiler.service.CompilerService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("Database Languages Execution Environment Integration Tests")
class DatabaseLanguagesExecutionEnvironmentTest extends BaseEnvironmentTest {

    @Autowired
    private CompilerService compilerService;

    @Nested
    @DisplayName("PostgreSQL 16 Advanced Database Execution")
    class PostgreSQLTests {

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
                    .contains("90000.00") // AVG for Engineering (95000 + 85000) / 2
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
            assertThat(response.stdout()).contains("15"); // Running total sum of 1..5 is 15
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

    @Nested
    @DisplayName("MySQL 8.0 Advanced Database Execution")
    class MySQLTests {

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

    @Nested
    @DisplayName("SQLite 3 Database Execution")
    class SQLiteTests {

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

    @Nested
    @DisplayName("MongoDB 8.0 Document Database Execution")
    class MongoDbTests {

        @Test
        @DisplayName("Executes Insert and Query in MongoDB")
        void fullWorkflow() {
            String script = """
                    db.items.insertMany([
                        { item: "journal", qty: 25, status: "A" },
                        { item: "notebook", qty: 50, status: "A" }
                    ]);
                    printjson(db.items.find({ status: "A" }).toArray());
                    """;
            ExecuteResponse response = execute("mongodb-8.0", script);
            assertThat(response.status()).isEqualTo(ExecutionStatus.SUCCESS);
            assertThat(response.stdout())
                    .contains("journal")
                    .contains("notebook");
        }
    }

    private ExecuteResponse execute(String language, String sql) {
        ExecuteRequest request = new ExecuteRequest(
                language,
                sql,
                "",
                new CompilerOptionsDto(10000L, 524288L)
        );
        return compilerService.execute(request);
    }
}
