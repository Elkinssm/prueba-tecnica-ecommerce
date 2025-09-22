package org.example.pruebatecnicaecommerce.infrastructure.persistence.adapter;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.pruebatecnicaecommerce.domain.model.order.Order;
import org.example.pruebatecnicaecommerce.infrastructure.persistence.mapper.OrderMapper;
import org.example.pruebatecnicaecommerce.infrastructure.persistence.order.JpaOrderRepository;
import org.example.pruebatecnicaecommerce.infrastructure.persistence.order.OrderEntity;
import org.example.pruebatecnicaecommerce.shared.error.OptimisticLockException;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Component;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;

/**
 * Wrapper for Order repository operations with optimistic locking support
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class ConcurrentOrderRepositoryWrapper {

    private final JpaOrderRepository jpaRepository;

    @PersistenceContext
    private EntityManager entityManager;

    /**
     * Save order with optimistic locking and retry mechanism
     */
    @Retryable(value = { OptimisticLockingFailureException.class,
            OptimisticLockException.class }, maxAttempts = 3, backoff = @Backoff(delay = 100, multiplier = 2.0, maxDelay = 1000))
    public Order saveWithOptimisticLocking(Order order) {
        try {
            log.debug("Saving order with ID: {}, Version: {}", order.getId(), order.getVersion());
            OrderEntity saved;

            // Check if there's already a managed entity with this ID in the session
            OrderEntity managedEntity = entityManager.find(OrderEntity.class, order.getId());

            if (managedEntity != null) {
                // Entity is already managed - update it directly
                log.debug("Updating existing managed entity for order ID: {}", order.getId());

                // Use the mapper to sync the managed entity with the domain object
                OrderEntity updatedEntity = OrderMapper.toEntity(order);

                // Update the managed entity fields manually to avoid session conflicts
                managedEntity.setPublicId(updatedEntity.getPublicId());
                managedEntity.setStatus(updatedEntity.getStatus());
                managedEntity.setVersion(updatedEntity.getVersion());

                // Clear and rebuild items to ensure proper cascade
                managedEntity.getItems().clear();
                entityManager.flush(); // Flush to remove old items

                final OrderEntity finalManagedEntity = managedEntity;
                updatedEntity.getItems().forEach(item -> {
                    item.setOrder(finalManagedEntity);
                    finalManagedEntity.getItems().add(item);
                });

                entityManager.flush(); // Force update to detect optimistic lock conflicts
                saved = managedEntity;
            } else {
                // Convert to entity for new or detached entities
                OrderEntity entity = OrderMapper.toEntity(order);

                // Check if this is a new order by looking in the database
                boolean existsInDB = jpaRepository.existsById(order.getId());
                boolean isNewOrder = !existsInDB;
                log.debug("Exists in DB: {}, Is new order: {}", existsInDB, isNewOrder);

                if (isNewOrder) {
                    // For new orders - use persist
                    entity.setVersion(null); // Let JPA auto-generate version for new entities
                    entityManager.persist(entity);
                    entityManager.flush(); // Force persistence to get the generated version
                    saved = entity;
                    log.debug("Creating new order with ID: {}", order.getId());
                } else {
                    // For existing orders - use merge
                    saved = entityManager.merge(entity);
                    entityManager.flush(); // Force merge to detect optimistic lock conflicts
                    log.debug("Updating existing order ID: {} with version {}",
                            order.getId(), order.getVersion());
                }
            }

            log.debug("Order saved successfully with version: {}", saved.getVersion());
            return OrderMapper.toDomain(saved);

        } catch (OptimisticLockingFailureException e) {
            log.warn("Optimistic lock failure for order ID: {}. Retrying...", order.getId());
            throw new OptimisticLockException("Concurrent modification detected for order: " + order.getId(), e);
        } catch (Exception e) {
            log.error("Unexpected error saving order ID: {}. Error: {}", order.getId(), e.getMessage(), e);
            throw new OptimisticLockException("Unexpected error saving order: " + order.getId(), e);
        }
    }
}