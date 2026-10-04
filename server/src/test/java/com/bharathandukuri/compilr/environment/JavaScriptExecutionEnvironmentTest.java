package com.bharathandukuri.compilr.environment;

import com.bharathandukuri.compilr.compiler.dto.ExecuteResponse;
import com.bharathandukuri.compilr.compiler.enums.ExecutionStatus;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("JavaScript (Node.js 20) Execution Environment Tests")
class JavaScriptExecutionEnvironmentTest extends BaseEnvironmentTest {

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
