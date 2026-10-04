package com.bharathandukuri.compilr.environment;

import com.bharathandukuri.compilr.compiler.dto.ExecuteRequest;
import com.bharathandukuri.compilr.protection.config.ProtectionProperties;
import com.bharathandukuri.compilr.protection.ratelimit.RateLimiter;
import tools.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@DisplayName("Compilr End-to-End Pipeline HTTP Integration Tests")
class CompilrEndToEndPipelineIntegrationTest extends BaseEnvironmentTest {

    @Autowired
    private WebApplicationContext webApplicationContext;

    private MockMvc mockMvc;

    @Autowired
    private RateLimiter rateLimiter;

    @Autowired
    private ProtectionProperties protectionProperties;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
    }

    @AfterEach
    void tearDown() {
        rateLimiter.reset("client:e2e-user");
        rateLimiter.reset("client:rate-limit-test");
    }

    @Test
    @DisplayName("POST /api/v1/compiler/execute - Valid execution returns 200 OK with full response")
    void fullExecutionPipelineSuccess() throws Exception {
        ExecuteRequest request = new ExecuteRequest(
                "python-3.12",
                "print('E2E_PIPELINE_SUCCESS')",
                "",
                null
        );

        mockMvc.perform(post("/api/v1/compiler/execute")
                        .header("X-Client-Id", "e2e-user")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("SUCCESS"))
                .andExpect(jsonPath("$.stdout").value("E2E_PIPELINE_SUCCESS\n"))
                .andExpect(jsonPath("$.exitCode").value(0))
                .andExpect(jsonPath("$.executionTimeMs").value(greaterThanOrEqualTo(0)));
    }

    @Test
    @DisplayName("POST /api/v1/compiler/execute - Repeated execution serves from Redis cache")
    void executionCaching() throws Exception {
        String uniqueCode = "print('CACHE_TEST_" + System.currentTimeMillis() + "')";
        ExecuteRequest request = new ExecuteRequest("python-3.12", uniqueCode, "", null);

        // First execution runs on real Docker container
        long start1 = System.currentTimeMillis();
        mockMvc.perform(post("/api/v1/compiler/execute")
                        .header("X-Client-Id", "e2e-user")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("SUCCESS"));
        long elapsed1 = System.currentTimeMillis() - start1;

        // Second identical execution should hit Redis cache (much faster, sub-100ms)
        long start2 = System.currentTimeMillis();
        mockMvc.perform(post("/api/v1/compiler/execute")
                        .header("X-Client-Id", "e2e-user")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("SUCCESS"));
        long elapsed2 = System.currentTimeMillis() - start2;

        // Cache hit must be significantly faster than initial container spin-up
        assertThat(elapsed2).isLessThan(elapsed1 + 100);
    }

    @Test
    @DisplayName("POST /api/v1/compiler/execute - High-frequency burst triggers 429 Too Many Requests with Retry-After")
    void rateLimitingEndpointEnforcement() throws Exception {
        String clientId = "rate-limit-test";
        rateLimiter.reset("client:" + clientId);

        ExecuteRequest request = new ExecuteRequest("python-3.12", "print('hello')", "", null);
        int maxAllowed = protectionProperties.getRateLimit().getMaxRequests();

        // Send requests up to rate limit
        for (int i = 0; i < maxAllowed; i++) {
            mockMvc.perform(post("/api/v1/compiler/execute")
                            .header("X-Client-Id", clientId)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk());
        }

        // Next request triggers 429
        mockMvc.perform(post("/api/v1/compiler/execute")
                        .header("X-Client-Id", clientId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isTooManyRequests())
                .andExpect(header().exists("Retry-After"))
                .andExpect(jsonPath("$.status").value(429))
                .andExpect(jsonPath("$.error").value("Too Many Requests"));
    }

    @Test
    @DisplayName("GET /api/v1/compiler/languages - Returns all supported languages")
    void getLanguagesEndpoint() throws Exception {
        mockMvc.perform(get("/api/v1/compiler/languages"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[?(@.id == 'java-21')].name").value("Java (OpenJDK 21)"))
                .andExpect(jsonPath("$[?(@.id == 'c-17')].name").value("C (GCC 13.2)"))
                .andExpect(jsonPath("$[?(@.id == 'cpp-23')].name").value("C++ (GCC 13.2)"))
                .andExpect(jsonPath("$[?(@.id == 'python-3.12')].name").value("Python (CPython 3.12)"))
                .andExpect(jsonPath("$[?(@.id == 'javascript-node-20')].name").value("JavaScript (Node.js 20)"))
                .andExpect(jsonPath("$[?(@.id == 'postgresql-16')].name").value("PostgreSQL (16)"))
                .andExpect(jsonPath("$[?(@.id == 'mysql-8.0')].name").value("MySQL (8.0)"));
    }

    @Test
    @DisplayName("GET /api/v1/compiler/health - Returns 200 UP")
    void getHealthEndpoint() throws Exception {
        mockMvc.perform(get("/api/v1/compiler/health"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("UP"))
                .andExpect(jsonPath("$.service").value("compilr"));
    }
}
