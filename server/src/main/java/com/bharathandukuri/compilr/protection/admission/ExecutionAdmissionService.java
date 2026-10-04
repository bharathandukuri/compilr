package com.bharathandukuri.compilr.protection.admission;

import com.bharathandukuri.compilr.compiler.dto.ExecuteRequest;
import com.bharathandukuri.compilr.compiler.dto.ExecuteResponse;
import jakarta.servlet.http.HttpServletRequest;

public interface ExecutionAdmissionService {

    /**
     * Adimits and executes a compiler request through the complete protection pipeline:
     * Request -> Rate Limiter -> Admission Control / Cache -> Environment Queue -> Execution -> Result
     *
     * @param request compiler execution payload
     * @param httpRequest HTTP request for client token / IP resolution
     * @return execution outcome
     */
    ExecuteResponse execute(ExecuteRequest request, HttpServletRequest httpRequest);
}
