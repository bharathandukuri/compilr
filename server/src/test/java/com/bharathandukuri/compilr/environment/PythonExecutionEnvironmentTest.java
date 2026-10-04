package com.bharathandukuri.compilr.environment;

import com.bharathandukuri.compilr.compiler.dto.ExecuteResponse;
import com.bharathandukuri.compilr.compiler.enums.ExecutionStatus;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("Python 3.12 (CPython 3.12) Execution Environment Tests")
class PythonExecutionEnvironmentTest extends BaseEnvironmentTest {

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
