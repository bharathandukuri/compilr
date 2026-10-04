package com.bharathandukuri.compilr.protection.ratelimit;

import com.bharathandukuri.compilr.protection.config.ProtectionProperties;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class SlidingWindowRateLimiterTest {

    private ProtectionProperties properties;
    private SlidingWindowRateLimiter rateLimiter;

    @BeforeEach
    void setUp() {
        properties = new ProtectionProperties();
        properties.getRateLimit().setEnabled(true);
        properties.getRateLimit().setMaxRequests(3);
        properties.getRateLimit().setWindowSeconds(2); // 2 second window

        // Initialize with null Redis to test in-memory sliding window engine
        rateLimiter = new SlidingWindowRateLimiter(properties, null);
    }

    @Test
    @DisplayName("Allows requests up to configured limit")
    void allowsRequestsUpToLimit() {
        String clientId = "client-user-1";

        assertThatCode(() -> {
            rateLimiter.checkRateLimit(clientId);
            rateLimiter.checkRateLimit(clientId);
            rateLimiter.checkRateLimit(clientId);
        }).doesNotThrowAnyException();
    }

    @Test
    @DisplayName("Rejects request when limit is exceeded within sliding window")
    void rejectsWhenLimitExceeded() {
        String clientId = "client-user-2";

        rateLimiter.checkRateLimit(clientId);
        rateLimiter.checkRateLimit(clientId);
        rateLimiter.checkRateLimit(clientId);

        assertThatThrownBy(() -> rateLimiter.checkRateLimit(clientId))
                .isInstanceOf(RateLimitExceededException.class)
                .hasMessageContaining("Rate limit exceeded for client [client-user-2]")
                .satisfies(ex -> {
                    RateLimitExceededException rle = (RateLimitExceededException) ex;
                    assertThat(rle.getClientIdentifier()).isEqualTo(clientId);
                    assertThat(rle.getRetryAfterSeconds()).isGreaterThanOrEqualTo(1L);
                });
    }

    @Test
    @DisplayName("Tracks different clients independently")
    void isolatesClients() {
        String clientA = "client-A";
        String clientB = "client-B";

        rateLimiter.checkRateLimit(clientA);
        rateLimiter.checkRateLimit(clientA);
        rateLimiter.checkRateLimit(clientA);

        // Client A is now rate limited
        assertThatThrownBy(() -> rateLimiter.checkRateLimit(clientA))
                .isInstanceOf(RateLimitExceededException.class);

        // Client B is still free to submit
        assertThatCode(() -> {
            rateLimiter.checkRateLimit(clientB);
            rateLimiter.checkRateLimit(clientB);
        }).doesNotThrowAnyException();
    }

    @Test
    @DisplayName("Allows reset of rate limit for specific client")
    void resetAllowsNewRequests() {
        String clientId = "client-reset-test";

        rateLimiter.checkRateLimit(clientId);
        rateLimiter.checkRateLimit(clientId);
        rateLimiter.checkRateLimit(clientId);

        assertThatThrownBy(() -> rateLimiter.checkRateLimit(clientId))
                .isInstanceOf(RateLimitExceededException.class);

        rateLimiter.reset(clientId);

        assertThatCode(() -> rateLimiter.checkRateLimit(clientId))
                .doesNotThrowAnyException();
    }

    @Test
    @DisplayName("Bypasses rate limiting when disabled in configuration")
    void bypassesWhenDisabled() {
        properties.getRateLimit().setEnabled(false);
        String clientId = "client-disabled-test";

        assertThatCode(() -> {
            for (int i = 0; i < 20; i++) {
                rateLimiter.checkRateLimit(clientId);
            }
        }).doesNotThrowAnyException();
    }
}
