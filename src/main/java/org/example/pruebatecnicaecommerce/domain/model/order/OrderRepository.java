package org.example.pruebatecnicaecommerce.domain.model.order;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface OrderRepository {
    Order save(Order order);

    Optional<Order> findById(UUID orderId);

    Optional<Order> findByPublicId(String publicId); // Nuevo método para API amigable

    List<Order> findByCustomerId(UUID customerId);

    List<Order> findAll();
}
