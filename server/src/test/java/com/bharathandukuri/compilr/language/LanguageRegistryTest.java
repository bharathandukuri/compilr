package com.bharathandukuri.compilr.language;

import com.bharathandukuri.compilr.language.exception.LanguageNotFoundException;
import com.bharathandukuri.compilr.language.factory.impl.LanguageFactoryImpl;
import com.bharathandukuri.compilr.language.registry.LanguageRegistry;
import com.bharathandukuri.compilr.language.registry.impl.LanguageRegistryImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class LanguageRegistryTest {

    private LanguageRegistry languageRegistry;

    @BeforeEach
    void setUp() {
        LanguageFactoryImpl factory = new LanguageFactoryImpl();
        LanguageRegistryImpl registry = new LanguageRegistryImpl(factory);
        registry.init();
        languageRegistry = registry;
    }

    @Test
    @DisplayName("init: registers all standard languages")
    void init_registersAllStandardLanguages() {
        List<Language> all = languageRegistry.getAll();
        assertThat(all).hasSize(16);

        assertThat(languageRegistry.isSupported("java-21")).isTrue();
        assertThat(languageRegistry.isSupported("c-17")).isTrue();
        assertThat(languageRegistry.isSupported("cpp-23")).isTrue();
        assertThat(languageRegistry.isSupported("python-3.12")).isTrue();
        assertThat(languageRegistry.isSupported("javascript-node-20")).isTrue();
        assertThat(languageRegistry.isSupported("typescript-5.4")).isTrue();
        assertThat(languageRegistry.isSupported("go-1.22")).isTrue();
        assertThat(languageRegistry.isSupported("rust-1.75")).isTrue();
        assertThat(languageRegistry.isSupported("php-8.3")).isTrue();
        assertThat(languageRegistry.isSupported("csharp-12")).isTrue();
        assertThat(languageRegistry.isSupported("kotlin-1.9")).isTrue();
        assertThat(languageRegistry.isSupported("dart-3.4")).isTrue();
        assertThat(languageRegistry.isSupported("mysql-8.0")).isTrue();
        assertThat(languageRegistry.isSupported("postgresql-16")).isTrue();
        assertThat(languageRegistry.isSupported("sqlite-3")).isTrue();
        assertThat(languageRegistry.isSupported("mongodb-8.0")).isTrue();
    }

    @Test
    @DisplayName("get: resolves by aliases")
    void get_resolvesByAliases() {
        Language java = languageRegistry.get("java");
        assertThat(java.id()).isEqualTo("java-21");

        Language python = languageRegistry.get("py");
        assertThat(python.id()).isEqualTo("python-3.12");

        Language cpp = languageRegistry.get("cpp");
        assertThat(cpp.id()).isEqualTo("cpp-23");

        Language c = languageRegistry.get("c");
        assertThat(c.id()).isEqualTo("c-17");

        Language js = languageRegistry.get("js");
        assertThat(js.id()).isEqualTo("javascript-node-20");

        Language ts = languageRegistry.get("ts");
        assertThat(ts.id()).isEqualTo("typescript-5.4");

        Language go = languageRegistry.get("golang");
        assertThat(go.id()).isEqualTo("go-1.22");

        Language rust = languageRegistry.get("rs");
        assertThat(rust.id()).isEqualTo("rust-1.75");

        Language php = languageRegistry.get("php");
        assertThat(php.id()).isEqualTo("php-8.3");

        Language csharp = languageRegistry.get("c#");
        assertThat(csharp.id()).isEqualTo("csharp-12");

        Language kotlin = languageRegistry.get("kt");
        assertThat(kotlin.id()).isEqualTo("kotlin-1.9");

        Language dart = languageRegistry.get("dart");
        assertThat(dart.id()).isEqualTo("dart-3.4");

        Language sqlite = languageRegistry.get("sqlite3");
        assertThat(sqlite.id()).isEqualTo("sqlite-3");

        Language mongo = languageRegistry.get("mongo");
        assertThat(mongo.id()).isEqualTo("mongodb-8.0");

        Language sql = languageRegistry.get("mysql");
        assertThat(sql.id()).isEqualTo("mysql-8.0");

        Language postgres = languageRegistry.get("postgresql");
        assertThat(postgres.id()).isEqualTo("postgresql-16");
    }

    @Test
    @DisplayName("get: throws LanguageNotFoundException for unknown language")
    void get_unknownThrows() {
        assertThatThrownBy(() -> languageRegistry.get("unknown-lang"))
                .isInstanceOf(LanguageNotFoundException.class);
    }

    @Test
    @DisplayName("defaultStarterCode: returns valid code snippets")
    void defaultStarterCode_returnsBoilerplate() {
        Language java = languageRegistry.get("java-21");
        assertThat(java.defaultStarterCode()).contains("class Main");

        Language python = languageRegistry.get("python-3.12");
        assertThat(python.defaultStarterCode()).contains("sys.stdin");

        Language cpp = languageRegistry.get("cpp-23");
        assertThat(cpp.defaultStarterCode()).contains("int main()");
    }
}
