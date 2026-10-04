package com.bharathandukuri.compilr.execution.dto.response;

import com.bharathandukuri.compilr.execution.enums.IsolateExecutionStatus;

public record IsolateExecutionResult(
        IsolateExecutionStatus status,

        String stdout,
        String stderr,

        Double cpuTimeSeconds,
        Double wallTimeSeconds,

        Long memoryKb,

        Long exitCode,
        Long exitSignal,

        Boolean killed,

        Long contextSwitchesVoluntary,
        Long contextSwitchesForced
) {
}