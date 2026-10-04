package com.bharathandukuri.compilr.environment;

import com.bharathandukuri.compilr.compiler.dto.ExecuteResponse;
import com.bharathandukuri.compilr.compiler.enums.ExecutionStatus;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("Kotlin 1.9 Execution Environment Tests")
class KotlinExecutionEnvironmentTest extends BaseEnvironmentTest {

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
