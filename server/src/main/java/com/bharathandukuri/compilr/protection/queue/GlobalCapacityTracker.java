package com.bharathandukuri.compilr.protection.queue;

import com.bharathandukuri.compilr.protection.config.ProtectionProperties;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

@Slf4j
@Component
public class GlobalCapacityTracker {

    private final int globalMaxProcessing;
    private final int globalMaxQueued;
    private final Semaphore processingSemaphore;
    private final AtomicInteger queuedCount = new AtomicInteger(0);

    public GlobalCapacityTracker(ProtectionProperties properties) {
        this.globalMaxProcessing = Math.max(1, properties.getQueue().getGlobalMaxProcessing());
        this.globalMaxQueued = Math.max(1, properties.getQueue().getGlobalMaxQueued());
        this.processingSemaphore = new Semaphore(this.globalMaxProcessing, true); // fair semaphore
        log.info("Initialized GlobalCapacityTracker: globalMaxProcessing={}, globalMaxQueued={}",
                this.globalMaxProcessing, this.globalMaxQueued);
    }

    /**
     * Attempts to reserve a global queue slot.
     * Returns true if slot successfully reserved, false if global queue capacity is reached.
     */
    public boolean tryReserveQueueSlot() {
        while (true) {
            int current = queuedCount.get();
            if (current >= globalMaxQueued) {
                return false;
            }
            if (queuedCount.compareAndSet(current, current + 1)) {
                return true;
            }
        }
    }

    /**
     * Decrements the global queued count when a task is dequeued or rejected.
     */
    public void releaseQueueSlot() {
        int updated = queuedCount.decrementAndGet();
        if (updated < 0) {
            queuedCount.set(0);
        }
    }

    /**
     * Tries to acquire a global processing slot.
     */
    public boolean tryAcquireProcessingSlot(long timeoutMs) throws InterruptedException {
        if (timeoutMs <= 0) {
            return processingSemaphore.tryAcquire();
        }
        return processingSemaphore.tryAcquire(timeoutMs, TimeUnit.MILLISECONDS);
    }

    /**
     * Releases an acquired global processing slot.
     */
    public void releaseProcessingSlot() {
        processingSemaphore.release();
    }

    public int getActiveProcessingCount() {
        return globalMaxProcessing - processingSemaphore.availablePermits();
    }

    public int getQueuedCount() {
        return Math.max(0, queuedCount.get());
    }

    public int getGlobalMaxProcessing() {
        return globalMaxProcessing;
    }

    public int getGlobalMaxQueued() {
        return globalMaxQueued;
    }
}
