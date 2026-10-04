package com.bharathandukuri.compilr.execution.exception;

public class DockerContainerException extends DockerException{
    public DockerContainerException(String message) {
        super(message);
    }

    public DockerContainerException(String message, Throwable cause) {
        super(message, cause);
    }
}
