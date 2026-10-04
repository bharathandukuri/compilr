package com.bharathandukuri.compilr.compiler.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

@Schema(description = "Details about a supported execution language runtime")
public record LanguageResponse(
        @Schema(description = "Unique canonical language identifier", example = "java-21")
        String id,

        @Schema(description = "Human-readable display name", example = "Java (OpenJDK 21)")
        String name,

        @Schema(description = "Runtime or compiler version", example = "21 (OpenJDK)")
        String version,

        @Schema(description = "Execution paradigm category (COMPILED, INTERPRETED, DATABASE)", example = "COMPILED")
        String type,

        @Schema(description = "Source code file extension", example = ".java")
        String fileExtension,

        @Schema(description = "Whether an upfront compilation phase is required", example = "true")
        boolean compiled,

        @Schema(description = "Boilerplate starter code for this language")
        String defaultStarterCode,

        @Schema(description = "Recognized alias identifiers", example = "[\"java\", \"java21\"]")
        List<String> aliases
) {
}
