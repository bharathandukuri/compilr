package com.bharathandukuri.compilr.protection.queue;

import com.bharathandukuri.compilr.protection.config.ProtectionProperties;
import jakarta.annotation.PreDestroy;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Component
@RequiredArgsConstructor
public class ExecutionQueueManager {

    private final ProtectionProperties protectionProperties;
    private final GlobalCapacityTracker globalCapacityTracker;
    private final ConcurrentHashMap<String, EnvironmentExecutionQueue> queues = new ConcurrentHashMap<>();

    public <T> CompletableFuture<T> submit(String environmentId, Callable<T> callable) {
        String envKey = normalizeEnvironmentKey(environmentId);
        EnvironmentExecutionQueue queue = getQueue(envKey);
        return queue.submit(callable);
    }

    public EnvironmentExecutionQueue getQueue(String environmentId) {
        String envKey = normalizeEnvironmentKey(environmentId);
        return queues.computeIfAbsent(envKey, this::createEnvironmentQueue);
    }

    public Map<String, EnvironmentExecutionQueue> getAllQueues() {
        return Collections.unmodifiableMap(queues);
    }

    public GlobalCapacityTracker getGlobalCapacityTracker() {
        return globalCapacityTracker;
    }

    private EnvironmentExecutionQueue createEnvironmentQueue(String envKey) {
        ProtectionProperties.QueueProperties queueConfig = protectionProperties.getQueue();

        int processingCapacity = queueConfig.getDefaultProcessingCapacity();
        int queueCapacity = queueConfig.getDefaultQueueCapacity();
        long maxWaitTimeoutMs = queueConfig.getMaxQueueWaitTimeoutMs();

        // Check for per-environment capacity overrides in configuration
        if (queueConfig.getEnvironments() != null && queueConfig.getEnvironments().containsKey(envKey)) {
            ProtectionProperties.EnvironmentCapacity override = queueConfig.getEnvironments().get(envKey);
            if (override.getProcessingCapacity() != null && override.getProcessingCapacity() > 0) {
                processingCapacity = override.getProcessingCapacity();
            }
            if (override.getQueueCapacity() != null && override.getQueueCapacity() > 0) {
                queueCapacity = override.getQueueCapacity();
            }
        }

        return new EnvironmentExecutionQueue(
                envKey,
                processingCapacity,
                queueCapacity,
                maxWaitTimeoutMs,
                globalCapacityTracker
        );
    }

    private String normalizeEnvironmentKey(String environmentId) {
        if (environmentId == null || environmentId.isBlank()) {
            return "default";
        }
        return environmentId.trim().toLowerCase();
    }

    @PreDestroy
    public void shutdown() {
        log.info("Shutting down all environment execution queues ({} active environments)...", queues.size());
        for (EnvironmentExecutionQueue queue : queues.values()) {
            try {
                queue.shutdown();
            } catch (Exception e) {
                log.warn("Error shutting down execution queue for [{}]: {}", queue.getEnvironmentId(), e.getMessage());
            }
        }
        queues.clear();
    }
}
