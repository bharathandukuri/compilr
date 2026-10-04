package com.bharathandukuri.compilr.execution.exception;

public class IsolateCleanupException extends IsolateException{
    public IsolateCleanupException(String message) {
        super(message);
    }

    public IsolateCleanupException(String message, Throwable cause) {
        super(message, cause);
    }
}
