package com.bharathandukuri.compilr.compiler.exception;

public class UnsupportedLanguageException extends CompilrException {

    public UnsupportedLanguageException(String languageId) {
        super(String.format("Unsupported language '%s'. Use GET /api/v1/compiler/languages to view supported languages.", languageId));
    }
}
