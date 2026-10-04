package com.bharathandukuri.compilr.compiler.exception;

public class DockerUnavailableException extends CompilrException {

    public DockerUnavailableException(String message) {
        super(message);
    }

    public DockerUnavailableException(String message, Throwable cause) {
        super(message, cause);
    }
}
