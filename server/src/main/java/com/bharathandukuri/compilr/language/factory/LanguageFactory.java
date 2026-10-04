package com.bharathandukuri.compilr.language.factory;

import com.bharathandukuri.compilr.language.CompiledLanguage;
import com.bharathandukuri.compilr.language.DatabaseLanguage;
import com.bharathandukuri.compilr.language.InterpretedLanguage;
import com.bharathandukuri.compilr.language.Language;
import com.bharathandukuri.compilr.execution.registry.DockerImageRegistry;

import java.util.List;
import java.util.function.Function;

public interface LanguageFactory {

    Language create(String languageId);

    CompiledLanguage createCompiled(
            String id,
            String name,
            String fileExtension,
            DockerImageRegistry dockerImage,
            Function<List<String>, List<String>> compileCommandGenerator,
            Function<List<String>, List<String>> runCommandGenerator
    );

    InterpretedLanguage createInterpreted(
            String id,
            String name,
            String fileExtension,
            DockerImageRegistry dockerImage,
            Function<List<String>, List<String>> runCommandGenerator
    );

    DatabaseLanguage createDatabase(
            String id,
            String name,
            String fileExtension,
            DockerImageRegistry dockerImage,
            Function<List<String>, List<String>> runCommandGenerator
    );

    List<String> supportedLanguageIds();
}
