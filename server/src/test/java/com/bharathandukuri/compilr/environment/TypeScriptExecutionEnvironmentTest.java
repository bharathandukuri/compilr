package com.bharathandukuri.compilr.environment;

import com.bharathandukuri.compilr.compiler.dto.ExecuteResponse;
import com.bharathandukuri.compilr.compiler.enums.ExecutionStatus;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("TypeScript 5.4 (Node.js 20) Execution Environment Tests")
class TypeScriptExecutionEnvironmentTest extends BaseEnvironmentTest {

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
