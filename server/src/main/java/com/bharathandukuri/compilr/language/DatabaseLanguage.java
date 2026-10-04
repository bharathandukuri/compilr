package com.bharathandukuri.compilr.language;

public interface DatabaseLanguage extends Language {

    @Override
    default LanguageType type() {
        return LanguageType.DATABASE;
    }
}
