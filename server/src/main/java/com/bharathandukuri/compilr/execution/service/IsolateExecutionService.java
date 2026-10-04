package com.bharathandukuri.compilr.execution.service;

import com.bharathandukuri.compilr.execution.exception.IsolateCleanupException;
import com.bharathandukuri.compilr.execution.exception.IsolateExecutionException;
import com.bharathandukuri.compilr.execution.exception.IsolateInitializationException;
import com.bharathandukuri.compilr.execution.dto.DockerContainerDetails;
import com.bharathandukuri.compilr.execution.dto.IsolateExecutionConstraints;
import com.bharathandukuri.compilr.execution.dto.response.IsolateExecutionResult;
import com.bharathandukuri.compilr.execution.dto.IsolateSandBoxDetails;

import java.util.List;

public interface IsolateExecutionService {

    IsolateSandBoxDetails initialize(DockerContainerDetails dockerContainer) throws IsolateInitializationException;

    void cleanup(IsolateSandBoxDetails sandboxDetailsIsolate) throws IsolateCleanupException;

    IsolateExecutionResult executeWithConstraints(
            IsolateSandBoxDetails sandboxDetailsIsolate,
            List<String> command,
            String stdin,
            IsolateExecutionConstraints executionConstraints
    ) throws IsolateExecutionException;
}
