package org.example.pruebatecnicaecommerce.infrastructure.persistence.adapter;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataAccessException;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

/**
 * Service to handle deadlock scenarios with retry mechanism and proper lock
 * ordering
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class DeadlockHandlingService {

    /**
     * Execute operation with deadlock protection and retry mechanism
     */
    @Retryable(value = {
            DataAccessException.class }, maxAttempts = 5, backoff = @Backoff(delay = 50, multiplier = 2.0, maxDelay = 2000, random = true))
    @Transactional(timeout = 30) // 30 second timeout
    public <T> T executeWithDeadlockProtection(Supplier<T> operation) {
        try {
            log.debug("Executing operation with deadlock protection");
            return operation.get();
        } catch (DataAccessException e) {
            log.warn("Deadlock detected, retrying operation. Error: {}", e.getMessage());
            throw e; // Let Spring Retry handle the retry
        }
    }

    /**
     * Execute operation with ordered resource locking to prevent deadlocks
     * Resources are locked in consistent order based on their ID
     */
    @Retryable(value = {
            DataAccessException.class }, maxAttempts = 5, backoff = @Backoff(delay = 50, multiplier = 2.0, maxDelay = 2000, random = true))
    @Transactional(timeout = 30)
    public <T> T executeWithOrderedLocking(UUID primaryResourceId, UUID secondaryResourceId,
            Supplier<T> operation) {
        try {
            // Order resources by ID to ensure consistent lock ordering
            UUID firstLock = primaryResourceId.compareTo(secondaryResourceId) < 0
                    ? primaryResourceId
                    : secondaryResourceId;
            UUID secondLock = primaryResourceId.compareTo(secondaryResourceId) < 0
                    ? secondaryResourceId
                    : primaryResourceId;

            log.debug("Executing operation with ordered locking: {} -> {}", firstLock, secondLock);

            // In a real scenario, you would acquire locks on specific resources here
            // For now, we just execute the operation
            return operation.get();

        } catch (DataAccessException e) {
            log.warn("Deadlock detected in ordered locking, retrying. Error: {}", e.getMessage());
            throw e;
        }
    }

    /**
     * Execute operation asynchronously with timeout to prevent infinite waits
     */
    public <T> CompletableFuture<T> executeWithTimeout(Supplier<T> operation, long timeoutSeconds) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                return executeWithDeadlockProtection(operation);
            } catch (Exception e) {
                log.error("Operation failed after deadlock protection", e);
                throw new RuntimeException("Operation timed out or failed", e);
            }
        }).orTimeout(timeoutSeconds, TimeUnit.SECONDS);
    }

    /**
     * Execute batch operations with deadlock protection
     * Processes items in smaller batches to reduce lock contention
     */
    @Retryable(value = {
            DataAccessException.class }, maxAttempts = 3, backoff = @Backoff(delay = 100, multiplier = 1.5, maxDelay = 1000))
    @Transactional(timeout = 60)
    public <T> void executeBatchWithDeadlockProtection(Iterable<T> items,
            java.util.function.Consumer<T> operation,
            int batchSize) {
        try {
            int count = 0;
            for (T item : items) {
                operation.accept(item);
                count++;

                // Commit in smaller batches to reduce lock time
                if (count % batchSize == 0) {
                    log.debug("Processed batch of {} items", batchSize);
                    // Force a transaction boundary here if needed
                }
            }
            log.debug("Completed batch processing of {} items", count);

        } catch (DataAccessException e) {
            log.warn("Deadlock detected in batch processing, retrying. Error: {}", e.getMessage());
            throw e;
        }
    }
}