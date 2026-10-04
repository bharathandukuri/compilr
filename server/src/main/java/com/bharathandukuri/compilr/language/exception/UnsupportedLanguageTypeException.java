package com.bharathandukuri.compilr.language.exception;

import com.bharathandukuri.compilr.compiler.exception.CompilrException;
import com.bharathandukuri.compilr.language.LanguageType;

public class UnsupportedLanguageTypeException extends CompilrException {

    public UnsupportedLanguageTypeException(String languageId, LanguageType expectedType, LanguageType actualType) {
        super(String.format("Language '%s' is of type %s, but expected type was %s.", languageId, actualType, expectedType));
    }

    public UnsupportedLanguageTypeException(String message) {
        super(message);
    }
}
