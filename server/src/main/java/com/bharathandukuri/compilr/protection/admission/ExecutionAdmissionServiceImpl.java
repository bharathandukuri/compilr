package com.bharathandukuri.compilr.protection.admission;

import com.bharathandukuri.compilr.compiler.configuration.CompilerProperties;
import com.bharathandukuri.compilr.compiler.dto.ExecuteRequest;
import com.bharathandukuri.compilr.compiler.dto.ExecuteResponse;
import com.bharathandukuri.compilr.compiler.enums.ExecutionStatus;
import com.bharathandukuri.compilr.compiler.exception.ExecutionTimeoutException;
import com.bharathandukuri.compilr.compiler.exception.InvalidCompilerRequestException;
import com.bharathandukuri.compilr.compiler.service.CompilerService;
import com.bharathandukuri.compilr.protection.config.ProtectionProperties;
import com.bharathandukuri.compilr.protection.queue.ExecutionQueueManager;
import com.bharathandukuri.compilr.protection.ratelimit.ClientIdentifierResolver;
import com.bharathandukuri.compilr.protection.ratelimit.RateLimiter;
import com.bharathandukuri.compilr.redis.service.RedisService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Duration;
import java.util.HexFormat;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

@Slf4j
@Service
@RequiredArgsConstructor
public class ExecutionAdmissionServiceImpl implements ExecutionAdmissionService {

    private static final String CACHE_KEY_PREFIX = "compilr:cache:exec:";
    private static final Duration EXECUTION_CACHE_TTL = Duration.ofMinutes(10);

    private final RateLimiter rateLimiter;
    private final ClientIdentifierResolver clientIdentifierResolver;
    private final ExecutionQueueManager executionQueueManager;
    private final CompilerService compilerService;
    private final RedisService redisService;
    private final CompilerProperties compilerProperties;
    private final ProtectionProperties protectionProperties;

    @Override
    public ExecuteResponse execute(ExecuteRequest request, HttpServletRequest httpRequest) {
        // Step 1: Client Identification & Sliding-Window Rate Limiting
        String clientIdentifier = clientIdentifierResolver.resolveClientId(httpRequest);
        rateLimiter.checkRateLimit(clientIdentifier);

        // Step 2: Basic Payload Validation prior to queue admission
        validateRequest(request);

        // Step 3: Check Redis Execution Cache (Bypass sandbox execution on cache hit)
        String cacheKey = computeCacheKey(request);
        ExecuteResponse cachedResult = tryGetFromCache(cacheKey, clientIdentifier);
        if (cachedResult != null) {
            return cachedResult;
        }

        // Step 4: Submit to Isolated Environment Execution Queue
        String environmentId = request.language();
        CompletableFuture<ExecuteResponse> executionFuture =
                executionQueueManager.submit(environmentId, () -> compilerService.execute(request));

        // Step 5: Wait for Result within Maximum Bounded Duration
        long timeLimitMs = resolveTimeLimit(request);
        long maxQueueWaitMs = protectionProperties.getQueue().getMaxQueueWaitTimeoutMs();
        long totalWaitTimeoutMs = timeLimitMs + maxQueueWaitMs + 3000L; // Safety buffer for container cleanup

        ExecuteResponse response;
        try {
            response = executionFuture.get(totalWaitTimeoutMs, TimeUnit.MILLISECONDS);
        } catch (TimeoutException e) {
            executionFuture.cancel(true);
            log.warn("Execution request for client [{}] timed out after {}ms total wait", clientIdentifier, totalWaitTimeoutMs);
            throw new ExecutionTimeoutException("Execution timed out after " + totalWaitTimeoutMs + "ms.");
        } catch (InterruptedException e) {
            executionFuture.cancel(true);
            Thread.currentThread().interrupt();
            throw new RuntimeException("Execution was interrupted.", e);
        } catch (ExecutionException e) {
            Throwable cause = e.getCause();
            if (cause instanceof RuntimeException runtimeException) {
                throw runtimeException;
            }
            throw new RuntimeException("Execution failed: " + cause.getMessage(), cause);
        }

        // Step 6: Populate Redis Cache for Deterministic Results
        tryCacheExecutionResult(cacheKey, response);

        return response;
    }

    private void validateRequest(ExecuteRequest request) {
        if (request == null) {
            throw new InvalidCompilerRequestException("Execution request body must not be null.");
        }
        if (request.language() == null || request.language().isBlank()) {
            throw new InvalidCompilerRequestException("Language identifier must not be null or blank.");
        }
    }

    private String computeCacheKey(ExecuteRequest request) {
        String language = request.language() != null ? request.language() : "";
        String code = request.sourceCode() != null ? request.sourceCode() : "";
        String stdin = request.stdin() != null ? request.stdin() : "";
        String raw = language + "\n--COMPILR_SEP--\n" + code + "\n--COMPILR_SEP--\n" + stdin;

        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(raw.getBytes(StandardCharsets.UTF_8));
            return CACHE_KEY_PREFIX + HexFormat.of().formatHex(hash);
        } catch (NoSuchAlgorithmException e) {
            return CACHE_KEY_PREFIX + Integer.toHexString(raw.hashCode());
        }
    }

    private ExecuteResponse tryGetFromCache(String cacheKey, String clientIdentifier) {
        try {
            ExecuteResponse cached = redisService.get(cacheKey, ExecuteResponse.class);
            if (cached != null) {
                log.info("Execution cache HIT for client [{}] (cache key: {})", clientIdentifier, cacheKey);
                return cached;
            }
        } catch (Exception e) {
            log.warn("Redis execution cache lookup failed: {}", e.getMessage());
        }
        return null;
    }

    private void tryCacheExecutionResult(String cacheKey, ExecuteResponse response) {
        if (response == null) {
            return;
        }

        // Only cache deterministic outputs (SUCCESS, COMPILATION_ERROR, RUNTIME_ERROR)
        ExecutionStatus status = response.status();
        if (status == ExecutionStatus.SUCCESS ||
                status == ExecutionStatus.COMPILATION_ERROR ||
                status == ExecutionStatus.RUNTIME_ERROR) {
            try {
                redisService.set(cacheKey, response, EXECUTION_CACHE_TTL);
            } catch (Exception e) {
                log.warn("Failed to cache execution result in Redis: {}", e.getMessage());
            }
        }
    }

    private long resolveTimeLimit(ExecuteRequest request) {
        if (request.options() != null && request.options().timeLimitMs() != null && request.options().timeLimitMs() > 0) {
            long requested = request.options().timeLimitMs();
            return Math.clamp(requested, compilerProperties.getMinTimeLimitMs(), compilerProperties.getMaxTimeLimitMs());
        }
        return compilerProperties.getDefaultTimeLimitMs();
    }
}
