package com.bharathandukuri.compilr.environment;

import com.bharathandukuri.compilr.compiler.dto.ExecuteResponse;
import com.bharathandukuri.compilr.compiler.enums.ExecutionStatus;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("C 17 (GCC) Execution Environment Tests")
class CExecutionEnvironmentTest extends BaseEnvironmentTest {

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
