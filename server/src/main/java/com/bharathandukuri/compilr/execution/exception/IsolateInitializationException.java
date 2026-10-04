package com.bharathandukuri.compilr.execution.exception;

public class IsolateInitializationException extends IsolateException{
    public IsolateInitializationException(String message) {
        super(message);
    }

    public IsolateInitializationException(String message, Throwable cause) {
        super(message, cause);
    }
}
