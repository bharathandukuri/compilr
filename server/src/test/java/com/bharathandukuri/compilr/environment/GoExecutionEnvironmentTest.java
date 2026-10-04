package com.bharathandukuri.compilr.environment;

import com.bharathandukuri.compilr.compiler.dto.ExecuteResponse;
import com.bharathandukuri.compilr.compiler.enums.ExecutionStatus;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("Go 1.22 Execution Environment Tests")
class GoExecutionEnvironmentTest extends BaseEnvironmentTest {

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
