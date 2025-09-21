package org.example.pruebatecnicaecommerce.application.service;

import lombok.RequiredArgsConstructor;
import org.example.pruebatecnicaecommerce.application.dto.OrderResponse;
import org.example.pruebatecnicaecommerce.application.dto.OrderResponseMapper;
import org.example.pruebatecnicaecommerce.domain.model.inventory.Inventory;
import org.example.pruebatecnicaecommerce.domain.model.inventory.InventoryRepository;
import org.example.pruebatecnicaecommerce.domain.model.order.Order;
import org.example.pruebatecnicaecommerce.domain.model.order.OrderRepository;
import org.example.pruebatecnicaecommerce.domain.model.order.OrderStatus;
import org.example.pruebatecnicaecommerce.shared.error.InventoryNotFoundException;
import org.example.pruebatecnicaecommerce.shared.error.OrderNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class CancelOrderService {

    private final OrderRepository orderRepository;
    private final InventoryRepository inventoryRepository;

    public OrderResponse execute(String publicOrderId) {
        Order order = orderRepository.findByPublicId(publicOrderId)
                .orElseThrow(() -> new OrderNotFoundException(publicOrderId));

        if (order.getStatus() == OrderStatus.PAID) {
            order.getItems().forEach(item -> {
                Inventory inventory = inventoryRepository.findByProductId(item.getProductId())
                        .orElseThrow(() -> new InventoryNotFoundException(item.getProductId()));
                inventory.release(item.getQuantity());
                inventoryRepository.save(inventory);
            });
        }

        order.cancel();
        Order savedOrder = orderRepository.save(order);

        return OrderResponseMapper.fromDomain(savedOrder);
    }
}
