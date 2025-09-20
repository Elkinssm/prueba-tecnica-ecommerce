package org.example.pruebatecnicaecommerce.application.service;

import org.example.pruebatecnicaecommerce.application.dto.OrderResponse;
import org.example.pruebatecnicaecommerce.application.dto.OrderResponseMapper;
import org.example.pruebatecnicaecommerce.domain.model.inventory.Inventory;
import org.example.pruebatecnicaecommerce.domain.model.inventory.InventoryRepository;
import org.example.pruebatecnicaecommerce.domain.model.order.Order;
import org.example.pruebatecnicaecommerce.domain.model.order.OrderRepository;
import org.example.pruebatecnicaecommerce.domain.model.order.OrderStatus;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class CancelOrderService {

    private final OrderRepository orderRepository;
    private final InventoryRepository inventoryRepository;

    public CancelOrderService(OrderRepository orderRepository, InventoryRepository inventoryRepository) {
        this.orderRepository = orderRepository;
        this.inventoryRepository = inventoryRepository;
    }

    public OrderResponse execute(String orderId){
        Order order = orderRepository.findById(UUID.fromString(orderId))
                .orElseThrow(() -> new IllegalArgumentException("Order not found"));

        if (order.getStatus().equals(OrderStatus.PAID)) {
            order.getItems().forEach(item -> {
                Inventory inventory = inventoryRepository.findByProductId(item.getProductId())
                        .orElseThrow(() -> new IllegalArgumentException(
                                "Inventory not found for product " + item.getProductId()
                        ));
                inventory.release(item.getQuantity());
                inventoryRepository.save(inventory);
            });
        }

        order.cancel();
        orderRepository.save(order);

        return OrderResponseMapper.fromDomain(order);
    }
}
