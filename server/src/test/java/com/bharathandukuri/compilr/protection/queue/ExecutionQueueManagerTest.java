package com.bharathandukuri.compilr.protection.queue;

import com.bharathandukuri.compilr.protection.config.ProtectionProperties;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ExecutionQueueManagerTest {

    private ProtectionProperties properties;
    private GlobalCapacityTracker globalCapacityTracker;
    private ExecutionQueueManager manager;

    @BeforeEach
    void setUp() {
        properties = new ProtectionProperties();
        properties.getQueue().setDefaultProcessingCapacity(1);
        properties.getQueue().setDefaultQueueCapacity(1);
        properties.getQueue().setMaxQueueWaitTimeoutMs(5000L);
        properties.getQueue().setGlobalMaxProcessing(10);
        properties.getQueue().setGlobalMaxQueued(20);

        // Per-environment override for python-3.12: processing=2, queue=3
        ProtectionProperties.EnvironmentCapacity pythonOverride = new ProtectionProperties.EnvironmentCapacity();
        pythonOverride.setProcessingCapacity(2);
        pythonOverride.setQueueCapacity(3);
        properties.getQueue().getEnvironments().put("python-3.12", pythonOverride);

        globalCapacityTracker = new GlobalCapacityTracker(properties);
        manager = new ExecutionQueueManager(properties, globalCapacityTracker);
    }

    @AfterEach
    void tearDown() {
        if (manager != null) {
            manager.shutdown();
        }
    }

    @Test
    @DisplayName("Treats execution environments independently (Environment Isolation)")
    void isolatesEnvironments() throws Exception {
        CountDownLatch javaBlockLatch = new CountDownLatch(1);

        // 1. Fill Java environment (processing=1, queue=1)
        manager.submit("java-21", () -> {
            javaBlockLatch.await(3, TimeUnit.SECONDS);
            return "java-1";
        });
        manager.submit("java-21", () -> "java-2");

        // Java queue is now full: next Java task must be rejected!
        assertThatThrownBy(() -> manager.submit("java-21", () -> "java-overflow"))
                .isInstanceOf(CapacityUnavailableException.class)
                .hasMessageContaining("Execution queue for environment [java-21] is full");

        // 2. Python environment MUST STILL ACCEPT requests normally!
        CompletableFuture<String> pythonTask = manager.submit("python-3.12", () -> "python-hello");
        assertThat(pythonTask.get(2, TimeUnit.SECONDS)).isEqualTo("python-hello");

        // Unblock Java
        javaBlockLatch.countDown();
    }

    @Test
    @DisplayName("Applies per-environment configuration overrides correctly")
    void appliesOverrides() {
        EnvironmentExecutionQueue defaultQueue = manager.getQueue("cpp-23");
        assertThat(defaultQueue.getProcessingCapacity()).isEqualTo(1);
        assertThat(defaultQueue.getQueueCapacity()).isEqualTo(1);

        EnvironmentExecutionQueue pythonQueue = manager.getQueue("python-3.12");
        assertThat(pythonQueue.getProcessingCapacity()).isEqualTo(2);
        assertThat(pythonQueue.getQueueCapacity()).isEqualTo(3);
    }
}
