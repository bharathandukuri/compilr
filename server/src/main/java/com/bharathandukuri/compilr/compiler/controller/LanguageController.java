package com.bharathandukuri.compilr.compiler.controller;

import com.bharathandukuri.compilr.compiler.dto.LanguageResponse;
import com.bharathandukuri.compilr.compiler.service.CompilerService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/compiler/languages")
@RequiredArgsConstructor
@Tag(name = "Languages", description = "Query supported programming languages and configurations")
public class LanguageController {

    private final CompilerService compilerService;

    @Operation(summary = "List all supported programming languages", description = "Retrieves all supported compilers, interpreters, and database query engines with versions and templates.")
    @GetMapping
    public ResponseEntity<List<LanguageResponse>> listLanguages() {
        return ResponseEntity.ok(compilerService.getSupportedLanguages());
    }

    @Operation(summary = "Get language details", description = "Retrieves details, aliases, and boilerplate code for a specific language by ID or alias.")
    @GetMapping("/{id}")
    public ResponseEntity<LanguageResponse> getLanguage(
            @Parameter(description = "Language ID or alias (e.g. java, java-21, python, cpp)", example = "java-21")
            @PathVariable String id
    ) {
        return ResponseEntity.ok(compilerService.getLanguage(id));
    }
}
