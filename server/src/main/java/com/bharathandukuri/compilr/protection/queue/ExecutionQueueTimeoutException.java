package com.bharathandukuri.compilr.protection.queue;

import lombok.Getter;

@Getter
public class ExecutionQueueTimeoutException extends RuntimeException {

    private final String environment;
    private final long waitTimeMs;

    public ExecutionQueueTimeoutException(String environment, long waitTimeMs) {
        super(String.format("Execution request for environment [%s] timed out after waiting %dms in queue.",
                environment, waitTimeMs));
        this.environment = environment;
        this.waitTimeMs = waitTimeMs;
    }
}
