package com.bharathandukuri.compilr.compiler.dto;

import com.bharathandukuri.compilr.compiler.enums.ExecutionStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

import java.util.List;

@Builder
@Schema(description = "Structured output and metrics from isolated code execution")
public record ExecuteResponse(
        @Schema(description = "Unique correlation identifier for this execution", example = "550e8400-e29b-41d4-a716-446655440000")
        String executionId,

        @Schema(description = "Canonical language identifier used for execution", example = "java-21")
        String language,

        @Schema(description = "Execution status verdict")
        ExecutionStatus status,

        @Schema(description = "Captured standard output (stdout)")
        String stdout,

        @Schema(description = "Captured standard error (stderr) or compiler diagnostics")
        String stderr,

        @Schema(description = "Process exit code (0 indicates clean exit)", example = "0")
        Long exitCode,

        @Schema(description = "Signal number if killed by OS signal (e.g., 9 for SIGKILL, 11 for SIGSEGV)", example = "0")
        Long exitSignal,

        @Schema(description = "Execution time in milliseconds", example = "85")
        Long executionTimeMs,

        @Schema(description = "Peak memory usage in kilobytes (where supported by sandbox)", example = "14320")
        Long memoryUsageKb,

        @Schema(description = "Diagnostic sandbox logs")
        List<String> logs,

        @Schema(description = "Error summary if execution failed")
        String error
) {
}
