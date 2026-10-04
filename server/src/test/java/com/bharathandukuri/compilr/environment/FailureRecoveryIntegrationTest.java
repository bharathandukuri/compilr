package com.bharathandukuri.compilr.environment;

import com.bharathandukuri.compilr.compiler.dto.CompilerOptionsDto;
import com.bharathandukuri.compilr.compiler.dto.ExecuteRequest;
import com.bharathandukuri.compilr.compiler.dto.ExecuteResponse;
import com.bharathandukuri.compilr.compiler.enums.ExecutionStatus;
import com.bharathandukuri.compilr.compiler.exception.UnsupportedLanguageException;
import com.bharathandukuri.compilr.compiler.service.CompilerService;
import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.api.model.Container;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("Failure Recovery and Resource Leakage Integration Tests")
class FailureRecoveryIntegrationTest extends BaseEnvironmentTest {

    @Autowired
    private CompilerService compilerService;

    @Autowired
    private DockerClient dockerClient;

    @Test
    @DisplayName("Unsupported language throws UnsupportedLanguageException")
    void unsupportedLanguageHandling() {
        ExecuteRequest request = new ExecuteRequest(
                "fortran-90",
                "PROGRAM HELLO; END",
                "",
                null
        );

        assertThatThrownBy(() -> compilerService.execute(request))
                .isInstanceOf(UnsupportedLanguageException.class)
                .hasMessageContaining("Unsupported language 'fortran-90'");
    }

    @Test
    @DisplayName("Blank source code handles gracefully without container crash")
    void blankSourceCodeHandling() {
        ExecuteRequest request = new ExecuteRequest(
                "python-3.12",
                "",
                "",
                null
        );

        ExecuteResponse response = compilerService.execute(request);

        assertThat(response).isNotNull();
        assertThat(response.status()).isEqualTo(ExecutionStatus.SUCCESS);
        assertThat(response.stdout()).isEmpty();
        assertThat(response.exitCode()).isEqualTo(0L);
    }

    @Test
    @DisplayName("Ephemeral Container Leakage: zero orphaned compilr containers remain after executions")
    void verifyZeroOrphanedContainers() {
        // Execute three runs across different languages
        compilerService.execute(new ExecuteRequest("python-3.12", "print(1)", "", null));
        compilerService.execute(new ExecuteRequest("c-17", "int main(){return 0;}", "", null));
        compilerService.execute(new ExecuteRequest("postgresql-16", "SELECT 1;", "", null));

        // Inspect docker container list (including stopped ones)
        List<Container> allContainers = dockerClient.listContainersCmd()
                .withShowAll(true)
                .exec();

        // No container named "compilr-execution-" should remain
        List<String> leakedContainers = allContainers.stream()
                .flatMap(c -> java.util.Arrays.stream(c.getNames()))
                .filter(name -> name.contains("compilr-execution"))
                .toList();

        assertThat(leakedContainers).isEmpty();
    }
}
