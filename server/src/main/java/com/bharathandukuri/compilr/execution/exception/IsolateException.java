package com.bharathandukuri.compilr.execution.exception;

public class IsolateException extends RuntimeException {
    public IsolateException(String message) {
        super(message);
    }
    public IsolateException(String message, Throwable cause) {
        super(message, cause);
    }
}
