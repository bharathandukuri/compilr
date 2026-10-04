package com.bharathandukuri.compilr.execution.dto.response;

public record DockerExecutionResult (
        long exitCode,
        String stdout,
        String stderr
) {
}
