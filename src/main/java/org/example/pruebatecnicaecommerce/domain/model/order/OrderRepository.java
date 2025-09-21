package org.example.pruebatecnicaecommerce.domain.model.order;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface OrderRepository {
    Order save(Order order);

    Optional<Order> findById(UUID orderId);

    Optional<Order> findByPublicId(String publicId);

    List<Order> findByCustomerId(UUID customerId);

    List<Order> findAll();

    List<Order> findByStatus(OrderStatus status);

    List<Order> findByCreatedAtBetween(Instant start, Instant end);

    List<Order> findByCustomerIdAndStatus(UUID customerId, OrderStatus status);
}
