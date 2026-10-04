package com.bharathandukuri.compilr.compiler.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Optional execution constraints such as time and memory limit")
public record CompilerOptionsDto(
        @Schema(description = "Maximum execution time in milliseconds (100 - 15000)", example = "5000")
        Long timeLimitMs,

        @Schema(description = "Maximum memory limit in kilobytes", example = "262144")
        Long memoryLimitKb
) {
}
