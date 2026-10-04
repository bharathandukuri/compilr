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

@DisplayName("All Languages Real Execution Environment Tests")
class AllLanguagesExecutionEnvironmentTest extends BaseEnvironmentTest {

    @Autowired
    private CompilerService compilerService;

    @Nested
    @DisplayName("Java 21 (OpenJDK 21)")
    class Java21Tests {

        @Test
        @DisplayName("Verifies OpenJDK version is 21")
        void verifyJavaVersion() {
            String code = """
                    public class Main {
                        public static void main(String[] args) {
                            System.out.println("JAVA_VERSION=" + System.getProperty("java.version"));
                        }
                    }
                    """;
            ExecuteResponse response = execute("java-21", code, "");
            assertThat(response.status()).isEqualTo(ExecutionStatus.SUCCESS);
            assertThat(response.stdout()).contains("JAVA_VERSION=21");
            assertThat(response.exitCode()).isEqualTo(0L);
        }

        @Test
        @DisplayName("Executes arithmetic and standard output")
        void executeValidJava() {
            String code = """
                    public class Main {
                        public static void main(String[] args) {
                            int sum = 0;
                            for (int i = 1; i <= 10; i++) sum += i;
                            System.out.println("SUM=" + sum);
                        }
                    }
                    """;
            ExecuteResponse response = execute("java-21", code, "");
            assertThat(response.status()).isEqualTo(ExecutionStatus.SUCCESS);
            assertThat(response.stdout()).contains("SUM=55");
        }

        @Test
        @DisplayName("Compilation error captures diagnostic compiler stderr")
        void compilationError() {
            String code = """
                    public class Main {
                        public static void main(String[] args) {
                            broken_syntax_without_semicolon
                        }
                    }
                    """;
            ExecuteResponse response = execute("java-21", code, "");
            assertThat(response.status()).isEqualTo(ExecutionStatus.COMPILATION_ERROR);
            assertThat(response.stderr()).contains("error:");
            assertThat(response.exitCode()).isNotEqualTo(0L);
        }

        @Test
        @DisplayName("Runtime error captures exception stack trace")
        void runtimeError() {
            String code = """
                    public class Main {
                        public static void main(String[] args) {
                            throw new IllegalArgumentException("Forced runtime failure in Java");
                        }
                    }
                    """;
            ExecuteResponse response = execute("java-21", code, "");
            assertThat(response.status()).isEqualTo(ExecutionStatus.RUNTIME_ERROR);
            assertThat(response.stderr()).contains("IllegalArgumentException", "Forced runtime failure in Java");
        }

        @Test
        @DisplayName("Processes multi-line stdin via Scanner")
        void stdinProcessing() {
            String code = """
                    import java.util.Scanner;
                    public class Main {
                        public static void main(String[] args) {
                            Scanner sc = new Scanner(System.in);
                            int a = sc.nextInt();
                            int b = sc.nextInt();
                            System.out.println("PRODUCT=" + (a * b));
                        }
                    }
                    """;
            ExecuteResponse response = execute("java-21", code, "7 8\n");
            assertThat(response.status()).isEqualTo(ExecutionStatus.SUCCESS);
            assertThat(response.stdout()).contains("PRODUCT=56");
        }

        @Test
        @DisplayName("Correctly outputs UTF-8 Unicode characters and emoji")
        void unicodeOutput() {
            String code = """
                    public class Main {
                        public static void main(String[] args) {
                            System.out.println("UNICODE: こんにちは 🚀 Compilr λ ∑ ≠ π");
                        }
                    }
                    """;
            ExecuteResponse response = execute("java-21", code, "");
            assertThat(response.status()).isEqualTo(ExecutionStatus.SUCCESS);
            assertThat(response.stdout()).contains("こんにちは 🚀 Compilr λ ∑ ≠ π");
        }

        @Test
        @DisplayName("Handles empty output without errors")
        void emptyOutput() {
            String code = """
                    public class Main {
                        public static void main(String[] args) {}
                    }
                    """;
            ExecuteResponse response = execute("java-21", code, "");
            assertThat(response.status()).isEqualTo(ExecutionStatus.SUCCESS);
            assertThat(response.stdout()).isEmpty();
            assertThat(response.exitCode()).isEqualTo(0L);
        }
    }

    @Nested
    @DisplayName("C 17 (GCC)")
    class C17Tests {

        @Test
        @DisplayName("Verifies C standard version is C17 (201710L)")
        void verifyCVersion() {
            String code = """
                    #include <stdio.h>
                    int main() {
                        printf("C_STDC_VERSION=%ld\\n", __STDC_VERSION__);
                        return 0;
                    }
                    """;
            ExecuteResponse response = execute("c-17", code, "");
            assertThat(response.status()).isEqualTo(ExecutionStatus.SUCCESS);
            assertThat(response.stdout()).contains("C_STDC_VERSION=201710");
        }

        @Test
        @DisplayName("Executes valid C program")
        void executeValidC() {
            String code = """
                    #include <stdio.h>
                    int main() {
                        int a = 12, b = 34;
                        printf("RESULT=%d\\n", a * b);
                        return 0;
                    }
                    """;
            ExecuteResponse response = execute("c-17", code, "");
            assertThat(response.status()).isEqualTo(ExecutionStatus.SUCCESS);
            assertThat(response.stdout()).contains("RESULT=408");
        }

        @Test
        @DisplayName("Captures gcc compilation error")
        void compilationError() {
            String code = """
                    #include <stdio.h>
                    int main() {
                        this_is_an_undefined_error();
                        return 0;
                    }
                    """;
            ExecuteResponse response = execute("c-17", code, "");
            assertThat(response.status()).isEqualTo(ExecutionStatus.COMPILATION_ERROR);
            assertThat(response.stderr()).contains("error:", "this_is_an_undefined_error");
        }

        @Test
        @DisplayName("Handles non-zero exit code via exit(42)")
        void runtimeError() {
            String code = """
                    #include <stdlib.h>
                    int main() {
                        exit(42);
                    }
                    """;
            ExecuteResponse response = execute("c-17", code, "");
            assertThat(response.status()).isEqualTo(ExecutionStatus.RUNTIME_ERROR);
            assertThat(response.exitCode()).isEqualTo(42L);
        }

        @Test
        @DisplayName("Processes multi-token stdin via scanf")
        void stdinProcessing() {
            String code = """
                    #include <stdio.h>
                    int main() {
                        int x, y;
                        if (scanf("%d %d", &x, &y) == 2) {
                            printf("SUM=%d\\n", x + y);
                        }
                        return 0;
                    }
                    """;
            ExecuteResponse response = execute("c-17", code, "40 2");
            assertThat(response.status()).isEqualTo(ExecutionStatus.SUCCESS);
            assertThat(response.stdout()).contains("SUM=42");
        }

        @Test
        @DisplayName("Outputs UTF-8 strings")
        void unicodeOutput() {
            String code = """
                    #include <stdio.h>
                    int main() {
                        printf("UNICODE_C: 世界 ⚡ α β γ\\n");
                        return 0;
                    }
                    """;
            ExecuteResponse response = execute("c-17", code, "");
            assertThat(response.status()).isEqualTo(ExecutionStatus.SUCCESS);
            assertThat(response.stdout()).contains("世界 ⚡ α β γ");
        }
    }

    @Nested
    @DisplayName("C++ 23 (GCC 13.2)")
    class Cpp23Tests {

        @Test
        @DisplayName("Verifies C++ standard version is C++23")
        void verifyCppVersion() {
            String code = """
                    #include <iostream>
                    int main() {
                        std::cout << "CPP_VERSION=" << __cplusplus << std::endl;
                        return 0;
                    }
                    """;
            ExecuteResponse response = execute("cpp-23", code, "");
            assertThat(response.status()).isEqualTo(ExecutionStatus.SUCCESS);
            assertThat(response.stdout()).containsAnyOf("CPP_VERSION=202100", "CPP_VERSION=202302");
        }

        @Test
        @DisplayName("Executes C++23 modern features (vector, ranges, lambdas)")
        void executeValidCpp() {
            String code = """
                    #include <iostream>
                    #include <vector>
                    #include <numeric>
                    int main() {
                        std::vector<int> numbers = {1, 2, 3, 4, 5};
                        int sum = std::accumulate(numbers.begin(), numbers.end(), 0);
                        std::cout << "ACCUMULATE=" << sum << std::endl;
                        return 0;
                    }
                    """;
            ExecuteResponse response = execute("cpp-23", code, "");
            assertThat(response.status()).isEqualTo(ExecutionStatus.SUCCESS);
            assertThat(response.stdout()).contains("ACCUMULATE=15");
        }

        @Test
        @DisplayName("Captures g++ compilation error")
        void compilationError() {
            String code = """
                    #include <iostream>
                    int main() {
                        std::cout << missing_variable << std::endl;
                        return 0;
                    }
                    """;
            ExecuteResponse response = execute("cpp-23", code, "");
            assertThat(response.status()).isEqualTo(ExecutionStatus.COMPILATION_ERROR);
            assertThat(response.stderr()).contains("error:", "missing_variable");
        }

        @Test
        @DisplayName("Handles non-zero exit code in C++ as RUNTIME_ERROR")
        void runtimeError() {
            String code = """
                    #include <cstdlib>
                    int main() {
                        std::exit(42);
                    }
                    """;
            ExecuteResponse response = execute("cpp-23", code, "");
            assertThat(response.status()).isEqualTo(ExecutionStatus.RUNTIME_ERROR);
            assertThat(response.exitCode()).isEqualTo(42L);
        }

        @Test
        @DisplayName("Reads standard input via std::cin")
        void stdinProcessing() {
            String code = """
                    #include <iostream>
                    #include <string>
                    int main() {
                        std::string name;
                        if (std::cin >> name) {
                            std::cout << "GREETING: Welcome, " << name << "!" << std::endl;
                        }
                        return 0;
                    }
                    """;
            ExecuteResponse response = execute("cpp-23", code, "Engineer");
            assertThat(response.status()).isEqualTo(ExecutionStatus.SUCCESS);
            assertThat(response.stdout()).contains("GREETING: Welcome, Engineer!");
        }
    }

    @Nested
    @DisplayName("Python 3.12 (CPython 3.12)")
    class Python312Tests {

        @Test
        @DisplayName("Verifies Python version is 3.12")
        void verifyPythonVersion() {
            String code = """
                    import sys
                    print(f"PY_VERSION={sys.version_info.major}.{sys.version_info.minor}")
                    """;
            ExecuteResponse response = execute("python-3.12", code, "");
            assertThat(response.status()).isEqualTo(ExecutionStatus.SUCCESS);
            assertThat(response.stdout()).contains("PY_VERSION=3.12");
        }

        @Test
        @DisplayName("Executes Python code with list comprehensions and dictionaries")
        void executeValidPython() {
            String code = """
                    squares = {x: x * x for x in range(1, 6)}
                    print(f"SQUARES={squares}")
                    """;
            ExecuteResponse response = execute("python-3.12", code, "");
            assertThat(response.status()).isEqualTo(ExecutionStatus.SUCCESS);
            assertThat(response.stdout()).contains("SQUARES={1: 1, 2: 4, 3: 9, 4: 16, 5: 25}");
        }

        @Test
        @DisplayName("Captures Python syntax error")
        void syntaxError() {
            String code = """
                    def broken(
                        print("missing paren")
                    """;
            ExecuteResponse response = execute("python-3.12", code, "");
            assertThat(response.status()).isEqualTo(ExecutionStatus.RUNTIME_ERROR);
            assertThat(response.stderr()).contains("SyntaxError");
        }

        @Test
        @DisplayName("Captures runtime exception with traceback")
        void runtimeError() {
            String code = """
                    def divide(a, b):
                        return a / b
                    divide(10, 0)
                    """;
            ExecuteResponse response = execute("python-3.12", code, "");
            assertThat(response.status()).isEqualTo(ExecutionStatus.RUNTIME_ERROR);
            assertThat(response.stderr()).contains("ZeroDivisionError: division by zero");
        }

        @Test
        @DisplayName("Reads multiline stdin from sys.stdin")
        void stdinProcessing() {
            String code = """
                    import sys
                    lines = [line.strip() for line in sys.stdin]
                    print(f"LINES_COUNT={len(lines)}, JOINED={'-'.join(lines)}")
                    """;
            ExecuteResponse response = execute("python-3.12", code, "alpha\nbeta\ngamma\n");
            assertThat(response.status()).isEqualTo(ExecutionStatus.SUCCESS);
            assertThat(response.stdout()).contains("LINES_COUNT=3, JOINED=alpha-beta-gamma");
        }

        @Test
        @DisplayName("Correctly prints UTF-8 and emoji")
        void unicodeOutput() {
            String code = """
                    print("PY_UNICODE: ✨ Привет 🐍 🌍 日本語")
                    """;
            ExecuteResponse response = execute("python-3.12", code, "");
            assertThat(response.status()).isEqualTo(ExecutionStatus.SUCCESS);
            assertThat(response.stdout()).contains("PY_UNICODE: ✨ Привет 🐍 🌍 日本語");
        }
    }

    @Nested
    @DisplayName("JavaScript (Node.js 20)")
    class JavaScriptNode20Tests {

        @Test
        @DisplayName("Verifies Node.js version starts with v20")
        void verifyNodeVersion() {
            String code = """
                    console.log("NODE_VERSION=" + process.version);
                    """;
            ExecuteResponse response = execute("javascript-node-20", code, "");
            assertThat(response.status()).isEqualTo(ExecutionStatus.SUCCESS);
            assertThat(response.stdout()).contains("NODE_VERSION=v20.");
        }

        @Test
        @DisplayName("Executes modern JavaScript features (Array methods, destructuring)")
        void executeValidJs() {
            String code = """
                    const items = [10, 20, 30];
                    const doubled = items.map(x => x * 2);
                    console.log("DOUBLED=" + JSON.stringify(doubled));
                    """;
            ExecuteResponse response = execute("javascript-node-20", code, "");
            assertThat(response.status()).isEqualTo(ExecutionStatus.SUCCESS);
            assertThat(response.stdout()).contains("DOUBLED=[20,40,60]");
        }

        @Test
        @DisplayName("Captures JavaScript syntax error")
        void syntaxError() {
            String code = """
                    const x = ;
                    """;
            ExecuteResponse response = execute("javascript-node-20", code, "");
            assertThat(response.status()).isEqualTo(ExecutionStatus.RUNTIME_ERROR);
            assertThat(response.stderr()).contains("SyntaxError");
        }

        @Test
        @DisplayName("Captures uncaught JavaScript exception")
        void runtimeError() {
            String code = """
                    throw new Error("Explicit JS runtime error");
                    """;
            ExecuteResponse response = execute("javascript-node-20", code, "");
            assertThat(response.status()).isEqualTo(ExecutionStatus.RUNTIME_ERROR);
            assertThat(response.stderr()).contains("Error: Explicit JS runtime error");
        }

        @Test
        @DisplayName("Reads stdin synchronously via fs.readFileSync")
        void stdinProcessing() {
            String code = """
                    const fs = require('fs');
                    const input = fs.readFileSync(0, 'utf-8').trim();
                    console.log("NODE_INPUT=" + input.toUpperCase());
                    """;
            ExecuteResponse response = execute("javascript-node-20", code, "fullstack node");
            assertThat(response.status()).isEqualTo(ExecutionStatus.SUCCESS);
            assertThat(response.stdout()).contains("NODE_INPUT=FULLSTACK NODE");
        }
    }

    @Nested
    @DisplayName("PostgreSQL 16")
    class PostgreSql16Tests {

        @Test
        @DisplayName("Verifies PostgreSQL version is 16")
        void verifyPostgreSqlVersion() {
            String sql = "SELECT version();";
            ExecuteResponse response = execute("postgresql-16", sql, "");
            assertThat(response.status()).isEqualTo(ExecutionStatus.SUCCESS);
            assertThat(response.stdout()).contains("PostgreSQL 16");
        }

        @Test
        @DisplayName("Executes simple SQL expression")
        void executeValidSql() {
            String sql = "SELECT 100 * 5 AS product, 'ready' AS status;";
            ExecuteResponse response = execute("postgresql-16", sql, "");
            assertThat(response.status()).isEqualTo(ExecutionStatus.SUCCESS);
            assertThat(response.stdout()).contains("500", "ready");
        }

        @Test
        @DisplayName("Captures SQL syntax error as RUNTIME_ERROR with diagnostic")
        void syntaxError() {
            String sql = "SELECT * FORM non_existent_table;";
            ExecuteResponse response = execute("postgresql-16", sql, "");
            assertThat(response.status()).isEqualTo(ExecutionStatus.RUNTIME_ERROR);
            assertThat(response.stderr()).contains("ERROR:", "syntax error");
        }
    }

    @Nested
    @DisplayName("MySQL 8.0")
    class MySql80Tests {

        @Test
        @DisplayName("Verifies MySQL version is 8.0")
        void verifyMySqlVersion() {
            String sql = "SELECT VERSION();";
            ExecuteResponse response = execute("mysql-8.0", sql, "");
            assertThat(response.status()).isEqualTo(ExecutionStatus.SUCCESS);
            assertThat(response.stdout()).contains("8.0.");
        }

        @Test
        @DisplayName("Executes simple SQL expression")
        void executeValidSql() {
            String sql = "SELECT CONCAT('Hello, ', 'MySQL!') AS greeting, 1 + 1 AS two;";
            ExecuteResponse response = execute("mysql-8.0", sql, "");
            assertThat(response.status()).isEqualTo(ExecutionStatus.SUCCESS);
            assertThat(response.stdout()).contains("Hello, MySQL!", "2");
        }

        @Test
        @DisplayName("Captures MySQL syntax error as RUNTIME_ERROR with diagnostic")
        void syntaxError() {
            String sql = "SELCT 1 FROM missing_table;";
            ExecuteResponse response = execute("mysql-8.0", sql, "");
            assertThat(response.status()).isEqualTo(ExecutionStatus.RUNTIME_ERROR);
            assertThat(response.stderr()).contains("ERROR 1064");
        }
    }

    @Nested
    @DisplayName("TypeScript 5.4 (Node.js 20)")
    class TypeScript54Tests {

        @Test
        @DisplayName("Executes TypeScript code with typed reduce")
        void executeValidTypeScript() {
            String code = """
                    const numbers: number[] = [1, 2, 3, 4, 5];
                    const sum: number = numbers.reduce((acc, curr) => acc + curr, 0);
                    console.log(`TS_SUM=${sum}`);
                    """;
            ExecuteResponse response = execute("typescript-5.4", code, "");
            assertThat(response.status()).isEqualTo(ExecutionStatus.SUCCESS);
            assertThat(response.stdout()).contains("TS_SUM=15");
            assertThat(response.exitCode()).isEqualTo(0L);
        }

        @Test
        @DisplayName("Runtime error captures exception stack trace")
        void runtimeError() {
            String code = """
                    throw new Error("Forced TypeScript exception");
                    """;
            ExecuteResponse response = execute("typescript-5.4", code, "");
            assertThat(response.status()).isEqualTo(ExecutionStatus.RUNTIME_ERROR);
            assertThat(response.stderr()).contains("Forced TypeScript exception");
        }

        @Test
        @DisplayName("Processes stdin via fs.readFileSync")
        void stdinProcessing() {
            String code = """
                    import * as fs from 'fs';
                    const input: string = fs.readFileSync(0, 'utf-8').trim();
                    console.log(`ECHO=${input}`);
                    """;
            ExecuteResponse response = execute("typescript-5.4", code, "TypeScript Rocks");
            assertThat(response.status()).isEqualTo(ExecutionStatus.SUCCESS);
            assertThat(response.stdout()).contains("ECHO=TypeScript Rocks");
        }
    }

    @Nested
    @DisplayName("Go 1.22")
    class Go122Tests {

        @Test
        @DisplayName("Executes valid Go program")
        void executeValidGo() {
            String code = """
                    package main
                    import "fmt"
                    func main() {
                        sum := 0
                        for i := 1; i <= 10; i++ {
                            sum += i
                        }
                        fmt.Printf("GO_SUM=%d\\n", sum)
                    }
                    """;
            ExecuteResponse response = execute("go-1.22", code, "");
            assertThat(response.status()).isEqualTo(ExecutionStatus.SUCCESS);
            assertThat(response.stdout()).contains("GO_SUM=55");
            assertThat(response.exitCode()).isEqualTo(0L);
        }

        @Test
        @DisplayName("Compilation error captures diagnostic compiler stderr")
        void compilationError() {
            String code = """
                    package main
                    func main() {
                        syntax error here
                    }
                    """;
            ExecuteResponse response = execute("go-1.22", code, "");
            assertThat(response.status()).isEqualTo(ExecutionStatus.COMPILATION_ERROR);
            assertThat(response.stderr()).contains("syntax error");
        }

        @Test
        @DisplayName("Runtime error captures panic trace")
        void runtimeError() {
            String code = """
                    package main
                    func main() {
                        panic("Forced Go panic")
                    }
                    """;
            ExecuteResponse response = execute("go-1.22", code, "");
            assertThat(response.status()).isEqualTo(ExecutionStatus.RUNTIME_ERROR);
            assertThat(response.stderr()).contains("panic: Forced Go panic");
        }

        @Test
        @DisplayName("Processes stdin via bufio.Scanner")
        void stdinProcessing() {
            String code = """
                    package main
                    import (
                        "bufio"
                        "fmt"
                        "os"
                    )
                    func main() {
                        scanner := bufio.NewScanner(os.Stdin)
                        if scanner.Scan() {
                            fmt.Printf("ECHO=%s\\n", scanner.Text())
                        }
                    }
                    """;
            ExecuteResponse response = execute("go-1.22", code, "Hello Go");
            assertThat(response.status()).isEqualTo(ExecutionStatus.SUCCESS);
            assertThat(response.stdout()).contains("ECHO=Hello Go");
        }
    }

    @Nested
    @DisplayName("Rust 1.75")
    class Rust175Tests {

        @Test
        @DisplayName("Executes valid Rust program")
        void executeValidRust() {
            String code = """
                    fn main() {
                        let sum: i32 = (1..=10).sum();
                        println!("RUST_SUM={}", sum);
                    }
                    """;
            ExecuteResponse response = execute("rust-1.75", code, "");
            assertThat(response.status()).isEqualTo(ExecutionStatus.SUCCESS);
            assertThat(response.stdout()).contains("RUST_SUM=55");
            assertThat(response.exitCode()).isEqualTo(0L);
        }

        @Test
        @DisplayName("Compilation error captures rustc diagnostics")
        void compilationError() {
            String code = """
                    fn main() {
                        invalid_code_statement;
                    }
                    """;
            ExecuteResponse response = execute("rust-1.75", code, "");
            assertThat(response.status()).isEqualTo(ExecutionStatus.COMPILATION_ERROR);
            assertThat(response.stderr()).contains("error[E");
        }

        @Test
        @DisplayName("Runtime panic captures backtrace/panic message")
        void runtimeError() {
            String code = """
                    fn main() {
                        panic!("Forced Rust panic");
                    }
                    """;
            ExecuteResponse response = execute("rust-1.75", code, "");
            assertThat(response.status()).isEqualTo(ExecutionStatus.RUNTIME_ERROR);
            assertThat(response.stderr()).contains("panicked at");
        }
    }

    @Nested
    @DisplayName("PHP 8.3")
    class Php83Tests {

        @Test
        @DisplayName("Executes valid PHP script")
        void executeValidPhp() {
            String code = """
                    <?php
                    $sum = array_sum(range(1, 10));
                    echo "PHP_SUM=" . $sum . "\\n";
                    """;
            ExecuteResponse response = execute("php-8.3", code, "");
            assertThat(response.status()).isEqualTo(ExecutionStatus.SUCCESS);
            assertThat(response.stdout()).contains("PHP_SUM=55");
            assertThat(response.exitCode()).isEqualTo(0L);
        }

        @Test
        @DisplayName("Runtime error captures exception message")
        void runtimeError() {
            String code = """
                    <?php
                    throw new Exception("Forced PHP Exception");
                    """;
            ExecuteResponse response = execute("php-8.3", code, "");
            assertThat(response.status()).isEqualTo(ExecutionStatus.RUNTIME_ERROR);
            assertThat(response.stderr()).contains("Fatal error", "Forced PHP Exception");
        }

        @Test
        @DisplayName("Processes stdin via STDIN")
        void stdinProcessing() {
            String code = """
                    <?php
                    $input = trim(fgets(STDIN));
                    echo "ECHO=" . $input . "\\n";
                    """;
            ExecuteResponse response = execute("php-8.3", code, "PHP Input");
            assertThat(response.status()).isEqualTo(ExecutionStatus.SUCCESS);
            assertThat(response.stdout()).contains("ECHO=PHP Input");
        }
    }

    @Nested
    @DisplayName("C# 12 (.NET 8.0)")
    class Csharp12Tests {

        @Test
        @DisplayName("Executes valid C# program")
        void executeValidCsharp() {
            String code = """
                    using System;
                    using System.Linq;

                    class Program {
                        static void Main() {
                            int sum = Enumerable.Range(1, 10).Sum();
                            Console.WriteLine($"CSHARP_SUM={sum}");
                        }
                    }
                    """;
            ExecuteResponse response = execute("csharp-12", code, "");
            assertThat(response.status()).isEqualTo(ExecutionStatus.SUCCESS);
            assertThat(response.stdout()).contains("CSHARP_SUM=55");
            assertThat(response.exitCode()).isEqualTo(0L);
        }

        @Test
        @DisplayName("Compilation error captures diagnostic build failure")
        void compilationError() {
            String code = """
                    using System;
                    class Program {
                        static void Main() {
                            broken_syntax_without_semicolon
                        }
                    }
                    """;
            ExecuteResponse response = execute("csharp-12", code, "");
            assertThat(response.status()).isEqualTo(ExecutionStatus.COMPILATION_ERROR);
            assertThat(response.exitCode()).isNotEqualTo(0L);
        }

        @Test
        @DisplayName("Runtime error captures unhandled exception")
        void runtimeError() {
            String code = """
                    using System;
                    class Program {
                        static void Main() {
                            throw new InvalidOperationException("Forced C# Exception");
                        }
                    }
                    """;
            ExecuteResponse response = execute("csharp-12", code, "");
            assertThat(response.status()).isEqualTo(ExecutionStatus.RUNTIME_ERROR);
            assertThat(response.stderr()).contains("Unhandled exception", "InvalidOperationException");
        }
    }

    @Nested
    @DisplayName("Kotlin 1.9")
    class Kotlin19Tests {

        @Test
        @DisplayName("Executes valid Kotlin program")
        void executeValidKotlin() {
            String code = """
                    fun main() {
                        val sum = (1..10).sum()
                        println("KOTLIN_SUM=$sum")
                    }
                    """;
            ExecuteResponse response = execute("kotlin-1.9", code, "");
            assertThat(response.status()).isEqualTo(ExecutionStatus.SUCCESS);
            assertThat(response.stdout()).contains("KOTLIN_SUM=55");
            assertThat(response.exitCode()).isEqualTo(0L);
        }

        @Test
        @DisplayName("Compilation error captures kotlinc error message")
        void compilationError() {
            String code = """
                    fun main() {
                        broken syntax error here
                    }
                    """;
            ExecuteResponse response = execute("kotlin-1.9", code, "");
            assertThat(response.status()).isEqualTo(ExecutionStatus.COMPILATION_ERROR);
            assertThat(response.stderr()).contains("error:");
        }

        @Test
        @DisplayName("Runtime error captures exception stack trace")
        void runtimeError() {
            String code = """
                    fun main() {
                        throw IllegalStateException("Forced Kotlin Exception")
                    }
                    """;
            ExecuteResponse response = execute("kotlin-1.9", code, "");
            assertThat(response.status()).isEqualTo(ExecutionStatus.RUNTIME_ERROR);
            assertThat(response.stderr()).contains("IllegalStateException", "Forced Kotlin Exception");
        }
    }

    @Nested
    @DisplayName("Dart 3.4")
    class Dart34Tests {

        @Test
        @DisplayName("Executes valid Dart script")
        void executeValidDart() {
            String code = """
                    void main() {
                      int sum = [1, 2, 3, 4, 5, 6, 7, 8, 9, 10].reduce((a, b) => a + b);
                      print('DART_SUM=$sum');
                    }
                    """;
            ExecuteResponse response = execute("dart-3.4", code, "");
            assertThat(response.status()).isEqualTo(ExecutionStatus.SUCCESS);
            assertThat(response.stdout()).contains("DART_SUM=55");
            assertThat(response.exitCode()).isEqualTo(0L);
        }

        @Test
        @DisplayName("Runtime error captures unhandled exception")
        void runtimeError() {
            String code = """
                    void main() {
                      throw Exception('Forced Dart Exception');
                    }
                    """;
            ExecuteResponse response = execute("dart-3.4", code, "");
            assertThat(response.status()).isEqualTo(ExecutionStatus.RUNTIME_ERROR);
            assertThat(response.stderr()).contains("Unhandled exception", "Forced Dart Exception");
        }

        @Test
        @DisplayName("Processes stdin via stdin.readLineSync")
        void stdinProcessing() {
            String code = """
                    import 'dart:io';
                    void main() {
                      String? line = stdin.readLineSync();
                      print('ECHO=$line');
                    }
                    """;
            ExecuteResponse response = execute("dart-3.4", code, "Dart Input");
            assertThat(response.status()).isEqualTo(ExecutionStatus.SUCCESS);
            assertThat(response.stdout()).contains("ECHO=Dart Input");
        }
    }

    @Nested
    @DisplayName("General Resource and Boundary Tests")
    class BoundaryAndResourceTests {

        @Test
        @DisplayName("Timeout: infinite loop triggers TIME_LIMIT_EXCEEDED within bounded limit")
        void timeoutHandling() {
            String infiniteLoop = """
                    while True:
                        pass
                    """;
            ExecuteRequest request = new ExecuteRequest(
                    "python-3.12",
                    infiniteLoop,
                    "",
                    new CompilerOptionsDto(600L, 131072L)
            );
            long start = System.currentTimeMillis();
            ExecuteResponse response = compilerService.execute(request);
            long elapsed = System.currentTimeMillis() - start;

            assertThat(response.status()).isEqualTo(ExecutionStatus.TIME_LIMIT_EXCEEDED);
            // Must finish reasonably close to the timeout, never hang
            assertThat(elapsed).isLessThan(5000L);
        }

        @Test
        @DisplayName("Large output: handles high volume output cleanly up to max output boundary")
        void largeOutputHandling() {
            String code = """
                    for i in range(5000):
                        print("LINE-" + str(i) + "-DATA-CHUNK-FOR-BUFFER-FILL")
                    """;
            ExecuteResponse response = execute("python-3.12", code, "");
            assertThat(response.status()).isEqualTo(ExecutionStatus.SUCCESS);
            assertThat(response.stdout()).isNotNull();
            assertThat(response.stdout()).contains("LINE-0-DATA-CHUNK", "LINE-4999-DATA-CHUNK");
            assertThat(response.stdout().length()).isGreaterThan(50000);
        }
    }

    private ExecuteResponse execute(String language, String code, String stdin) {
        ExecuteRequest request = new ExecuteRequest(
                language,
                code,
                stdin,
                new CompilerOptionsDto(10000L, 524288L)
        );
        return compilerService.execute(request);
    }
}
