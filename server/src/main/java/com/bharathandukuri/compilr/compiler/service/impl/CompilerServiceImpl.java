package com.bharathandukuri.compilr.compiler.service.impl;

import com.bharathandukuri.compilr.compiler.configuration.CompilerProperties;
import com.bharathandukuri.compilr.compiler.dto.ExecuteRequest;
import com.bharathandukuri.compilr.compiler.dto.ExecuteResponse;
import com.bharathandukuri.compilr.compiler.dto.LanguageResponse;
import com.bharathandukuri.compilr.compiler.enums.ExecutionStatus;
import com.bharathandukuri.compilr.compiler.exception.InvalidCompilerRequestException;
import com.bharathandukuri.compilr.compiler.exception.OutputLimitExceededException;
import com.bharathandukuri.compilr.compiler.exception.UnsupportedLanguageException;
import com.bharathandukuri.compilr.compiler.service.CompilerService;
import com.bharathandukuri.compilr.execution.dto.CodeExecutionConstraints;
import com.bharathandukuri.compilr.execution.dto.request.SimpleCodeExecutionRequest;
import com.bharathandukuri.compilr.execution.dto.response.SimpleCodeExecutionResult;
import com.bharathandukuri.compilr.execution.enums.CodeExecutionStatus;
import com.bharathandukuri.compilr.execution.service.CodeExecutionService;
import com.bharathandukuri.compilr.language.Language;
import com.bharathandukuri.compilr.language.LanguageType;
import com.bharathandukuri.compilr.language.exception.LanguageNotFoundException;
import com.bharathandukuri.compilr.language.registry.LanguageRegistry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class CompilerServiceImpl implements CompilerService {

    private final CodeExecutionService codeExecutionService;
    private final LanguageRegistry languageRegistry;
    private final CompilerProperties compilerProperties;

    @Override
    public ExecuteResponse execute(ExecuteRequest request) {
        String executionId = UUID.randomUUID().toString();

        if (request == null) {
            throw new InvalidCompilerRequestException("Execution request body must not be null.");
        }

        if (request.language() == null || request.language().isBlank()) {
            throw new InvalidCompilerRequestException("Language identifier must not be null or blank.");
        }

        Language language;
        try {
            language = languageRegistry.get(request.language());
        } catch (LanguageNotFoundException e) {
            throw new UnsupportedLanguageException(request.language());
        }

        log.info("[exec-{}] Received execution request for language [{}] ({})",
                executionId, language.id(), language.name());

        validatePayloadSize(request.sourceCode(), "Source code", compilerProperties.getMaxSourceSizeBytes());
        validatePayloadSize(request.stdin(), "Standard input", compilerProperties.getMaxStdinSizeBytes());

        long timeLimitMs = resolveTimeLimit(request);
        Long memoryLimitKb = resolveMemoryLimit(request);

        CodeExecutionConstraints constraints = new CodeExecutionConstraints(timeLimitMs, memoryLimitKb);

        String fileName = resolveFileName(language);
        String sourceCode = request.sourceCode() != null ? request.sourceCode() : "";
        String stdin = request.stdin() != null ? request.stdin() : "";

        SimpleCodeExecutionRequest execRequest = SimpleCodeExecutionRequest.builder()
                .language(language)
                .code(sourceCode)
                .fileName(fileName)
                .stdin(stdin)
                .constraints(constraints)
                .build();

        long startTime = System.currentTimeMillis();
        SimpleCodeExecutionResult rawResult;
        try {
            rawResult = codeExecutionService.run(execRequest);
        } catch (Exception e) {
            log.error("[exec-{}] Unexpected error during code execution: {}", executionId, e.getMessage(), e);
            return ExecuteResponse.builder()
                    .executionId(executionId)
                    .language(language.id())
                    .status(ExecutionStatus.SYSTEM_ERROR)
                    .error("Execution failed due to an unexpected system error: " + e.getMessage())
                    .build();
        }
        long totalDurationMs = System.currentTimeMillis() - startTime;

        String stdout = sanitizeAndTruncate(rawResult.stdout(), compilerProperties.getMaxOutputSizeBytes());
        String stderr = sanitizeAndTruncate(rawResult.stderr(), compilerProperties.getMaxOutputSizeBytes());
        ExecutionStatus status = mapStatus(rawResult.executionStatus());

        Long executionTimeMs = rawResult.executionTimeMs() != null ? rawResult.executionTimeMs() : totalDurationMs;
        Long memoryUsageKb = rawResult.memoryUsageKb();

        log.info("[exec-{}] Execution completed with status [{}] in {}ms (process exit code: {})",
                executionId, status, executionTimeMs, rawResult.exitCode());

        return ExecuteResponse.builder()
                .executionId(executionId)
                .language(language.id())
                .status(status)
                .stdout(stdout)
                .stderr(stderr)
                .exitCode(rawResult.exitCode())
                .exitSignal(rawResult.exitSignal())
                .executionTimeMs(executionTimeMs)
                .memoryUsageKb(memoryUsageKb)
                .logs(rawResult.logs())
                .error(status == ExecutionStatus.SYSTEM_ERROR ? "An error occurred while executing the code." : null)
                .build();
    }

    @Override
    public List<LanguageResponse> getSupportedLanguages() {
        return languageRegistry.getAll().stream()
                .map(this::toResponse)
                .toList();
    }

    @Override
    public LanguageResponse getLanguage(String languageId) {
        if (languageId == null || languageId.isBlank()) {
            throw new InvalidCompilerRequestException("Language ID must not be blank.");
        }
        Language language = languageRegistry.get(languageId);
        return toResponse(language);
    }

    private LanguageResponse toResponse(Language language) {
        return new LanguageResponse(
                language.id(),
                language.name(),
                language.version(),
                language.type().name(),
                language.fileExtension(),
                language.type() == LanguageType.COMPILED,
                language.defaultStarterCode(),
                language.aliases()
        );
    }

    private void validatePayloadSize(String content, String fieldName, int maxBytes) {
        if (content == null) {
            return;
        }
        int byteCount = content.getBytes(StandardCharsets.UTF_8).length;
        if (byteCount > maxBytes) {
            throw new InvalidCompilerRequestException(
                    String.format("%s size (%d bytes) exceeds allowed maximum of %d bytes.", fieldName, byteCount, maxBytes)
            );
        }
    }

    private long resolveTimeLimit(ExecuteRequest request) {
        if (request.options() != null && request.options().timeLimitMs() != null && request.options().timeLimitMs() > 0) {
            long requested = request.options().timeLimitMs();
            return Math.clamp(requested, compilerProperties.getMinTimeLimitMs(), compilerProperties.getMaxTimeLimitMs());
        }
        return compilerProperties.getDefaultTimeLimitMs();
    }

    private Long resolveMemoryLimit(ExecuteRequest request) {
        if (request.options() != null && request.options().memoryLimitKb() != null && request.options().memoryLimitKb() > 0) {
            return request.options().memoryLimitKb();
        }
        return compilerProperties.getDefaultMemoryLimitKb();
    }

    private String resolveFileName(Language language) {
        if ("java-21".equalsIgnoreCase(language.id())) {
            return "Solution.java";
        }
        String ext = language.fileExtension() != null ? language.fileExtension() : "";
        return "solution" + ext;
    }

    private String sanitizeAndTruncate(String output, int maxBytes) {
        if (output == null) {
            return "";
        }
        byte[] bytes = output.getBytes(StandardCharsets.UTF_8);
        if (bytes.length <= maxBytes) {
            return output;
        }
        String truncated = new String(bytes, 0, maxBytes, StandardCharsets.UTF_8);
        return truncated + "\n... [Output truncated after reaching maximum limit of " + (maxBytes / 1024) + " KB]";
    }

    private ExecutionStatus mapStatus(CodeExecutionStatus status) {
        if (status == null) {
            return ExecutionStatus.SYSTEM_ERROR;
        }
        return switch (status) {
            case SUCCESS -> ExecutionStatus.SUCCESS;
            case COMPILATION_ERROR -> ExecutionStatus.COMPILATION_ERROR;
            case RUNTIME_ERROR -> ExecutionStatus.RUNTIME_ERROR;
            case TIME_LIMIT_EXCEEDED -> ExecutionStatus.TIME_LIMIT_EXCEEDED;
            case MEMORY_LIMIT_EXCEEDED -> ExecutionStatus.MEMORY_LIMIT_EXCEEDED;
            case OUTPUT_LIMIT_EXCEEDED -> ExecutionStatus.OUTPUT_LIMIT_EXCEEDED;
            case SYSTEM_ERROR -> ExecutionStatus.SYSTEM_ERROR;
        };
    }
}
