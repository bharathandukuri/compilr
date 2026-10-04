package com.bharathandukuri.compilr.environment;

import com.bharathandukuri.compilr.compiler.dto.ExecuteResponse;
import com.bharathandukuri.compilr.compiler.enums.ExecutionStatus;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("C++ 23 (GCC 14) Execution Environment Tests")
class CppExecutionEnvironmentTest extends BaseEnvironmentTest {

    @Test
    @DisplayName("Verifies C++ standard version is C++23")
    void verifyCppVersion() {
        String code = """
                #include <iostream>
                int main() {
                    std::cout << "CPP_VERSION=" << __cplusplus << std::endl;
                    return 0;
                }
                """;
        ExecuteResponse response = execute("cpp-23", code, "");
        assertThat(response.status()).isEqualTo(ExecutionStatus.SUCCESS);
        assertThat(response.stdout()).containsAnyOf("CPP_VERSION=202100", "CPP_VERSION=202302");
    }

    @Test
    @DisplayName("Executes C++23 modern features (vector, ranges, lambdas)")
    void executeValidCpp() {
        String code = """
                #include <iostream>
                #include <vector>
                #include <numeric>
                int main() {
                    std::vector<int> numbers = {1, 2, 3, 4, 5};
                    int sum = std::accumulate(numbers.begin(), numbers.end(), 0);
                    std::cout << "ACCUMULATE=" << sum << std::endl;
                    return 0;
                }
                """;
        ExecuteResponse response = execute("cpp-23", code, "");
        assertThat(response.status()).isEqualTo(ExecutionStatus.SUCCESS);
        assertThat(response.stdout()).contains("ACCUMULATE=15");
    }

    @Test
    @DisplayName("Captures g++ compilation error")
    void compilationError() {
        String code = """
                #include <iostream>
                int main() {
                    std::cout << missing_variable << std::endl;
                    return 0;
                }
                """;
        ExecuteResponse response = execute("cpp-23", code, "");
        assertThat(response.status()).isEqualTo(ExecutionStatus.COMPILATION_ERROR);
        assertThat(response.stderr()).contains("error:", "missing_variable");
    }

    @Test
    @DisplayName("Handles non-zero exit code in C++ as RUNTIME_ERROR")
    void runtimeError() {
        String code = """
                #include <cstdlib>
                int main() {
                    std::exit(42);
                }
                """;
        ExecuteResponse response = execute("cpp-23", code, "");
        assertThat(response.status()).isEqualTo(ExecutionStatus.RUNTIME_ERROR);
        assertThat(response.exitCode()).isEqualTo(42L);
    }

    @Test
    @DisplayName("Reads standard input via std::cin")
    void stdinProcessing() {
        String code = """
                #include <iostream>
                #include <string>
                int main() {
                    std::string name;
                    if (std::cin >> name) {
                        std::cout << "GREETING: Welcome, " << name << "!" << std::endl;
                    }
                    return 0;
                }
                """;
        ExecuteResponse response = execute("cpp-23", code, "Engineer");
        assertThat(response.status()).isEqualTo(ExecutionStatus.SUCCESS);
        assertThat(response.stdout()).contains("GREETING: Welcome, Engineer!");
    }
}
