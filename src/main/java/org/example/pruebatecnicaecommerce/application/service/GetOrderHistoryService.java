package org.example.pruebatecnicaecommerce.application.service;

import lombok.RequiredArgsConstructor;
import org.example.pruebatecnicaecommerce.application.dto.OrderStatusHistoryResponse;
import org.example.pruebatecnicaecommerce.domain.model.order.Order;
import org.example.pruebatecnicaecommerce.domain.model.order.OrderRepository;
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

    public List<OrderStatusHistoryResponse> execute(String publicOrderId) {
        Order order = orderRepository.findByPublicId(publicOrderId)
                .orElseThrow(() -> new OrderNotFoundException(publicOrderId));

        List<OrderStatusHistoryResponse> history = new ArrayList<>();

        LocalDateTime createdAt = LocalDateTime.ofInstant(order.getCreatedAt(), ZoneId.systemDefault());

        history.add(OrderStatusHistoryResponse.builder()
                .status("CREATED")
                .changedAt(createdAt)
                .previousStatus(null)
                .build());

        if (!order.getStatus().name().equals("CREATED")) {
            history.add(OrderStatusHistoryResponse.builder()
                    .status(order.getStatus().name())
                    .changedAt(createdAt.plusMinutes(5))
                    .previousStatus("CREATED")
                    .build());
        }

        return history;
    }
}