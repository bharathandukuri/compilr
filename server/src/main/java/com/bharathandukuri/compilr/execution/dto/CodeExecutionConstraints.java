package com.bharathandukuri.compilr.execution.dto;

public record CodeExecutionConstraints(
        long timeLimitMs,
        Long memoryLimitKb
) {
    public CodeExecutionConstraints(long timeLimitMs) {
        this(timeLimitMs, null);
    }
}