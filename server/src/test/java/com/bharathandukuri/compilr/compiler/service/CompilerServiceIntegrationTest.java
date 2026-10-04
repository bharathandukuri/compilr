package com.bharathandukuri.compilr.compiler.service;

import com.bharathandukuri.compilr.compiler.dto.CompilerOptionsDto;
import com.bharathandukuri.compilr.compiler.dto.ExecuteRequest;
import com.bharathandukuri.compilr.compiler.dto.ExecuteResponse;
import com.bharathandukuri.compilr.compiler.enums.ExecutionStatus;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.concurrent.CompletableFuture;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class CompilerServiceIntegrationTest {

    @Autowired
    private CompilerService compilerService;

    @Test
    @DisplayName("Python 3.12: executes simple print with stdin")
    void execute_pythonWithStdin() {
        ExecuteRequest request = new ExecuteRequest(
                "python-3.12",
                "import sys\nname = sys.stdin.read().strip()\nprint(f'Hello, {name}!')",
                "Compilr",
                new CompilerOptionsDto(4000L, 131072L)
        );

        ExecuteResponse response = compilerService.execute(request);

        assertThat(response).isNotNull();
        assertThat(response.status()).isEqualTo(ExecutionStatus.SUCCESS);
        assertThat(response.stdout()).contains("Hello, Compilr!");
        assertThat(response.exitCode()).isEqualTo(0L);
    }

    @Test
    @DisplayName("Java 21: compiles and executes Main class")
    void execute_javaSuccess() {
        String javaCode = """
                public class Main {
                    public static void main(String[] args) {
                        int a = 15;
                        int b = 27;
                        System.out.println("SUM=" + (a + b));
                    }
                }
                """;

        ExecuteRequest request = new ExecuteRequest(
                "java-21",
                javaCode,
                "",
                new CompilerOptionsDto(6000L, 262144L)
        );

        ExecuteResponse response = compilerService.execute(request);

        assertThat(response).isNotNull();
        assertThat(response.status()).isEqualTo(ExecutionStatus.SUCCESS);
        assertThat(response.stdout()).contains("SUM=42");
        assertThat(response.exitCode()).isEqualTo(0L);
    }

    @Test
    @DisplayName("C++ 23: compiles and executes correctly")
    void execute_cppSuccess() {
        String cppCode = """
                #include <iostream>

                int main() {
                    std::cout << "CPP_RUNNING" << std::endl;
                    return 0;
                }
                """;

        ExecuteRequest request = new ExecuteRequest(
                "cpp-23",
                cppCode,
                "",
                new CompilerOptionsDto(5000L, 131072L)
        );

        ExecuteResponse response = compilerService.execute(request);

        assertThat(response).isNotNull();
        assertThat(response.status()).isEqualTo(ExecutionStatus.SUCCESS);
        assertThat(response.stdout()).contains("CPP_RUNNING");
        assertThat(response.exitCode()).isEqualTo(0L);
    }

    @Test
    @DisplayName("C++ 23: compilation error captures diagnostic compiler stderr")
    void execute_cppCompilationError() {
        String brokenCpp = """
                #include <iostream>
                int main() {
                    syntax_error_here();
                }
                """;

        ExecuteRequest request = new ExecuteRequest(
                "cpp-23",
                brokenCpp,
                "",
                new CompilerOptionsDto(4000L, 131072L)
        );

        ExecuteResponse response = compilerService.execute(request);

        assertThat(response).isNotNull();
        assertThat(response.status()).isEqualTo(ExecutionStatus.COMPILATION_ERROR);
        assertThat(response.stderr()).contains("syntax_error_here");
        assertThat(response.exitCode()).isNotEqualTo(0L);
    }

    @Test
    @DisplayName("Python 3.12: runtime exception returns RUNTIME_ERROR")
    void execute_pythonRuntimeError() {
        String brokenCode = """
                x = 1 / 0
                """;

        ExecuteRequest request = new ExecuteRequest(
                "python-3.12",
                brokenCode,
                "",
                new CompilerOptionsDto(3000L, 131072L)
        );

        ExecuteResponse response = compilerService.execute(request);

        assertThat(response).isNotNull();
        assertThat(response.status()).isEqualTo(ExecutionStatus.RUNTIME_ERROR);
        assertThat(response.stderr()).contains("ZeroDivisionError");
        assertThat(response.exitCode()).isNotEqualTo(0L);
    }

    @Test
    @DisplayName("Python 3.12: infinite loop triggers TIME_LIMIT_EXCEEDED")
    void execute_pythonTimeout() {
        String infiniteLoop = """
                while True:
                    pass
                """;

        ExecuteRequest request = new ExecuteRequest(
                "python-3.12",
                infiniteLoop,
                "",
                new CompilerOptionsDto(500L, 131072L)
        );

        ExecuteResponse response = compilerService.execute(request);

        assertThat(response).isNotNull();
        assertThat(response.status()).isEqualTo(ExecutionStatus.TIME_LIMIT_EXCEEDED);
    }

    @Test
    @DisplayName("Concurrency: parallel execution requests succeed independently")
    void execute_concurrentExecutions() {
        ExecuteRequest req1 = new ExecuteRequest("python-3.12", "print('EXEC_1')", "", null);
        ExecuteRequest req2 = new ExecuteRequest("python-3.12", "print('EXEC_2')", "", null);

        CompletableFuture<ExecuteResponse> f1 = CompletableFuture.supplyAsync(() -> compilerService.execute(req1));
        CompletableFuture<ExecuteResponse> f2 = CompletableFuture.supplyAsync(() -> compilerService.execute(req2));

        ExecuteResponse r1 = f1.join();
        ExecuteResponse r2 = f2.join();

        assertThat(r1.status()).isEqualTo(ExecutionStatus.SUCCESS);
        assertThat(r1.stdout()).contains("EXEC_1");

        assertThat(r2.status()).isEqualTo(ExecutionStatus.SUCCESS);
        assertThat(r2.stdout()).contains("EXEC_2");

        assertThat(r1.executionId()).isNotEqualTo(r2.executionId());
    }
}
