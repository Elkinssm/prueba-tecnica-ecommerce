package org.example.pruebatecnicaecommerce.infrastructure.persistence.adapter;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.pruebatecnicaecommerce.domain.model.inventory.Inventory;
import org.example.pruebatecnicaecommerce.infrastructure.persistence.inventory.InventoryEntity;
import org.example.pruebatecnicaecommerce.infrastructure.persistence.inventory.JpaInventoryRepository;
import org.example.pruebatecnicaecommerce.infrastructure.persistence.mapper.InventoryMapper;
import org.example.pruebatecnicaecommerce.shared.error.OptimisticLockException;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Component;

import java.util.UUID;

/**
 * Wrapper for Inventory repository operations with optimistic locking support
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class ConcurrentInventoryRepositoryWrapper {

    private final JpaInventoryRepository jpaRepository;

    /**
     * Save inventory with optimistic locking and retry mechanism
     */
    @Retryable(value = { OptimisticLockingFailureException.class,
            OptimisticLockException.class }, maxAttempts = 3, backoff = @Backoff(delay = 100, multiplier = 2.0, maxDelay = 1000))
    public Inventory saveWithOptimisticLocking(Inventory inventory) {
        try {
            InventoryEntity entity = jpaRepository.findByProductId(inventory.getProductId())
                    .orElseGet(() -> InventoryMapper.toEntity(inventory));

            // Validate version for optimistic locking
            if (entity.getVersion() != null && !entity.getVersion().equals(inventory.getVersion())) {
                throw new OptimisticLockException(
                        String.format("Inventory version mismatch for product %s. Expected: %d, Found: %d",
                                inventory.getProductId(), inventory.getVersion(), entity.getVersion()));
            }

            // Update entity with new values
            entity.setStock(inventory.getStock());
            entity.setVersion(inventory.getVersion());

            InventoryEntity saved = jpaRepository.save(entity);
            log.debug("Inventory saved successfully for product {} with version: {}",
                    saved.getProductId(), saved.getVersion());
            return InventoryMapper.toDomain(saved);

        } catch (OptimisticLockingFailureException e) {
            log.warn("Optimistic lock failure for inventory product ID: {}. Retrying...", inventory.getProductId());
            throw new OptimisticLockException(
                    "Concurrent modification detected for inventory: " + inventory.getProductId(), e);
        }
    }

    /**
     * Update stock with optimistic locking
     */
    @Retryable(value = { OptimisticLockingFailureException.class,
            OptimisticLockException.class }, maxAttempts = 3, backoff = @Backoff(delay = 100, multiplier = 2.0, maxDelay = 1000))
    public Inventory updateStockWithOptimisticLocking(UUID productId, int newStock, Long expectedVersion) {
        try {
            InventoryEntity entity = jpaRepository.findByProductId(productId)
                    .orElseThrow(() -> new IllegalArgumentException("Inventory not found for product: " + productId));

            // Validate version for optimistic locking
            if (!entity.getVersion().equals(expectedVersion)) {
                throw new OptimisticLockException(
                        String.format("Inventory version mismatch for product %s. Expected: %d, Found: %d",
                                productId, expectedVersion, entity.getVersion()));
            }

            entity.setStock(newStock);
            InventoryEntity saved = jpaRepository.save(entity);
            log.debug("Inventory stock updated for product {} to {} with version: {}",
                    saved.getProductId(), saved.getStock(), saved.getVersion());
            return InventoryMapper.toDomain(saved);

        } catch (OptimisticLockingFailureException e) {
            log.warn("Optimistic lock failure updating stock for product ID: {}. Retrying...", productId);
            throw new OptimisticLockException(
                    "Concurrent modification detected updating stock for product: " + productId, e);
        }
    }
}