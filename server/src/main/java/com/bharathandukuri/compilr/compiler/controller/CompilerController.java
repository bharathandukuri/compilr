package com.bharathandukuri.compilr.compiler.controller;

import com.bharathandukuri.compilr.compiler.dto.ExecuteRequest;
import com.bharathandukuri.compilr.compiler.dto.ExecuteResponse;
import com.bharathandukuri.compilr.protection.admission.ExecutionAdmissionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/v1/compiler")
@RequiredArgsConstructor
@Tag(name = "Compiler", description = "Online Code Execution and Sandbox API")
public class CompilerController {

    private final ExecutionAdmissionService admissionService;

    @Operation(summary = "Execute source code", description = "Executes arbitrary untrusted code in an ephemeral, resource-constrained isolated sandbox container.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Code executed successfully (or failed gracefully inside sandbox with diagnostics)",
                    content = @Content(schema = @Schema(implementation = ExecuteResponse.class))),
            @ApiResponse(responseCode = "400", description = "Invalid request payload or unsupported language"),
            @ApiResponse(responseCode = "413", description = "Source code or input payload exceeds maximum allowed size"),
            @ApiResponse(responseCode = "429", description = "Rate limit exceeded"),
            @ApiResponse(responseCode = "500", description = "Internal sandbox engine fault"),
            @ApiResponse(responseCode = "503", description = "Execution queue or daemon capacity unavailable"),
            @ApiResponse(responseCode = "504", description = "Execution queue wait timed out")
    })
    @PostMapping("/execute")
    public ResponseEntity<ExecuteResponse> execute(
            @Valid @RequestBody ExecuteRequest request,
            HttpServletRequest servletRequest
    ) {
        ExecuteResponse response = admissionService.execute(request, servletRequest);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Compiler service health check", description = "Returns compiler service operational status")
    @GetMapping("/health")
    public ResponseEntity<Map<String, Object>> health() {
        return ResponseEntity.ok(Map.of(
                "status", "UP",
                "service", "compilr",
                "version", "0.0.1",
                "timestamp", System.currentTimeMillis()
        ));
    }
}
