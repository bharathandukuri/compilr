package com.bharathandukuri.compilr.environment;

import com.bharathandukuri.compilr.compiler.dto.ExecuteResponse;
import com.bharathandukuri.compilr.compiler.enums.ExecutionStatus;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("Java 21 (OpenJDK 21) Execution Environment Tests")
class JavaExecutionEnvironmentTest extends BaseEnvironmentTest {

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
