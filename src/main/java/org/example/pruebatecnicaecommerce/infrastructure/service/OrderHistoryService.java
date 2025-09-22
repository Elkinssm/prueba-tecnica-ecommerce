package org.example.pruebatecnicaecommerce.infrastructure.service;

import lombok.RequiredArgsConstructor;
import org.example.pruebatecnicaecommerce.domain.model.order.OrderStatus;
import org.example.pruebatecnicaecommerce.infrastructure.persistence.order.JpaOrderStatusHistoryRepository;
import org.example.pruebatecnicaecommerce.infrastructure.persistence.order.OrderStatusHistoryEntity;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class OrderHistoryService {

    private final JpaOrderStatusHistoryRepository historyRepository;

    public void recordStatusChange(UUID orderId, OrderStatus previousStatus, OrderStatus newStatus) {
        OrderStatusHistoryEntity historyEntry = new OrderStatusHistoryEntity(
                orderId,
                previousStatus,
                newStatus,
                Instant.now());
        historyRepository.save(historyEntry);
    }
}
