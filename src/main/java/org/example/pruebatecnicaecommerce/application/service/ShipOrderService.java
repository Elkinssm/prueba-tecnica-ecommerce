package org.example.pruebatecnicaecommerce.application.service;

import lombok.RequiredArgsConstructor;
import org.example.pruebatecnicaecommerce.application.dto.OrderResponse;
import org.example.pruebatecnicaecommerce.application.dto.OrderResponseMapper;
import org.example.pruebatecnicaecommerce.domain.model.event.OrderStatusChangedEvent;
import org.example.pruebatecnicaecommerce.domain.model.order.Order;
import org.example.pruebatecnicaecommerce.domain.model.order.OrderRepository;
import org.example.pruebatecnicaecommerce.domain.model.order.OrderStatus;
import org.example.pruebatecnicaecommerce.domain.service.EventPublisher;
import org.example.pruebatecnicaecommerce.shared.error.OrderNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class ShipOrderService {

    private final OrderRepository orderRepository;
    private final EventPublisher eventPublisher;

    public OrderResponse execute(String publicOrderId) {
        Order order = orderRepository.findByPublicId(publicOrderId)
                .orElseThrow(() -> new OrderNotFoundException(publicOrderId));

        OrderStatus previousStatus = order.getStatus();

        order.ship();
        Order savedOrder = orderRepository.save(order);

        OrderStatusChangedEvent event = new OrderStatusChangedEvent(
                savedOrder.getId(),
                savedOrder.getPublicId(),
                savedOrder.getCustomerId(),
                previousStatus,
                savedOrder.getStatus(),
                savedOrder.getTotal());
        eventPublisher.publish(event);

        return OrderResponseMapper.fromDomain(savedOrder);
    }
}
