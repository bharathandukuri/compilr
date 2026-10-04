package com.bharathandukuri.compilr.protection.admission;

import com.bharathandukuri.compilr.compiler.configuration.CompilerProperties;
import com.bharathandukuri.compilr.compiler.dto.ExecuteRequest;
import com.bharathandukuri.compilr.compiler.dto.ExecuteResponse;
import com.bharathandukuri.compilr.compiler.enums.ExecutionStatus;
import com.bharathandukuri.compilr.compiler.service.CompilerService;
import com.bharathandukuri.compilr.protection.config.ProtectionProperties;
import com.bharathandukuri.compilr.protection.queue.ExecutionQueueManager;
import com.bharathandukuri.compilr.protection.queue.GlobalCapacityTracker;
import com.bharathandukuri.compilr.protection.ratelimit.ClientIdentifierResolver;
import com.bharathandukuri.compilr.protection.ratelimit.RateLimitExceededException;
import com.bharathandukuri.compilr.protection.ratelimit.RateLimiter;
import com.bharathandukuri.compilr.redis.service.RedisService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpServletRequest;

import java.time.Duration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ExecutionAdmissionServiceTest {

    @Mock
    private RateLimiter rateLimiter;

    @Mock
    private CompilerService compilerService;

    @Mock
    private RedisService redisService;

    private ClientIdentifierResolver clientResolver;
    private ExecutionQueueManager queueManager;
    private CompilerProperties compilerProperties;
    private ProtectionProperties protectionProperties;
    private ExecutionAdmissionServiceImpl admissionService;

    @BeforeEach
    void setUp() {
        clientResolver = new ClientIdentifierResolver();
        compilerProperties = new CompilerProperties();
        protectionProperties = new ProtectionProperties();

        GlobalCapacityTracker globalCapacityTracker = new GlobalCapacityTracker(protectionProperties);
        queueManager = new ExecutionQueueManager(protectionProperties, globalCapacityTracker);

        admissionService = new ExecutionAdmissionServiceImpl(
                rateLimiter,
                clientResolver,
                queueManager,
                compilerService,
                redisService,
                compilerProperties,
                protectionProperties
        );
    }

    @AfterEach
    void tearDown() {
        if (queueManager != null) {
            queueManager.shutdown();
        }
    }

    @Test
    @DisplayName("Executes request and caches deterministic success in Redis")
    void executesAndCachesResult() {
        ExecuteRequest request = new ExecuteRequest(
                "python-3.12",
                "print('hello world')",
                "",
                null
        );
        MockHttpServletRequest httpRequest = new MockHttpServletRequest();
        httpRequest.addHeader("X-Client-Id", "client-test-42");

        ExecuteResponse serviceResponse = ExecuteResponse.builder()
                .executionId("exec-999")
                .language("python-3.12")
                .status(ExecutionStatus.SUCCESS)
                .stdout("hello world\n")
                .executionTimeMs(50L)
                .build();

        // No cache hit
        when(redisService.get(anyString(), eq(ExecuteResponse.class))).thenReturn(null);
        when(compilerService.execute(request)).thenReturn(serviceResponse);

        ExecuteResponse result = admissionService.execute(request, httpRequest);

        assertThat(result).isNotNull();
        assertThat(result.status()).isEqualTo(ExecutionStatus.SUCCESS);
        assertThat(result.stdout()).isEqualTo("hello world\n");

        verify(rateLimiter).checkRateLimit("client:client-test-42");
        verify(compilerService).execute(request);
        verify(redisService).set(anyString(), eq(serviceResponse), any(Duration.class));
    }

    @Test
    @DisplayName("Returns cached execution directly without touching execution queue or compilerService")
    void returnsCachedResultOnCacheHit() {
        ExecuteRequest request = new ExecuteRequest(
                "python-3.12",
                "print('cached code')",
                "",
                null
        );
        MockHttpServletRequest httpRequest = new MockHttpServletRequest();

        ExecuteResponse cachedResponse = ExecuteResponse.builder()
                .executionId("cached-111")
                .language("python-3.12")
                .status(ExecutionStatus.SUCCESS)
                .stdout("cached output\n")
                .executionTimeMs(0L)
                .build();

        when(redisService.get(anyString(), eq(ExecuteResponse.class))).thenReturn(cachedResponse);

        ExecuteResponse result = admissionService.execute(request, httpRequest);

        assertThat(result).isSameAs(cachedResponse);
        verify(compilerService, never()).execute(any());
    }

    @Test
    @DisplayName("Rejects early when rate limiter throws RateLimitExceededException")
    void rejectsOnRateLimitExceeded() {
        ExecuteRequest request = new ExecuteRequest(
                "java-21",
                "code",
                "",
                null
        );
        MockHttpServletRequest httpRequest = new MockHttpServletRequest();
        httpRequest.addHeader("X-Client-Id", "blocked-client");

        doThrow(new RateLimitExceededException("client:blocked-client", 30))
                .when(rateLimiter).checkRateLimit("client:blocked-client");

        assertThatThrownBy(() -> admissionService.execute(request, httpRequest))
                .isInstanceOf(RateLimitExceededException.class);

        verify(compilerService, never()).execute(any());
        verify(redisService, never()).get(anyString(), eq(ExecuteResponse.class));
    }
}
