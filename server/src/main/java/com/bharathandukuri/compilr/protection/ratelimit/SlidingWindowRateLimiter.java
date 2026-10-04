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

    private void checkRedisSlidingWindow(String clientIdentifier, int maxRequests, long windowMs, long now) {
        String key = REDIS_RATE_LIMIT_PREFIX + clientIdentifier;
        long windowStart = now - windowMs;

        // Prune expired entries outside current sliding window
        stringRedisTemplate.opsForZSet().removeRangeByScore(key, 0, (double) windowStart);

        Long currentCount = stringRedisTemplate.opsForZSet().zCard(key);
        if (currentCount != null && currentCount >= maxRequests) {
            // Find oldest timestamp in current window to calculate retry-after
            Set<ZSetOperations.TypedTuple<String>> oldestTuples =
                    stringRedisTemplate.opsForZSet().rangeWithScores(key, 0, 0);

            long oldestTimestamp = now;
            if (oldestTuples != null && !oldestTuples.isEmpty()) {
                Double score = oldestTuples.iterator().next().getScore();
                if (score != null) {
                    oldestTimestamp = score.longValue();
                }
            }

            long retryAfterSeconds = Math.max(1, ((oldestTimestamp + windowMs) - now + 999) / 1000);
            log.warn("Rate limit exceeded for client [{}] (Redis: {}/{} reqs in {}s, retry after {}s)",
                    clientIdentifier, currentCount, maxRequests, windowMs / 1000, retryAfterSeconds);
            throw new RateLimitExceededException(clientIdentifier, retryAfterSeconds);
        }

        // Add current execution timestamp
        String member = now + ":" + UUID.randomUUID();
        stringRedisTemplate.opsForZSet().add(key, member, (double) now);
        stringRedisTemplate.expire(key, Duration.ofMillis(windowMs * 2));
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
