package com.bharathandukuri.compilr.compiler.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Schema(description = "Request body for running code in a secure isolated sandbox")
public record ExecuteRequest(
        @NotBlank(message = "Language is required.")
        @Schema(description = "Target programming language identifier or alias (e.g., java, python, c, cpp, js, java-21, python-3.12)", example = "java-21")
        String language,

        @Size(max = 262144, message = "Source code exceeds maximum allowed size (256 KB).")
        @Schema(description = "Source code to execute", example = "public class Main { public static void main(String[] args) { System.out.println(\"Hello World!\"); } }")
        String sourceCode,

        @Size(max = 262144, message = "Standard input exceeds maximum allowed size (256 KB).")
        @Schema(description = "Standard input (stdin) provided to the program", example = "Alice")
        String stdin,

        @Schema(description = "Execution options and constraints")
        CompilerOptionsDto options
) {
}
