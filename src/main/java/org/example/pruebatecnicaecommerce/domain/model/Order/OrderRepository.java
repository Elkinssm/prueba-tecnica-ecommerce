package org.example.pruebatecnicaecommerce.domain.model.Order;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface OrderRepository {
    void save(Order order);
    Optional<Order> findById(UUID orderId);
    List<Order> findByCustomerId(String customerId);
}
