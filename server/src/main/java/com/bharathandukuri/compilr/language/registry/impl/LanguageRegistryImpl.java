package com.bharathandukuri.compilr.language.registry.impl;

import com.bharathandukuri.compilr.language.CompiledLanguage;
import com.bharathandukuri.compilr.language.DatabaseLanguage;
import com.bharathandukuri.compilr.language.InterpretedLanguage;
import com.bharathandukuri.compilr.language.Language;
import com.bharathandukuri.compilr.language.LanguageType;
import com.bharathandukuri.compilr.language.exception.LanguageNotFoundException;
import com.bharathandukuri.compilr.language.exception.UnsupportedLanguageTypeException;
import com.bharathandukuri.compilr.language.factory.LanguageFactory;
import com.bharathandukuri.compilr.language.registry.LanguageRegistry;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Service
@RequiredArgsConstructor
public class LanguageRegistryImpl implements LanguageRegistry {

    private final LanguageFactory languageFactory;
    private final Map<String, Language> registry = new ConcurrentHashMap<>();

    @PostConstruct
    public void init() {
        for (String id : languageFactory.supportedLanguageIds()) {
            try {
                Language language = languageFactory.create(id);
                register(language);
            } catch (Exception e) {
                log.error("Failed to initialize built-in language [{}] in registry: {}", id, e.getMessage(), e);
            }
        }
        log.info("Initialized LanguageRegistry with {} language(s): {}", registry.size(), registry.keySet());
    }

    @Override
    public void register(Language language) {
        Objects.requireNonNull(language, "Language must not be null");
        Objects.requireNonNull(language.id(), "Language id must not be null");

        String normalizedId = normalizeId(language.id());
        registry.put(normalizedId, language);
        log.debug("Registered language [{}] of type [{}]", normalizedId, language.type());
    }

    @Override
    public void unregister(String id) {
        if (id == null || id.isBlank()) {
            return;
        }
        registry.remove(normalizeId(id));
        log.debug("Unregistered language [{}]", id);
    }

    @Override
    public Language get(String id) {
        if (id == null || id.isBlank()) {
            throw new LanguageNotFoundException("Language ID must not be null or blank.");
        }

        String normalized = normalizeId(id);
        Language language = registry.get(normalized);
        if (language != null) {
            return language;
        }

        // Fallback to checking language aliases
        for (Language lang : registry.values()) {
            if (lang.aliases() != null && lang.aliases().contains(normalized)) {
                return lang;
            }
        }

        throw new LanguageNotFoundException(id);
    }

    @Override
    public CompiledLanguage getCompiled(String id) {
        Language language = get(id);
        if (language instanceof CompiledLanguage compiledLanguage) {
            return compiledLanguage;
        }
        throw new UnsupportedLanguageTypeException(id, LanguageType.COMPILED, language.type());
    }

    @Override
    public InterpretedLanguage getInterpreted(String id) {
        Language language = get(id);
        if (language instanceof InterpretedLanguage interpretedLanguage) {
            return interpretedLanguage;
        }
        throw new UnsupportedLanguageTypeException(id, LanguageType.INTERPRETED, language.type());
    }

    @Override
    public DatabaseLanguage getDatabase(String id) {
        Language language = get(id);
        if (language instanceof DatabaseLanguage databaseLanguage) {
            return databaseLanguage;
        }
        throw new UnsupportedLanguageTypeException(id, LanguageType.DATABASE, language.type());
    }

    @Override
    public List<Language> getAll() {
        return List.copyOf(registry.values());
    }

    @Override
    public List<Language> getAll(LanguageType type) {
        Objects.requireNonNull(type, "LanguageType must not be null");
        return registry.values()
                .stream()
                .filter(lang -> lang.type() == type)
                .toList();
    }

    @Override
    public boolean isSupported(String id) {
        if (id == null || id.isBlank()) {
            return false;
        }
        String normalized = normalizeId(id);
        if (registry.containsKey(normalized)) {
            return true;
        }
        return registry.values().stream()
                .anyMatch(lang -> lang.aliases() != null && lang.aliases().contains(normalized));
    }

    private String normalizeId(String id) {
        return id.trim().toLowerCase();
    }
}
