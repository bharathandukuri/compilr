package com.bharathandukuri.compilr.protection.queue;

import com.bharathandukuri.compilr.protection.config.ProtectionProperties;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class EnvironmentExecutionQueueTest {

    private GlobalCapacityTracker globalCapacityTracker;
    private EnvironmentExecutionQueue queue;

    @BeforeEach
    void setUp() {
        ProtectionProperties properties = new ProtectionProperties();
        properties.getQueue().setGlobalMaxProcessing(10);
        properties.getQueue().setGlobalMaxQueued(20);
        globalCapacityTracker = new GlobalCapacityTracker(properties);

        // processingCapacity = 2, queueCapacity = 2, timeout = 5000ms
        queue = new EnvironmentExecutionQueue(
                "test-env",
                2,
                2,
                5000L,
                globalCapacityTracker
        );
    }

    @AfterEach
    void tearDown() {
        if (queue != null) {
            queue.shutdown();
        }
    }

    @Test
    @DisplayName("Executes tasks concurrently up to processing capacity")
    void executesConcurrentlyUpToCapacity() throws Exception {
        CountDownLatch startLatch = new CountDownLatch(1);
        CountDownLatch runningLatch = new CountDownLatch(2);

        CompletableFuture<String> task1 = queue.submit(() -> {
            runningLatch.countDown();
            startLatch.await(3, TimeUnit.SECONDS);
            return "res-1";
        });

        CompletableFuture<String> task2 = queue.submit(() -> {
            runningLatch.countDown();
            startLatch.await(3, TimeUnit.SECONDS);
            return "res-2";
        });

        // Verify both tasks are running concurrently
        boolean bothRunning = runningLatch.await(2, TimeUnit.SECONDS);
        assertThat(bothRunning).isTrue();
        assertThat(queue.getActiveCount()).isEqualTo(2);

        startLatch.countDown();

        assertThat(task1.get(2, TimeUnit.SECONDS)).isEqualTo("res-1");
        assertThat(task2.get(2, TimeUnit.SECONDS)).isEqualTo("res-2");
    }

    @Test
    @DisplayName("Buffers tasks in queue when processing capacity is saturated, and rejects when queue is full")
    void buffersInQueueAndRejectsWhenFull() throws Exception {
        CountDownLatch blockLatch = new CountDownLatch(1);

        // Fill processing capacity (2 tasks)
        CompletableFuture<String> p1 = queue.submit(() -> {
            blockLatch.await(3, TimeUnit.SECONDS);
            return "p1";
        });
        CompletableFuture<String> p2 = queue.submit(() -> {
            blockLatch.await(3, TimeUnit.SECONDS);
            return "p2";
        });

        // Fill queue capacity (2 tasks buffered in queue)
        CompletableFuture<String> q1 = queue.submit(() -> "q1");
        CompletableFuture<String> q2 = queue.submit(() -> "q2");

        // Attempt 5th task -> must immediately reject because queue is full!
        assertThatThrownBy(() -> queue.submit(() -> "overflow"))
                .isInstanceOf(CapacityUnavailableException.class)
                .hasMessageContaining("Execution queue for environment [test-env] is full (2 capacity)");

        // Unblock workers
        blockLatch.countDown();

        assertThat(p1.get(2, TimeUnit.SECONDS)).isEqualTo("p1");
        assertThat(p2.get(2, TimeUnit.SECONDS)).isEqualTo("p2");
        assertThat(q1.get(2, TimeUnit.SECONDS)).isEqualTo("q1");
        assertThat(q2.get(2, TimeUnit.SECONDS)).isEqualTo("q2");
    }

    @Test
    @DisplayName("Executes queued tasks in strict FIFO order")
    void executesInFifoOrder() throws Exception {
        CountDownLatch blockLatch = new CountDownLatch(1);
        List<Integer> executionOrder = Collections.synchronizedList(new ArrayList<>());

        // Saturation task
        queue.submit(() -> {
            blockLatch.await(3, TimeUnit.SECONDS);
            return 0;
        });
        queue.submit(() -> {
            blockLatch.await(3, TimeUnit.SECONDS);
            return 0;
        });

        // Submit 2 queued tasks
        CompletableFuture<Integer> f1 = queue.submit(() -> {
            executionOrder.add(1);
            return 1;
        });
        CompletableFuture<Integer> f2 = queue.submit(() -> {
            executionOrder.add(2);
            return 2;
        });

        blockLatch.countDown();

        f1.get(2, TimeUnit.SECONDS);
        f2.get(2, TimeUnit.SECONDS);

        assertThat(executionOrder).containsExactly(1, 2);
    }

    @Test
    @DisplayName("Guarantees zero permit or slot leakage when a task throws an exception")
    void releasesPermitsOnTaskFailure() throws Exception {
        CountDownLatch latch = new CountDownLatch(1);

        CompletableFuture<Void> failingTask = queue.submit(() -> {
            latch.countDown();
            throw new RuntimeException("Simulated task explosion");
        });

        assertThatThrownBy(() -> failingTask.get(2, TimeUnit.SECONDS))
                .isInstanceOf(ExecutionException.class)
                .hasCauseInstanceOf(RuntimeException.class);

        // Wait brief moment for worker thread finally block to finish
        Thread.sleep(100);

        // Verify permits are completely intact
        assertThat(globalCapacityTracker.getActiveProcessingCount()).isEqualTo(0);
        assertThat(globalCapacityTracker.getQueuedCount()).isEqualTo(0);

        // Should be able to process new tasks cleanly
        CompletableFuture<String> subsequentTask = queue.submit(() -> "success-after-failure");
        assertThat(subsequentTask.get(2, TimeUnit.SECONDS)).isEqualTo("success-after-failure");
    }

    @Test
    @DisplayName("Fails with ExecutionQueueTimeoutException when task waits longer than maxQueueWaitTimeoutMs")
    void timesOutQueuedTask() {
        EnvironmentExecutionQueue shortTimeoutQueue = new EnvironmentExecutionQueue(
                "short-timeout-env",
                1,
                2,
                150L, // 150ms timeout
                globalCapacityTracker
        );

        try {
            CountDownLatch blockingLatch = new CountDownLatch(1);

            // Block worker for 300ms
            shortTimeoutQueue.submit(() -> {
                blockingLatch.await(300, TimeUnit.MILLISECONDS);
                return "blocker";
            });

            // This queued task will wait > 150ms
            CompletableFuture<String> queuedTask = shortTimeoutQueue.submit(() -> "should-timeout");

            assertThatThrownBy(() -> queuedTask.get(2, TimeUnit.SECONDS))
                    .isInstanceOf(ExecutionException.class)
                    .hasCauseInstanceOf(ExecutionQueueTimeoutException.class);
        } finally {
            shortTimeoutQueue.shutdown();
        }
    }
}
