package org.example.pruebatecnicaecommerce.application.service;

import lombok.RequiredArgsConstructor;
import org.example.pruebatecnicaecommerce.application.dto.OrderResponse;
import org.example.pruebatecnicaecommerce.application.dto.OrderResponseMapper;
import org.example.pruebatecnicaecommerce.domain.model.order.Order;
import org.example.pruebatecnicaecommerce.domain.model.order.OrderRepository;
import org.example.pruebatecnicaecommerce.shared.error.OrderNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class ShipOrderService {

    private final OrderRepository orderRepository;

    public OrderResponse execute(String publicOrderId) {
        Order order = orderRepository.findByPublicId(publicOrderId)
                .orElseThrow(() -> new OrderNotFoundException(null)); // TODO: Ajustar excepción

        order.ship();
        Order savedOrder = orderRepository.save(order);

        return OrderResponseMapper.fromDomain(savedOrder);
    }
}
