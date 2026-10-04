package com.bharathandukuri.compilr.environment;

import com.bharathandukuri.compilr.protection.config.ProtectionProperties;
import com.bharathandukuri.compilr.protection.queue.CapacityUnavailableException;
import com.bharathandukuri.compilr.protection.queue.EnvironmentExecutionQueue;
import com.bharathandukuri.compilr.protection.queue.ExecutionQueueManager;
import com.bharathandukuri.compilr.protection.queue.ExecutionQueueTimeoutException;
import com.bharathandukuri.compilr.protection.queue.GlobalCapacityTracker;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("Execution Queue and Concurrency Integration Tests")
class ExecutionQueueConcurrencyIntegrationTest extends BaseEnvironmentTest {

    @Autowired
    private ExecutionQueueManager queueManager;

    @Autowired
    private ProtectionProperties protectionProperties;

    @AfterEach
    void tearDown() {
        // Ensure clean state
    }

    @Test
    @DisplayName("Worker saturation and queueing: tasks queue when workers are full and process upon release")
    void workerSaturationAndQueueing() throws Exception {
        // Create an isolated environment queue with 2 workers and 3 queue slots
        GlobalCapacityTracker tracker = queueManager.getGlobalCapacityTracker();
        EnvironmentExecutionQueue queue = new EnvironmentExecutionQueue(
                "saturation-test-env",
                2,
                3,
                5000L,
                tracker
        );

        try {
            CountDownLatch workersBlock = new CountDownLatch(1);
            List<String> finished = Collections.synchronizedList(new ArrayList<>());

            // Task 1 & 2 fill the 2 processing workers
            CompletableFuture<String> t1 = queue.submit(() -> {
                workersBlock.await(5, TimeUnit.SECONDS);
                finished.add("worker-1");
                return "w1";
            });

            CompletableFuture<String> t2 = queue.submit(() -> {
                workersBlock.await(5, TimeUnit.SECONDS);
                finished.add("worker-2");
                return "w2";
            });

            // Task 3 queues up in the work queue
            CompletableFuture<String> t3 = queue.submit(() -> {
                finished.add("queued-1");
                return "q1";
            });

            // At this point, t3 should not be done yet because workers are blocked
            assertThat(t3.isDone()).isFalse();

            // Release the workers
            workersBlock.countDown();

            String r1 = t1.get(3, TimeUnit.SECONDS);
            String r2 = t2.get(3, TimeUnit.SECONDS);
            String r3 = t3.get(3, TimeUnit.SECONDS);

            assertThat(r1).isEqualTo("w1");
            assertThat(r2).isEqualTo("w2");
            assertThat(r3).isEqualTo("q1");
            assertThat(finished).contains("worker-1", "worker-2", "queued-1");
        } finally {
            queue.shutdown();
        }
    }

    @Test
    @DisplayName("Queue capacity overflow: immediately rejects with CapacityUnavailableException (503)")
    void queueCapacityRejection() throws Exception {
        GlobalCapacityTracker tracker = queueManager.getGlobalCapacityTracker();
        // 1 processing capacity, 1 queue capacity
        EnvironmentExecutionQueue queue = new EnvironmentExecutionQueue(
                "overflow-test-env",
                1,
                1,
                5000L,
                tracker
        );

        try {
            CountDownLatch blockLatch = new CountDownLatch(1);

            // Task 1 occupies the worker
            CompletableFuture<Void> t1 = queue.submit(() -> {
                blockLatch.await(5, TimeUnit.SECONDS);
                return null;
            });

            // Task 2 occupies the 1 available queue slot
            CompletableFuture<Void> t2 = queue.submit(() -> {
                blockLatch.await(5, TimeUnit.SECONDS);
                return null;
            });

            // Task 3 exceeds capacity and MUST be rejected immediately
            assertThatThrownBy(() -> queue.submit(() -> "rejected"))
                    .isInstanceOf(CapacityUnavailableException.class)
                    .hasMessageContaining("Execution queue for environment [overflow-test-env] is full");

            blockLatch.countDown();
            t1.get(2, TimeUnit.SECONDS);
            t2.get(2, TimeUnit.SECONDS);
        } finally {
            queue.shutdown();
        }
    }

    @Test
    @DisplayName("Queue timeout: request waiting longer than max timeout fails with ExecutionQueueTimeoutException (504)")
    void queueWaitTimeout() throws Exception {
        GlobalCapacityTracker tracker = queueManager.getGlobalCapacityTracker();
        // 1 worker, 2 queue capacity, 150ms timeout
        EnvironmentExecutionQueue queue = new EnvironmentExecutionQueue(
                "timeout-test-env",
                1,
                2,
                150L,
                tracker
        );

        try {
            CountDownLatch blockLatch = new CountDownLatch(1);

            // Occupy worker for 400ms
            queue.submit(() -> {
                blockLatch.await(500, TimeUnit.MILLISECONDS);
                return "done";
            });

            // Queued task will wait > 150ms
            CompletableFuture<String> queuedTask = queue.submit(() -> "never-run");

            // Queued task must fail with ExecutionQueueTimeoutException
            assertThatThrownBy(() -> queuedTask.get(3, TimeUnit.SECONDS))
                    .isInstanceOf(ExecutionException.class)
                    .hasCauseInstanceOf(ExecutionQueueTimeoutException.class);

            blockLatch.countDown();
        } finally {
            queue.shutdown();
        }
    }

    @Test
    @DisplayName("Parallel multi-environment isolation: distinct environments execute concurrently")
    void multiEnvironmentConcurrency() throws Exception {
        int envCount = 4;
        ExecutorService executor = Executors.newFixedThreadPool(envCount);
        CountDownLatch startGate = new CountDownLatch(1);
        CountDownLatch endGate = new CountDownLatch(envCount);

        AtomicInteger successCount = new AtomicInteger(0);

        for (int i = 0; i < envCount; i++) {
            final String envName = "multi-env-" + i;
            executor.submit(() -> {
                try {
                    startGate.await();
                    CompletableFuture<String> future = queueManager.submit(envName, () -> {
                        Thread.sleep(50);
                        return envName + "-SUCCESS";
                    });
                    String res = future.get(5, TimeUnit.SECONDS);
                    if (res.contains("SUCCESS")) {
                        successCount.incrementAndGet();
                    }
                } catch (Exception ignored) {
                } finally {
                    endGate.countDown();
                }
            });
        }

        startGate.countDown();
        boolean completed = endGate.await(10, TimeUnit.SECONDS);
        executor.shutdown();

        assertThat(completed).isTrue();
        assertThat(successCount.get()).isEqualTo(envCount);
    }
}
