package com.bharathandukuri.compilr.compiler.controller;

import com.bharathandukuri.compilr.compiler.dto.ExecuteRequest;
import com.bharathandukuri.compilr.compiler.dto.ExecuteResponse;
import com.bharathandukuri.compilr.compiler.dto.LanguageResponse;
import com.bharathandukuri.compilr.compiler.enums.ExecutionStatus;
import com.bharathandukuri.compilr.compiler.exception.GlobalExceptionHandler;
import com.bharathandukuri.compilr.compiler.exception.UnsupportedLanguageException;
import com.bharathandukuri.compilr.compiler.service.CompilerService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class CompilerControllerTest {

    @Mock
    private CompilerService compilerService;

    private MockMvc mockMvc;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    void setUp() {
        CompilerController compilerController = new CompilerController(compilerService);
        LanguageController languageController = new LanguageController(compilerService);
        mockMvc = MockMvcBuilders
                .standaloneSetup(compilerController, languageController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    @DisplayName("POST /api/v1/compiler/execute - success returns 200 with result")
    void execute_success() throws Exception {
        ExecuteResponse response = ExecuteResponse.builder()
                .executionId("exec-12345")
                .language("java-21")
                .status(ExecutionStatus.SUCCESS)
                .stdout("Hello World!\n")
                .stderr("")
                .exitCode(0L)
                .executionTimeMs(120L)
                .memoryUsageKb(15000L)
                .build();

        when(compilerService.execute(any(ExecuteRequest.class))).thenReturn(response);

        ExecuteRequest request = new ExecuteRequest(
                "java-21",
                "public class Solution { public static void main(String[] args) {} }",
                "",
                null
        );

        mockMvc.perform(post("/api/v1/compiler/execute")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.executionId").value("exec-12345"))
                .andExpect(jsonPath("$.status").value("SUCCESS"))
                .andExpect(jsonPath("$.stdout").value("Hello World!\n"))
                .andExpect(jsonPath("$.executionTimeMs").value(120));
    }

    @Test
    @DisplayName("POST /api/v1/compiler/execute - blank language returns 400 Bad Request")
    void execute_blankLanguage() throws Exception {
        ExecuteRequest request = new ExecuteRequest("", "code", "", null);

        mockMvc.perform(post("/api/v1/compiler/execute")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("Bad Request"));
    }

    @Test
    @DisplayName("POST /api/v1/compiler/execute - unsupported language returns 400")
    void execute_unsupportedLanguage() throws Exception {
        when(compilerService.execute(any()))
                .thenThrow(new UnsupportedLanguageException("unknown-lang"));

        ExecuteRequest request = new ExecuteRequest("unknown-lang", "code", "", null);

        mockMvc.perform(post("/api/v1/compiler/execute")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Unsupported language 'unknown-lang'. Use GET /api/v1/compiler/languages to view supported languages."));
    }

    @Test
    @DisplayName("GET /api/v1/compiler/health - returns 200 UP")
    void health() throws Exception {
        mockMvc.perform(get("/api/v1/compiler/health"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("UP"))
                .andExpect(jsonPath("$.service").value("compilr"));
    }

    @Test
    @DisplayName("GET /api/v1/compiler/languages - returns 200 with list")
    void languages() throws Exception {
        LanguageResponse java = new LanguageResponse(
                "java-21",
                "Java (OpenJDK 21)",
                "21",
                "COMPILED",
                ".java",
                true,
                "public class Solution {}",
                List.of("java")
        );
        when(compilerService.getSupportedLanguages()).thenReturn(List.of(java));

        mockMvc.perform(get("/api/v1/compiler/languages"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value("java-21"))
                .andExpect(jsonPath("$[0].name").value("Java (OpenJDK 21)"))
                .andExpect(jsonPath("$[0].compiled").value(true));
    }
}
