package org.example.pruebatecnicaecommerce.application.service;

import lombok.RequiredArgsConstructor;
import org.example.pruebatecnicaecommerce.application.dto.OrderStatusHistoryResponse;
import org.example.pruebatecnicaecommerce.domain.model.order.Order;
import org.example.pruebatecnicaecommerce.domain.model.order.OrderRepository;
import org.example.pruebatecnicaecommerce.infrastructure.persistence.order.JpaOrderStatusHistoryRepository;
import org.example.pruebatecnicaecommerce.infrastructure.persistence.order.OrderStatusHistoryEntity;
import org.example.pruebatecnicaecommerce.shared.error.OrderNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class GetOrderHistoryService {

    private final OrderRepository orderRepository;
    private final JpaOrderStatusHistoryRepository historyRepository;

    public List<OrderStatusHistoryResponse> execute(String publicOrderId) {
        Order order = orderRepository.findByPublicId(publicOrderId)
                .orElseThrow(() -> new OrderNotFoundException(publicOrderId));

        // Get real history from database
        List<OrderStatusHistoryEntity> historyEntities = historyRepository
                .findByOrderIdOrderByChangedAtAsc(order.getId());

        List<OrderStatusHistoryResponse> history = new ArrayList<>();

        if (historyEntities.isEmpty()) {
            // Fallback: if no history exists, create initial CREATED entry
            LocalDateTime createdAt = LocalDateTime.ofInstant(order.getCreatedAt(), ZoneId.systemDefault());
            history.add(OrderStatusHistoryResponse.builder()
                    .status("CREATED")
                    .changedAt(createdAt)
                    .previousStatus(null)
                    .build());
        } else {
            // Convert history entities to response DTOs
            for (OrderStatusHistoryEntity entity : historyEntities) {
                LocalDateTime changedAt = LocalDateTime.ofInstant(entity.getChangedAt(), ZoneId.systemDefault());
                history.add(OrderStatusHistoryResponse.builder()
                        .status(entity.getNewStatus().name())
                        .changedAt(changedAt)
                        .previousStatus(entity.getPreviousStatus() != null ? entity.getPreviousStatus().name() : null)
                        .build());
            }
        }

        return history;
    }
}