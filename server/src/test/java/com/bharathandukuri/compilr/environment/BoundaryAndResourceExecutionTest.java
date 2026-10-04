package com.bharathandukuri.compilr.environment;

import com.bharathandukuri.compilr.compiler.dto.CompilerOptionsDto;
import com.bharathandukuri.compilr.compiler.dto.ExecuteRequest;
import com.bharathandukuri.compilr.compiler.dto.ExecuteResponse;
import com.bharathandukuri.compilr.compiler.enums.ExecutionStatus;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("Boundary and Resource Constraint Tests")
class BoundaryAndResourceExecutionTest extends BaseEnvironmentTest {

    @Test
    @DisplayName("Timeout: infinite loop triggers TIME_LIMIT_EXCEEDED within bounded limit")
    void timeoutHandling() {
        String infiniteLoop = """
                while True:
                    pass
                """;
        ExecuteRequest request = new ExecuteRequest(
                "python-3.12",
                infiniteLoop,
                "",
                new CompilerOptionsDto(600L, 131072L)
        );
        long start = System.currentTimeMillis();
        ExecuteResponse response = compilerService.execute(request);
        long elapsed = System.currentTimeMillis() - start;

        assertThat(response.status()).isEqualTo(ExecutionStatus.TIME_LIMIT_EXCEEDED);
        assertThat(elapsed).isLessThan(5000L);
    }

    @Test
    @DisplayName("Large output: handles high volume output cleanly up to max output boundary")
    void largeOutputHandling() {
        String code = """
                for i in range(5000):
                    print("LINE-" + str(i) + "-DATA-CHUNK-FOR-BUFFER-FILL")
                """;
        ExecuteResponse response = execute("python-3.12", code, "");
        assertThat(response.status()).isEqualTo(ExecutionStatus.SUCCESS);
        assertThat(response.stdout()).isNotNull();
        assertThat(response.stdout()).contains("LINE-0-DATA-CHUNK", "LINE-4999-DATA-CHUNK");
        assertThat(response.stdout().length()).isGreaterThan(50000);
    }
}
