package com.bharathandukuri.compilr.execution.exception;

public class DockerContainerStartException extends DockerContainerException{
    public DockerContainerStartException(String message) {
        super(message);
    }

    public DockerContainerStartException(String message, Throwable cause) {
        super(message, cause);
    }
}
