package com.bharathandukuri.compilr.execution.exception;

public class DockerImageCreationException extends DockerException{
    public DockerImageCreationException(String message) {
        super(message);
    }
    public DockerImageCreationException(String message, Throwable cause) {
        super(message, cause);
    }
}
