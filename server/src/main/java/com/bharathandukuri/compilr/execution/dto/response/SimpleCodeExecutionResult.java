package com.bharathandukuri.compilr.execution.dto.response;

import com.bharathandukuri.compilr.execution.enums.CodeExecutionStatus;
import lombok.Builder;

import java.util.List;

@Builder
public record SimpleCodeExecutionResult(
        String stdout,
        String stderr,
        Long exitCode,
        Long exitSignal,
        CodeExecutionStatus executionStatus,
        Long executionTimeMs,
        Long memoryUsageKb,
        List<String> logs
) {
}