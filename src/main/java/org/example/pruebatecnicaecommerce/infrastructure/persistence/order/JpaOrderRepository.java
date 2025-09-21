package org.example.pruebatecnicaecommerce.infrastructure.persistence.order;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface JpaOrderRepository extends JpaRepository<OrderEntity, UUID> {
    List<OrderEntity> findByCustomerId(UUID customerId);

    Optional<OrderEntity> findByPublicId(String publicId);
}
