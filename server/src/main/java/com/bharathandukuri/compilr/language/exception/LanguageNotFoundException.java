package com.bharathandukuri.compilr.language.exception;

import com.bharathandukuri.compilr.compiler.exception.CompilrException;

public class LanguageNotFoundException extends CompilrException {

    public LanguageNotFoundException(String languageId) {
        super(String.format("Language not found with id: '%s'", languageId));
    }

    public LanguageNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
}
