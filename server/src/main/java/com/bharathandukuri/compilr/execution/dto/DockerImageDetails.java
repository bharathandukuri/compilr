package com.bharathandukuri.compilr.execution.dto;

public record DockerImageDetails(
        String name,
        String tag,
        String resourcePath
) {



    public String reference() {
        return name + ":" + tag;
    }
}