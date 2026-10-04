package com.bharathandukuri.compilr.compiler.exception;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

import java.time.Instant;

@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(description = "Standard error payload returned on API failures")
public record ErrorResponse(
        @Schema(description = "UTC timestamp when the error occurred", example = "2026-10-04T10:30:00Z")
        Instant timestamp,

        @Schema(description = "HTTP status code", example = "400")
        int status,

        @Schema(description = "HTTP status name", example = "Bad Request")
        String error,

        @Schema(description = "Descriptive error message", example = "Unsupported language 'rust'")
        String message,

        @Schema(description = "Request URI path", example = "/api/v1/compiler/execute")
        String path,

        @Schema(description = "Correlation execution ID if available")
        String executionId
) {
}
