package com.bharathandukuri.compilr.environment;

import com.bharathandukuri.compilr.compiler.dto.ExecuteResponse;
import com.bharathandukuri.compilr.compiler.enums.ExecutionStatus;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("Rust 1.75 Execution Environment Tests")
class RustExecutionEnvironmentTest extends BaseEnvironmentTest {

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
