package com.bharathandukuri.compilr.compiler.exception;

public class RuntimeFailureException extends CompilrException {

    private final String errorOutput;
    private final Long exitCode;

    public RuntimeFailureException(String message, String errorOutput, Long exitCode) {
        super(message);
        this.errorOutput = errorOutput;
        this.exitCode = exitCode;
    }

    public String getErrorOutput() {
        return errorOutput;
    }

    public Long getExitCode() {
        return exitCode;
    }
}
