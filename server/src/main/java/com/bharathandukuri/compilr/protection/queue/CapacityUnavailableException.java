package com.bharathandukuri.compilr.protection.queue;

import lombok.Getter;

@Getter
public class CapacityUnavailableException extends RuntimeException {

    private final String environment;
    private final int capacity;

    public CapacityUnavailableException(String environment, int capacity, String message) {
        super(message);
        this.environment = environment;
        this.capacity = capacity;
    }

    public static CapacityUnavailableException environmentQueueFull(String environment, int queueCapacity) {
        return new CapacityUnavailableException(
                environment,
                queueCapacity,
                String.format("Execution queue for environment [%s] is full (%d capacity). Server is at capacity.", environment, queueCapacity)
        );
    }

    public static CapacityUnavailableException globalCapacityReached(int maxProcessing) {
        return new CapacityUnavailableException(
                "GLOBAL",
                maxProcessing,
                String.format("Global execution processing capacity reached (%d active executions). Server is busy.", maxProcessing)
        );
    }

    public static CapacityUnavailableException globalQueueLimitReached(int maxQueued) {
        return new CapacityUnavailableException(
                "GLOBAL",
                maxQueued,
                String.format("Global execution queue capacity reached (%d buffered requests). Server is busy.", maxQueued)
        );
    }
}
