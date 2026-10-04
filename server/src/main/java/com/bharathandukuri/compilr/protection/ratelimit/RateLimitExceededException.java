package com.bharathandukuri.compilr.protection.ratelimit;

import lombok.Getter;

@Getter
public class RateLimitExceededException extends RuntimeException {

    private final String clientIdentifier;
    private final long retryAfterSeconds;

    public RateLimitExceededException(String clientIdentifier, long retryAfterSeconds) {
        super(String.format("Rate limit exceeded for client [%s]. Please retry after %d seconds.",
                clientIdentifier, retryAfterSeconds));
        this.clientIdentifier = clientIdentifier;
        this.retryAfterSeconds = retryAfterSeconds;
    }
}
