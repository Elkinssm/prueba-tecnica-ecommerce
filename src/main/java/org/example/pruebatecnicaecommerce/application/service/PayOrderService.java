package org.example.pruebatecnicaecommerce.application.service;


import org.example.pruebatecnicaecommerce.application.dto.OrderResponse;
import org.example.pruebatecnicaecommerce.application.dto.OrderResponseMapper;
import org.example.pruebatecnicaecommerce.domain.model.inventory.InventoryRepository;
import org.example.pruebatecnicaecommerce.domain.model.order.Order;
import org.example.pruebatecnicaecommerce.domain.model.order.OrderRepository;
import org.example.pruebatecnicaecommerce.domain.model.inventory.Inventory;

import java.util.UUID;


public class PayOrderService {

    private final OrderRepository orderRepository;
    private final InventoryRepository inventoryRepository;

    public PayOrderService(OrderRepository orderRepository, InventoryRepository inventoryRepository) {
        this.orderRepository = orderRepository;
        this.inventoryRepository = inventoryRepository;
    }

    public OrderResponse execute(String orderId) {
        Order order = orderRepository.findById(UUID.fromString(orderId))
                .orElseThrow(() -> new IllegalArgumentException("Order not found"));

       order.getItems().forEach(item -> {
            Inventory inventory = inventoryRepository.findByProductId(item.getProductId())
                    .orElseThrow(() -> new IllegalArgumentException(
                            "Inventory not found for product " + item.getProductId()
                    ));
            inventory.reserve(item.getQuantity());
            inventoryRepository.save(inventory);
        });

        order.pay();
        orderRepository.save(order);

        return OrderResponseMapper.fromDomain(order);
    }
}
