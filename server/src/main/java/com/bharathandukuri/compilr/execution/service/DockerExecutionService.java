package com.bharathandukuri.compilr.execution.service;

import com.bharathandukuri.compilr.execution.dto.DatabaseContainerConstraints;
import com.bharathandukuri.compilr.execution.dto.DockerContainerDetails;
import com.bharathandukuri.compilr.execution.dto.DockerImageDetails;
import com.bharathandukuri.compilr.execution.dto.response.DockerExecutionResult;
import com.bharathandukuri.compilr.execution.exception.*;

import java.util.List;

public interface DockerExecutionService {
    boolean isImageExists(DockerImageDetails dockerImageDetails);

    default boolean isImageExists(com.bharathandukuri.compilr.execution.registry.DockerImageRegistry registry) {
        return registry != null && isImageExists(registry.dockerImage());
    }

    void createImage(DockerImageDetails dockerImageDetails) throws DockerImageCreationException;

    DockerContainerDetails createContainer(DockerImageDetails dockerImageDetails)
            throws DockerContainerCreationException;

    DockerContainerDetails createContainer(
            DockerImageDetails dockerImageDetails,
            DatabaseContainerConstraints constraints
    ) throws DockerContainerCreationException;

    default DockerContainerDetails createContainer(com.bharathandukuri.compilr.execution.registry.DockerImageRegistry registry)
            throws DockerContainerCreationException {
        if (registry == null) {
            throw new DockerContainerCreationException("DockerImageRegistry must not be null.");
        }
        return createContainer(registry.dockerImage());
    }

    default DockerContainerDetails createContainer(
            com.bharathandukuri.compilr.execution.registry.DockerImageRegistry registry,
            DatabaseContainerConstraints constraints
    ) throws DockerContainerCreationException {
        if (registry == null) {
            throw new DockerContainerCreationException("DockerImageRegistry must not be null.");
        }
        return createContainer(registry.dockerImage(), constraints);
    }

    void deleteContainer(String containerId) throws DockerContainerDeletionException;

    boolean isContainerExists(String containerId);

    boolean isContainerRunning(String containerId);

    String getContainerLogs(String containerId, int tailLines);

    void startContainer(String containerId) throws DockerContainerNotFoundException;

    void stopContainer(String containerId)
            throws DockerContainerStopException;

    DockerExecutionResult execContainer(String containerId, List<String> command) throws DockerExecutionException;

    DockerExecutionResult execContainer(
            String containerId,
            List<String> command,
            Long timeLimitMs
    ) throws DockerExecutionException;

    String readFile(
            String containerId,
            String path
    ) throws DockerExecutionException;

    void writeFile(
            String containerId,
            String path,
            String content
    ) throws DockerExecutionException;
}
