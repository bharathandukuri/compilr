package com.bharathandukuri.compilr.compiler.service;

import com.bharathandukuri.compilr.compiler.configuration.CompilerProperties;
import com.bharathandukuri.compilr.compiler.dto.CompilerOptionsDto;
import com.bharathandukuri.compilr.compiler.dto.ExecuteRequest;
import com.bharathandukuri.compilr.compiler.dto.ExecuteResponse;
import com.bharathandukuri.compilr.compiler.dto.LanguageResponse;
import com.bharathandukuri.compilr.compiler.enums.ExecutionStatus;
import com.bharathandukuri.compilr.compiler.exception.InvalidCompilerRequestException;
import com.bharathandukuri.compilr.compiler.exception.UnsupportedLanguageException;
import com.bharathandukuri.compilr.compiler.service.impl.CompilerServiceImpl;
import com.bharathandukuri.compilr.execution.dto.request.SimpleCodeExecutionRequest;
import com.bharathandukuri.compilr.execution.dto.response.SimpleCodeExecutionResult;
import com.bharathandukuri.compilr.execution.enums.CodeExecutionStatus;
import com.bharathandukuri.compilr.execution.service.CodeExecutionService;
import com.bharathandukuri.compilr.language.CompiledLanguage;
import com.bharathandukuri.compilr.language.Language;
import com.bharathandukuri.compilr.language.LanguageType;
import com.bharathandukuri.compilr.language.exception.LanguageNotFoundException;
import com.bharathandukuri.compilr.language.registry.LanguageRegistry;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CompilerServiceTest {

    @Mock
    private CodeExecutionService codeExecutionService;

    @Mock
    private LanguageRegistry languageRegistry;

    @Mock
    private CompiledLanguage mockLanguage;

    private CompilerProperties properties;
    private CompilerServiceImpl compilerService;

    @BeforeEach
    void setUp() {
        properties = new CompilerProperties();
        properties.setMaxSourceSizeBytes(1000);
        properties.setMaxStdinSizeBytes(500);
        properties.setMaxOutputSizeBytes(200);
        properties.setDefaultTimeLimitMs(3000L);
        properties.setMinTimeLimitMs(100L);
        properties.setMaxTimeLimitMs(10000L);
        properties.setDefaultMemoryLimitKb(131072L);

        compilerService = new CompilerServiceImpl(codeExecutionService, languageRegistry, properties);
    }

    @Test
    @DisplayName("execute: successfully executes code and maps metrics")
    void execute_successful() {
        when(languageRegistry.get("java-21")).thenReturn(mockLanguage);
        when(mockLanguage.id()).thenReturn("java-21");
        when(mockLanguage.name()).thenReturn("Java (OpenJDK 21)");

        SimpleCodeExecutionResult execResult = SimpleCodeExecutionResult.builder()
                .stdout("Hello World!\n")
                .stderr("")
                .exitCode(0L)
                .exitSignal(0L)
                .executionStatus(CodeExecutionStatus.SUCCESS)
                .executionTimeMs(150L)
                .memoryUsageKb(16384L)
                .logs(List.of("Execution ok"))
                .build();

        when(codeExecutionService.run(any(SimpleCodeExecutionRequest.class))).thenReturn(execResult);

        ExecuteRequest request = new ExecuteRequest(
                "java-21",
                "public class Solution { public static void main(String[] args) {} }",
                "test input",
                new CompilerOptionsDto(4000L, 262144L)
        );

        ExecuteResponse response = compilerService.execute(request);

        assertThat(response).isNotNull();
        assertThat(response.status()).isEqualTo(ExecutionStatus.SUCCESS);
        assertThat(response.stdout()).isEqualTo("Hello World!\n");
        assertThat(response.stderr()).isEmpty();
        assertThat(response.exitCode()).isEqualTo(0L);
        assertThat(response.executionTimeMs()).isEqualTo(150L);
        assertThat(response.memoryUsageKb()).isEqualTo(16384L);
        assertThat(response.executionId()).isNotBlank();

        ArgumentCaptor<SimpleCodeExecutionRequest> captor = ArgumentCaptor.forClass(SimpleCodeExecutionRequest.class);
        verify(codeExecutionService).run(captor.capture());
        SimpleCodeExecutionRequest captured = captor.getValue();
        assertThat(captured.getConstraints().timeLimitMs()).isEqualTo(4000L);
        assertThat(captured.getConstraints().memoryLimitKb()).isEqualTo(262144L);
    }

    @Test
    @DisplayName("execute: throws InvalidCompilerRequestException when request is null")
    void execute_nullRequest() {
        assertThatThrownBy(() -> compilerService.execute(null))
                .isInstanceOf(InvalidCompilerRequestException.class)
                .hasMessageContaining("must not be null");
    }

    @Test
    @DisplayName("execute: throws InvalidCompilerRequestException when language is blank")
    void execute_blankLanguage() {
        ExecuteRequest request = new ExecuteRequest("   ", "code", "in", null);
        assertThatThrownBy(() -> compilerService.execute(request))
                .isInstanceOf(InvalidCompilerRequestException.class)
                .hasMessageContaining("must not be null or blank");
    }

    @Test
    @DisplayName("execute: throws UnsupportedLanguageException when language is not registered")
    void execute_unsupportedLanguage() {
        when(languageRegistry.get("fortran")).thenThrow(new LanguageNotFoundException("fortran"));

        ExecuteRequest request = new ExecuteRequest("fortran", "code", "in", null);
        assertThatThrownBy(() -> compilerService.execute(request))
                .isInstanceOf(UnsupportedLanguageException.class)
                .hasMessageContaining("Unsupported language 'fortran'");
    }

    @Test
    @DisplayName("execute: throws InvalidCompilerRequestException when source code exceeds size limit")
    void execute_oversizedSourceCode() {
        when(languageRegistry.get("python-3.12")).thenReturn(mockLanguage);
        when(mockLanguage.id()).thenReturn("python-3.12");
        when(mockLanguage.name()).thenReturn("Python");

        String hugeCode = "a".repeat(1500); // limit is 1000
        ExecuteRequest request = new ExecuteRequest("python-3.12", hugeCode, "in", null);

        assertThatThrownBy(() -> compilerService.execute(request))
                .isInstanceOf(InvalidCompilerRequestException.class)
                .hasMessageContaining("Source code size");
    }

    @Test
    @DisplayName("execute: throws InvalidCompilerRequestException when stdin exceeds size limit")
    void execute_oversizedStdin() {
        when(languageRegistry.get("python-3.12")).thenReturn(mockLanguage);
        when(mockLanguage.id()).thenReturn("python-3.12");
        when(mockLanguage.name()).thenReturn("Python");

        String hugeStdin = "x".repeat(600); // limit is 500
        ExecuteRequest request = new ExecuteRequest("python-3.12", "print(1)", hugeStdin, null);

        assertThatThrownBy(() -> compilerService.execute(request))
                .isInstanceOf(InvalidCompilerRequestException.class)
                .hasMessageContaining("Standard input size");
    }

    @Test
    @DisplayName("execute: clamps time limit between min and max bounds")
    void execute_clampsTimeLimit() {
        when(languageRegistry.get("cpp-23")).thenReturn(mockLanguage);
        when(mockLanguage.id()).thenReturn("cpp-23");
        when(mockLanguage.name()).thenReturn("C++");

        SimpleCodeExecutionResult execResult = SimpleCodeExecutionResult.builder()
                .stdout("ok")
                .executionStatus(CodeExecutionStatus.SUCCESS)
                .build();
        when(codeExecutionService.run(any())).thenReturn(execResult);

        // Requested 999999ms, should be clamped to max 10000ms
        ExecuteRequest request = new ExecuteRequest(
                "cpp-23",
                "int main() {}",
                "",
                new CompilerOptionsDto(999999L, null)
        );

        compilerService.execute(request);

        ArgumentCaptor<SimpleCodeExecutionRequest> captor = ArgumentCaptor.forClass(SimpleCodeExecutionRequest.class);
        verify(codeExecutionService).run(captor.capture());
        assertThat(captor.getValue().getConstraints().timeLimitMs()).isEqualTo(10000L);
    }

    @Test
    @DisplayName("execute: truncates output exceeding maxOutputSizeBytes")
    void execute_truncatesOutput() {
        when(languageRegistry.get("python-3.12")).thenReturn(mockLanguage);
        when(mockLanguage.id()).thenReturn("python-3.12");
        when(mockLanguage.name()).thenReturn("Python");

        String hugeOutput = "A".repeat(500); // limit is 200
        SimpleCodeExecutionResult execResult = SimpleCodeExecutionResult.builder()
                .stdout(hugeOutput)
                .stderr("")
                .executionStatus(CodeExecutionStatus.SUCCESS)
                .build();
        when(codeExecutionService.run(any())).thenReturn(execResult);

        ExecuteRequest request = new ExecuteRequest("python-3.12", "print('A'*500)", "", null);
        ExecuteResponse response = compilerService.execute(request);

        assertThat(response.stdout()).contains("[Output truncated");
    }

    @Test
    @DisplayName("getSupportedLanguages: returns all registry languages")
    void getSupportedLanguages() {
        Language lang = mockLanguage;
        when(lang.id()).thenReturn("c-17");
        when(lang.name()).thenReturn("C (GCC 14)");
        when(lang.version()).thenReturn("17");
        when(lang.type()).thenReturn(LanguageType.COMPILED);
        when(lang.fileExtension()).thenReturn(".c");
        when(lang.defaultStarterCode()).thenReturn("#include <stdio.h>");
        when(lang.aliases()).thenReturn(List.of("c", "c17"));

        when(languageRegistry.getAll()).thenReturn(List.of(lang));

        List<LanguageResponse> responses = compilerService.getSupportedLanguages();
        assertThat(responses).hasSize(1);
        LanguageResponse resp = responses.get(0);
        assertThat(resp.id()).isEqualTo("c-17");
        assertThat(resp.compiled()).isTrue();
        assertThat(resp.aliases()).contains("c", "c17");
    }
}
