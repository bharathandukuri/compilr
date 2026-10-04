package com.bharathandukuri.compilr.execution.dto;

public record IsolateSandBoxDetails(
        int isolateBoxId,
        DockerContainerDetails dockerContainerDetails
) {
}
