package com.bharathandukuri.compilr.execution.dto;

import lombok.Builder;

@Builder
public record DatabaseContainerConstraints(
        long cpuLimit,
        long memoryLimitKb,
        long pidsLimit,
        boolean networkDisabled) {
    public static DatabaseContainerConstraints defaults() {
        return new DatabaseContainerConstraints(
                1L,
                786432L,
                250L,
                true);
    }
}
