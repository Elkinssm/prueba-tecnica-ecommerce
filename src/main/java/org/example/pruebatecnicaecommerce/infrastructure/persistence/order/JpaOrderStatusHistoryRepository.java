package org.example.pruebatecnicaecommerce.infrastructure.persistence.order;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface JpaOrderStatusHistoryRepository extends JpaRepository<OrderStatusHistoryEntity, UUID> {
    List<OrderStatusHistoryEntity> findByOrderIdOrderByChangedAtAsc(UUID orderId);
}
