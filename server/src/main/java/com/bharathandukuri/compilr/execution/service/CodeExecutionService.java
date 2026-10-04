package com.bharathandukuri.compilr.execution.service;

import com.bharathandukuri.compilr.execution.dto.request.SimpleCodeExecutionRequest;
import com.bharathandukuri.compilr.execution.dto.response.SimpleCodeExecutionResult;

public interface CodeExecutionService {
    SimpleCodeExecutionResult run(SimpleCodeExecutionRequest request);
}
