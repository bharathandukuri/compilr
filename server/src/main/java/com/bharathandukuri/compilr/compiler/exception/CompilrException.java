package com.bharathandukuri.compilr.compiler.exception;

public class CompilrException extends RuntimeException {

    public CompilrException(String message) {
        super(message);
    }

    public CompilrException(String message, Throwable cause) {
        super(message, cause);
    }
}
