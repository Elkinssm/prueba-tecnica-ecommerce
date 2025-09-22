package org.example.pruebatecnicaecommerce.infrastructure.persistence.order;

import org.example.pruebatecnicaecommerce.domain.model.order.OrderStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface JpaOrderRepository extends JpaRepository<OrderEntity, UUID> {
    List<OrderEntity> findByCustomerId(UUID customerId);

    Optional<OrderEntity> findByPublicId(String publicId);

    List<OrderEntity> findByStatus(OrderStatus status);

    List<OrderEntity> findByCreatedAtBetween(Instant start, Instant end);

    List<OrderEntity> findByCustomerIdAndStatus(UUID customerId, OrderStatus status);
}
