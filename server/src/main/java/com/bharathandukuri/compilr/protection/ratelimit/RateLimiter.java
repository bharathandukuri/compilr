package com.bharathandukuri.compilr.protection.ratelimit;

public interface RateLimiter {

    /**
     * Checks if the request is permitted within the sliding window.
     * Throws RateLimitExceededException if exceeded.
     *
     * @param clientIdentifier identifier of the client (token or IP)
     * @throws RateLimitExceededException if client exceeded allowable request rate
     */
    void checkRateLimit(String clientIdentifier);

    /**
     * Resets any rate limit state for the given client (useful in testing or administrative resets).
     */
    void reset(String clientIdentifier);
}
