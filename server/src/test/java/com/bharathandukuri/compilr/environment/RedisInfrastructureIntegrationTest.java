package com.bharathandukuri.compilr.environment;

import com.bharathandukuri.compilr.compiler.dto.ExecuteResponse;
import com.bharathandukuri.compilr.compiler.enums.ExecutionStatus;
import com.bharathandukuri.compilr.protection.config.ProtectionProperties;
import com.bharathandukuri.compilr.protection.ratelimit.RateLimitExceededException;
import com.bharathandukuri.compilr.protection.ratelimit.SlidingWindowRateLimiter;
import com.bharathandukuri.compilr.redis.service.RedisService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("Redis Real Infrastructure and Sliding Window Rate Limiting Tests")
class RedisInfrastructureIntegrationTest extends BaseEnvironmentTest {

    @Autowired
    private RedisService redisService;

    @Autowired
    private SlidingWindowRateLimiter rateLimiter;

    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    @Autowired
    private ProtectionProperties protectionProperties;

    @AfterEach
    void tearDown() {
        rateLimiter.reset("test-client-1");
        rateLimiter.reset("test-client-2");
        rateLimiter.reset("concurrent-client");
    }

    @Test
    @DisplayName("RedisService: set, get, expiration, hasKey, and delete operations")
    void redisBasicOperations() {
        String testKey = "integration:test:key:" + System.currentTimeMillis();
        String testVal = "compilr_redis_val_42";

        redisService.set(testKey, testVal, Duration.ofSeconds(30));

        assertThat(redisService.hasKey(testKey)).isTrue();
        assertThat(redisService.get(testKey, String.class)).isEqualTo(testVal);

        Long expireSeconds = redisService.getExpire(testKey);
        assertThat(expireSeconds).isNotNull().isGreaterThan(0L).isLessThanOrEqualTo(30L);

        boolean deleted = redisService.delete(testKey);
        assertThat(deleted).isTrue();
        assertThat(redisService.hasKey(testKey)).isFalse();
    }

    @Test
    @DisplayName("RedisService: serializes and deserializes complex DTO with Jackson")
    void redisObjectSerialization() {
        String cacheKey = "integration:cache:exec:" + System.currentTimeMillis();
        ExecuteResponse response = ExecuteResponse.builder()
                .executionId("exec-infra-101")
                .language("java-21")
                .status(ExecutionStatus.SUCCESS)
                .stdout("COMPILR_INFRA_OK\n")
                .stderr("")
                .exitCode(0L)
                .executionTimeMs(55L)
                .memoryUsageKb(24000L)
                .build();

        redisService.set(cacheKey, response, Duration.ofMinutes(5));

        ExecuteResponse cached = redisService.get(cacheKey, ExecuteResponse.class);

        assertThat(cached).isNotNull();
        assertThat(cached.executionId()).isEqualTo("exec-infra-101");
        assertThat(cached.language()).isEqualTo("java-21");
        assertThat(cached.status()).isEqualTo(ExecutionStatus.SUCCESS);
        assertThat(cached.stdout()).isEqualTo("COMPILR_INFRA_OK\n");
        assertThat(cached.exitCode()).isEqualTo(0L);

        redisService.delete(cacheKey);
    }

    @Test
    @DisplayName("SlidingWindowRateLimiter: enforces maxRequests limit and calculates Retry-After")
    void slidingWindowRateLimiting() {
        String clientId = "test-client-1";
        rateLimiter.reset(clientId);

        int maxAllowed = protectionProperties.getRateLimit().getMaxRequests();

        // Submit maxAllowed requests -> all must succeed
        for (int i = 0; i < maxAllowed; i++) {
            rateLimiter.checkRateLimit(clientId);
        }

        // Verify Redis ZSet cardinality matches maxAllowed
        String zsetKey = "compilr:ratelimit:zset:" + clientId;
        Long count = stringRedisTemplate.opsForZSet().zCard(zsetKey);
        assertThat(count).isEqualTo((long) maxAllowed);

        // Next request must be rejected with RateLimitExceededException
        assertThatThrownBy(() -> rateLimiter.checkRateLimit(clientId))
                .isInstanceOf(RateLimitExceededException.class)
                .satisfies(ex -> {
                    RateLimitExceededException rle = (RateLimitExceededException) ex;
                    assertThat(rle.getClientIdentifier()).isEqualTo(clientId);
                    assertThat(rle.getRetryAfterSeconds()).isGreaterThan(0L);
                });
    }

    @Test
    @DisplayName("SlidingWindowRateLimiter: multi-client isolation ensures independent limits")
    void multiClientIsolation() {
        String clientA = "test-client-1";
        String clientB = "test-client-2";
        rateLimiter.reset(clientA);
        rateLimiter.reset(clientB);

        int maxAllowed = protectionProperties.getRateLimit().getMaxRequests();

        // Exhaust client A's limit
        for (int i = 0; i < maxAllowed; i++) {
            rateLimiter.checkRateLimit(clientA);
        }

        // Client A is blocked
        assertThatThrownBy(() -> rateLimiter.checkRateLimit(clientA))
                .isInstanceOf(RateLimitExceededException.class);

        // Client B is completely unaffected and passes
        rateLimiter.checkRateLimit(clientB);

        String zsetB = "compilr:ratelimit:zset:" + clientB;
        assertThat(stringRedisTemplate.opsForZSet().zCard(zsetB)).isEqualTo(1L);
    }

    @Test
    @DisplayName("SlidingWindowRateLimiter: high-concurrency burst test guarantees exact admission count")
    void concurrentBurstRateLimiting() throws Exception {
        String clientId = "concurrent-client";
        rateLimiter.reset(clientId);

        int maxAllowed = protectionProperties.getRateLimit().getMaxRequests();
        int threadCount = maxAllowed * 2; // e.g. 30 threads for limit of 15

        ExecutorService executor = Executors.newFixedThreadPool(threadCount);
        CountDownLatch startGate = new CountDownLatch(1);
        CountDownLatch endGate = new CountDownLatch(threadCount);

        AtomicInteger allowedCount = new AtomicInteger(0);
        AtomicInteger rejectedCount = new AtomicInteger(0);
        List<Throwable> unexpectedErrors = Collections.synchronizedList(new ArrayList<>());

        for (int i = 0; i < threadCount; i++) {
            executor.submit(() -> {
                try {
                    startGate.await();
                    rateLimiter.checkRateLimit(clientId);
                    allowedCount.incrementAndGet();
                } catch (RateLimitExceededException e) {
                    rejectedCount.incrementAndGet();
                } catch (Throwable t) {
                    unexpectedErrors.add(t);
                } finally {
                    endGate.countDown();
                }
            });
        }

        startGate.countDown();
        boolean completed = endGate.await(10, TimeUnit.SECONDS);
        executor.shutdown();

        assertThat(completed).isTrue();
        assertThat(unexpectedErrors).isEmpty();
        assertThat(allowedCount.get()).isEqualTo(maxAllowed);
        assertThat(rejectedCount.get()).isEqualTo(threadCount - maxAllowed);
    }
}
