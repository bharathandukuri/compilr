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
        assertThat(all).hasSize(7);

        assertThat(languageRegistry.isSupported("java-21")).isTrue();
        assertThat(languageRegistry.isSupported("c-17")).isTrue();
        assertThat(languageRegistry.isSupported("cpp-23")).isTrue();
        assertThat(languageRegistry.isSupported("python-3.12")).isTrue();
        assertThat(languageRegistry.isSupported("javascript-node-20")).isTrue();
        assertThat(languageRegistry.isSupported("mysql-8.0")).isTrue();
        assertThat(languageRegistry.isSupported("postgresql-16")).isTrue();
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

        Language sql = languageRegistry.get("sql");
        assertThat(sql.id()).isEqualTo("mysql-8.0");

        Language postgres = languageRegistry.get("psql");
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
        assertThat(java.defaultStarterCode()).contains("class Solution");

        Language python = languageRegistry.get("python-3.12");
        assertThat(python.defaultStarterCode()).contains("sys.stdin");

        Language cpp = languageRegistry.get("cpp-23");
        assertThat(cpp.defaultStarterCode()).contains("int main()");
    }
}
