package org.example.pruebatecnicaecommerce.infrastructure.persistence.order;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface JpaOrderRepository  extends JpaRepository<OrderEntity, UUID> {
    List<OrderEntity> findByCustomerId(String customerId);
}
