package com.bharathandukuri.compilr.compiler.exception;

public class CompilationFailureException extends CompilrException {

    private final String compilationOutput;

    public CompilationFailureException(String message, String compilationOutput) {
        super(message);
        this.compilationOutput = compilationOutput;
    }

    public String getCompilationOutput() {
        return compilationOutput;
    }
}
