package com.bharathandukuri.compilr.execution.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Getter
@Setter
@ConfigurationProperties(prefix = "compilr.execution.docker")
public class DockerProperties {

    private boolean enabled = true;

    private String host = "unix:///var/run/docker.sock";
}