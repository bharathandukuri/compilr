package com.bharathandukuri.compilr.language.registry;

import com.bharathandukuri.compilr.language.CompiledLanguage;
import com.bharathandukuri.compilr.language.DatabaseLanguage;
import com.bharathandukuri.compilr.language.InterpretedLanguage;
import com.bharathandukuri.compilr.language.Language;
import com.bharathandukuri.compilr.language.LanguageType;

import java.util.List;

public interface LanguageRegistry {

    void register(Language language);

    void unregister(String id);

    Language get(String id);

    CompiledLanguage getCompiled(String id);

    InterpretedLanguage getInterpreted(String id);

    DatabaseLanguage getDatabase(String id);

    List<Language> getAll();

    List<Language> getAll(LanguageType type);

    boolean isSupported(String id);
}
