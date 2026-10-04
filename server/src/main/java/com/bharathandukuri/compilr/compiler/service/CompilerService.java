package com.bharathandukuri.compilr.compiler.service;

import com.bharathandukuri.compilr.compiler.dto.ExecuteRequest;
import com.bharathandukuri.compilr.compiler.dto.ExecuteResponse;
import com.bharathandukuri.compilr.compiler.dto.LanguageResponse;

import java.util.List;

public interface CompilerService {

    ExecuteResponse execute(ExecuteRequest request);

    List<LanguageResponse> getSupportedLanguages();

    LanguageResponse getLanguage(String languageId);
}
