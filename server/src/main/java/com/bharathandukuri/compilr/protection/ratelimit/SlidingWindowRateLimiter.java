package com.bharathandukuri.compilr.protection.ratelimit;

import com.bharathandukuri.compilr.protection.config.ProtectionProperties;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Component
public class SlidingWindowRateLimiter implements RateLimiter {

    private static final String REDIS_RATE_LIMIT_PREFIX = "compilr:ratelimit:zset:";

    private final ProtectionProperties protectionProperties;
    private final StringRedisTemplate stringRedisTemplate;

    // In-memory fallback / primary store for resilient zero-dependency execution
    private final ConcurrentHashMap<String, Deque<Long>> inMemoryBuckets = new ConcurrentHashMap<>();

    @Autowired
    public SlidingWindowRateLimiter(
            ProtectionProperties protectionProperties,
            @Autowired(required = false) StringRedisTemplate stringRedisTemplate
    ) {
        this.protectionProperties = protectionProperties;
        this.stringRedisTemplate = stringRedisTemplate;
    }

    @Override
    public void checkRateLimit(String clientIdentifier) {
        if (!protectionProperties.getRateLimit().isEnabled()) {
            return;
        }

        int maxRequests = protectionProperties.getRateLimit().getMaxRequests();
        int windowSeconds = protectionProperties.getRateLimit().getWindowSeconds();
        long windowMs = windowSeconds * 1000L;
        long now = System.currentTimeMillis();

        // 1. Try Redis sliding window if available
        if (stringRedisTemplate != null) {
            try {
                checkRedisSlidingWindow(clientIdentifier, maxRequests, windowMs, now);
                return;
            } catch (RateLimitExceededException e) {
                throw e;
            } catch (Exception e) {
                log.warn("Redis rate limiter check failed for [{}], falling back to in-memory sliding window: {}",
                        clientIdentifier, e.getMessage());
            }
        }

        // 2. Fallback to thread-safe in-memory sliding window
        checkInMemorySlidingWindow(clientIdentifier, maxRequests, windowMs, now);
    }

    private static final org.springframework.data.redis.core.script.RedisScript<java.util.List> RATE_LIMIT_LUA_SCRIPT =
            new org.springframework.data.redis.core.script.DefaultRedisScript<>(
                    """
                    local key = KEYS[1]
                    local now = tonumber(ARGV[1])
                    local windowStart = tonumber(ARGV[2])
                    local maxRequests = tonumber(ARGV[3])
                    local ttlSeconds = tonumber(ARGV[4])
                    local member = ARGV[5]

                    redis.call('ZREMRANGEBYSCORE', key, 0, windowStart)
                    local currentCount = redis.call('ZCARD', key)

                    if currentCount >= maxRequests then
                        local oldest = redis.call('ZRANGE', key, 0, 0, 'WITHSCORES')
                        if oldest and #oldest >= 2 then
                            return {0, tonumber(oldest[2])}
                        else
                            return {0, now}
                        end
                    end

                    redis.call('ZADD', key, now, member)
                    redis.call('EXPIRE', key, ttlSeconds)
                    return {1, currentCount + 1}
                    """,
                    java.util.List.class
            );

    private void checkRedisSlidingWindow(String clientIdentifier, int maxRequests, long windowMs, long now) {
        String key = REDIS_RATE_LIMIT_PREFIX + clientIdentifier;
        long windowStart = now - windowMs;
        long ttlSeconds = Math.max(60L, (windowMs * 2) / 1000L);
        String member = now + ":" + UUID.randomUUID();

        java.util.List<?> result = stringRedisTemplate.execute(
                RATE_LIMIT_LUA_SCRIPT,
                java.util.List.of(key),
                String.valueOf(now),
                String.valueOf(windowStart),
                String.valueOf(maxRequests),
                String.valueOf(ttlSeconds),
                member
        );

        if (result != null && !result.isEmpty()) {
            Number allowed = (Number) result.get(0);
            if (allowed != null && allowed.longValue() == 0L) {
                long oldestTimestamp = now;
                if (result.size() >= 2 && result.get(1) instanceof Number num) {
                    oldestTimestamp = num.longValue();
                }
                long retryAfterSeconds = Math.max(1, ((oldestTimestamp + windowMs) - now + 999) / 1000);
                log.warn("Rate limit exceeded for client [{}] (Redis: max {} reqs in {}s, retry after {}s)",
                        clientIdentifier, maxRequests, windowMs / 1000, retryAfterSeconds);
                throw new RateLimitExceededException(clientIdentifier, retryAfterSeconds);
            }
        }
    }

    private void checkInMemorySlidingWindow(String clientIdentifier, int maxRequests, long windowMs, long now) {
        Deque<Long> bucket = inMemoryBuckets.computeIfAbsent(clientIdentifier, k -> new ArrayDeque<>());
        synchronized (bucket) {
            long windowStart = now - windowMs;

            // Prune expired timestamps
            while (!bucket.isEmpty() && bucket.peekFirst() <= windowStart) {
                bucket.pollFirst();
            }

            if (bucket.size() >= maxRequests) {
                long oldest = bucket.peekFirst();
                long retryAfterSeconds = Math.max(1, ((oldest + windowMs) - now + 999) / 1000);
                log.warn("Rate limit exceeded for client [{}] (InMemory: {}/{} reqs in {}s, retry after {}s)",
                        clientIdentifier, bucket.size(), maxRequests, windowMs / 1000, retryAfterSeconds);
                throw new RateLimitExceededException(clientIdentifier, retryAfterSeconds);
            }

            bucket.addLast(now);
        }
    }

    @Override
    public void reset(String clientIdentifier) {
        inMemoryBuckets.remove(clientIdentifier);
        if (stringRedisTemplate != null) {
            try {
                stringRedisTemplate.delete(REDIS_RATE_LIMIT_PREFIX + clientIdentifier);
            } catch (Exception e) {
                log.warn("Failed to delete Redis rate limit key for [{}]: {}", clientIdentifier, e.getMessage());
            }
        }
    }
}
