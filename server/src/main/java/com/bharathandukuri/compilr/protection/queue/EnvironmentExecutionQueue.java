package com.bharathandukuri.compilr.protection.queue;

import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.Callable;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

@Slf4j
public class EnvironmentExecutionQueue {

    private final String environmentId;
    private final int processingCapacity;
    private final int queueCapacity;
    private final long maxQueueWaitTimeoutMs;
    private final GlobalCapacityTracker globalCapacityTracker;
    private final ThreadPoolExecutor executor;
    private final LinkedBlockingQueue<Runnable> workQueue;
    private final AtomicInteger threadCounter = new AtomicInteger(1);

    public EnvironmentExecutionQueue(
            String environmentId,
            int processingCapacity,
            int queueCapacity,
            long maxQueueWaitTimeoutMs,
            GlobalCapacityTracker globalCapacityTracker
    ) {
        this.environmentId = environmentId;
        this.processingCapacity = Math.max(1, processingCapacity);
        this.queueCapacity = Math.max(1, queueCapacity);
        this.maxQueueWaitTimeoutMs = maxQueueWaitTimeoutMs;
        this.globalCapacityTracker = globalCapacityTracker;

        this.workQueue = new LinkedBlockingQueue<>(this.queueCapacity);
        ThreadFactory threadFactory = runnable -> {
            Thread thread = new Thread(runnable, "compilr-env-" + environmentId + "-" + threadCounter.getAndIncrement());
            thread.setDaemon(true);
            return thread;
        };

        this.executor = new ThreadPoolExecutor(
                this.processingCapacity,
                this.processingCapacity,
                0L,
                TimeUnit.MILLISECONDS,
                this.workQueue,
                threadFactory,
                new ThreadPoolExecutor.AbortPolicy()
        );

        log.info("Initialized execution queue for environment [{}]: processingCapacity={}, queueCapacity={}, maxQueueWaitTimeoutMs={}",
                environmentId, this.processingCapacity, this.queueCapacity, this.maxQueueWaitTimeoutMs);
    }

    public <T> CompletableFuture<T> submit(Callable<T> callable) {
        // Step 1: Check and reserve global queue slot
        if (!globalCapacityTracker.tryReserveQueueSlot()) {
            log.warn("Global queue capacity reached while admitting request for environment [{}] (max: {})",
                    environmentId, globalCapacityTracker.getGlobalMaxQueued());
            throw CapacityUnavailableException.globalQueueLimitReached(globalCapacityTracker.getGlobalMaxQueued());
        }

        CompletableFuture<T> future = new CompletableFuture<>();
        long enqueueTime = System.currentTimeMillis();
        AtomicBoolean dequeuedOrCleaned = new AtomicBoolean(false);

        Runnable workerTask = () -> {
            // Task has been dequeued by a worker thread
            if (dequeuedOrCleaned.compareAndSet(false, true)) {
                globalCapacityTracker.releaseQueueSlot();
            }

            if (future.isCancelled()) {
                return;
            }

            long queueWaitTimeMs = System.currentTimeMillis() - enqueueTime;
            if (queueWaitTimeMs > maxQueueWaitTimeoutMs) {
                log.warn("Request in environment [{}] timed out waiting in queue (waited {}ms, max {}ms)",
                        environmentId, queueWaitTimeMs, maxQueueWaitTimeoutMs);
                future.completeExceptionally(new ExecutionQueueTimeoutException(environmentId, queueWaitTimeMs));
                return;
            }

            boolean acquiredProcessingSlot = false;
            try {
                acquiredProcessingSlot = globalCapacityTracker.tryAcquireProcessingSlot(maxQueueWaitTimeoutMs);
                if (!acquiredProcessingSlot) {
                    log.warn("Global processing capacity exceeded for environment [{}] (max: {})",
                            environmentId, globalCapacityTracker.getGlobalMaxProcessing());
                    future.completeExceptionally(
                            CapacityUnavailableException.globalCapacityReached(globalCapacityTracker.getGlobalMaxProcessing())
                    );
                    return;
                }

                T result = callable.call();
                future.complete(result);
            } catch (Throwable t) {
                future.completeExceptionally(t);
            } finally {
                if (acquiredProcessingSlot) {
                    globalCapacityTracker.releaseProcessingSlot();
                }
            }
        };

        try {
            executor.execute(workerTask);
        } catch (RejectedExecutionException e) {
            // Environment-specific queue was full!
            if (dequeuedOrCleaned.compareAndSet(false, true)) {
                globalCapacityTracker.releaseQueueSlot();
            }
            log.warn("Execution queue full for environment [{}] (processing: {}/{}, queue: {}/{})",
                    environmentId, executor.getActiveCount(), processingCapacity, workQueue.size(), queueCapacity);
            throw CapacityUnavailableException.environmentQueueFull(environmentId, queueCapacity);
        }

        // Handle early cancellation if future cancelled while waiting in queue
        future.whenComplete((res, ex) -> {
            if (future.isCancelled()) {
                workQueue.remove(workerTask);
                if (dequeuedOrCleaned.compareAndSet(false, true)) {
                    globalCapacityTracker.releaseQueueSlot();
                }
            }
        });

        return future;
    }

    public void shutdown() {
        executor.shutdown();
        try {
            if (!executor.awaitTermination(3, TimeUnit.SECONDS)) {
                executor.shutdownNow();
            }
        } catch (InterruptedException e) {
            executor.shutdownNow();
            Thread.currentThread().interrupt();
        }
    }

    public String getEnvironmentId() {
        return environmentId;
    }

    public int getProcessingCapacity() {
        return processingCapacity;
    }

    public int getQueueCapacity() {
        return queueCapacity;
    }

    public int getActiveCount() {
        return executor.getActiveCount();
    }

    public int getQueuedCount() {
        return workQueue.size();
    }
}
