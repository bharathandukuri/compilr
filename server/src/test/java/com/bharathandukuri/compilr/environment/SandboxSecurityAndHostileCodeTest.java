package com.bharathandukuri.compilr.environment;

import com.bharathandukuri.compilr.compiler.dto.CompilerOptionsDto;
import com.bharathandukuri.compilr.compiler.dto.ExecuteRequest;
import com.bharathandukuri.compilr.compiler.dto.ExecuteResponse;
import com.bharathandukuri.compilr.compiler.enums.ExecutionStatus;
import com.bharathandukuri.compilr.compiler.service.CompilerService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("Sandbox Security and Hostile Code Containment Tests")
class SandboxSecurityAndHostileCodeTest extends BaseEnvironmentTest {

    @Autowired
    private CompilerService compilerService;

    @Test
    @DisplayName("Hostile: Attempt to read /etc/shadow or /root fails with permission denied")
    void forbiddenFilesystemRead() {
        String hostilePython = """
                import os
                paths = ["/etc/shadow", "/root/.bashrc", "/proc/1/environ"]
                results = []
                for p in paths:
                    try:
                        with open(p, "r") as f:
                            results.append(p + ":READ_SUCCESS")
                    except Exception as e:
                        results.append(p + ":BLOCKED_" + type(e).__name__)
                print(",".join(results))
                """;

        ExecuteRequest request = new ExecuteRequest(
                "python-3.12",
                hostilePython,
                "",
                new CompilerOptionsDto(4000L, 131072L)
        );

        ExecuteResponse response = compilerService.execute(request);

        assertThat(response.status()).isEqualTo(ExecutionStatus.SUCCESS);
        assertThat(response.stdout()).doesNotContain("READ_SUCCESS");
        assertThat(response.stdout()).contains("BLOCKED_");
    }

    @Test
    @DisplayName("Hostile: Attempt to write to system directories /etc or /bin fails")
    void forbiddenFilesystemWrite() {
        String hostilePython = """
                import os
                try:
                    with open("/etc/pwned.txt", "w") as f:
                        f.write("hacked")
                    print("WRITE_SUCCESS")
                except Exception as e:
                    print("WRITE_FAILED:" + type(e).__name__)
                """;

        ExecuteRequest request = new ExecuteRequest(
                "python-3.12",
                hostilePython,
                "",
                new CompilerOptionsDto(4000L, 131072L)
        );

        ExecuteResponse response = compilerService.execute(request);

        assertThat(response.status()).isEqualTo(ExecutionStatus.SUCCESS);
        assertThat(response.stdout()).doesNotContain("WRITE_SUCCESS");
        assertThat(response.stdout()).contains("WRITE_FAILED");
    }

    @Test
    @DisplayName("Hostile: Fork bomb is contained by sandbox process limit and terminated safely")
    void forkBombContainment() {
        // C program attempting rapid uncontrolled fork
        String forkBombC = """
                #include <unistd.h>
                #include <stdio.h>
                int main() {
                    for (int i = 0; i < 200; i++) {
                        fork();
                    }
                    printf("FORK_FINISHED\\n");
                    return 0;
                }
                """;

        ExecuteRequest request = new ExecuteRequest(
                "c-17",
                forkBombC,
                "",
                new CompilerOptionsDto(1500L, 65536L)
        );

        long start = System.currentTimeMillis();
        ExecuteResponse response = compilerService.execute(request);
        long elapsed = System.currentTimeMillis() - start;

        // The fork bomb must either complete within bounds, be killed by runtime error/signal, or time out
        assertThat(response.status()).isIn(
                ExecutionStatus.SUCCESS,
                ExecutionStatus.RUNTIME_ERROR,
                ExecutionStatus.TIME_LIMIT_EXCEEDED
        );
        // Crucial guarantee: Host must not hang or freeze
        assertThat(elapsed).isLessThan(8000L);
    }

    @Test
    @DisplayName("Hostile: Outbound network connection attempt is blocked or unreachable")
    void networkIsolation() {
        String socketProbePython = """
                import socket
                try:
                    s = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
                    s.settimeout(1.0)
                    s.connect(("8.8.8.8", 80))
                    print("NETWORK_CONNECTED")
                    s.close()
                except Exception as e:
                    print("NETWORK_FAILED:" + type(e).__name__)
                """;

        ExecuteRequest request = new ExecuteRequest(
                "python-3.12",
                socketProbePython,
                "",
                new CompilerOptionsDto(3000L, 131072L)
        );

        ExecuteResponse response = compilerService.execute(request);

        assertThat(response.status()).isEqualTo(ExecutionStatus.SUCCESS);
        assertThat(response.stdout()).doesNotContain("NETWORK_CONNECTED");
        assertThat(response.stdout()).contains("NETWORK_FAILED");
    }

    @Test
    @DisplayName("Hostile: Infinite CPU consumption is strictly contained by time limits")
    void infiniteCpuContainment() {
        String infiniteC = """
                int main() {
                    volatile unsigned long long x = 0;
                    while(1) {
                        x++;
                    }
                    return 0;
                }
                """;

        ExecuteRequest request = new ExecuteRequest(
                "c-17",
                infiniteC,
                "",
                new CompilerOptionsDto(600L, 65536L)
        );

        long start = System.currentTimeMillis();
        ExecuteResponse response = compilerService.execute(request);
        long elapsed = System.currentTimeMillis() - start;

        assertThat(response.status()).isEqualTo(ExecutionStatus.TIME_LIMIT_EXCEEDED);
        assertThat(elapsed).isLessThan(5000L);
    }
}
