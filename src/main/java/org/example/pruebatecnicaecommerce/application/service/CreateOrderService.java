package org.example.pruebatecnicaecommerce.application.service;

import lombok.RequiredArgsConstructor;
import org.example.pruebatecnicaecommerce.application.dto.CreateOrderRequest;
import org.example.pruebatecnicaecommerce.application.dto.OrderResponse;
import org.example.pruebatecnicaecommerce.application.dto.OrderResponseMapper;
import org.example.pruebatecnicaecommerce.domain.model.event.OrderCreatedEvent;
import org.example.pruebatecnicaecommerce.domain.model.event.OrderStatusChangedEvent;
import org.example.pruebatecnicaecommerce.domain.model.inventory.Inventory;
import org.example.pruebatecnicaecommerce.domain.model.inventory.InventoryRepository;
import org.example.pruebatecnicaecommerce.domain.model.order.Order;
import org.example.pruebatecnicaecommerce.domain.model.order.OrderItem;
import org.example.pruebatecnicaecommerce.domain.model.order.OrderRepository;
import org.example.pruebatecnicaecommerce.domain.service.EventPublisher;
import org.example.pruebatecnicaecommerce.shared.utils.UuidUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class CreateOrderService {

    private final OrderRepository orderRepository;
    private final InventoryRepository inventoryRepository;
    private final EventPublisher eventPublisher;

    public OrderResponse execute(CreateOrderRequest request, String customerPublicId) {
        Order order = Order.create(UuidUtils.fromString(customerPublicId));

        request.getItems().forEach(item -> {
            Inventory inventory = inventoryRepository.findByProductCode(item.getProductCode())
                    .orElseThrow(() -> new IllegalArgumentException("Product not found: " + item.getProductCode()));

            inventory.reserve(item.getQuantity());
            inventoryRepository.save(inventory);

            UUID productId = inventory.getProductId();

            BigDecimal unitPrice = item.getUnitPrice() != null ? item.getUnitPrice() : BigDecimal.valueOf(10.00);

            order.addItem(new OrderItem(
                    productId,
                    item.getQuantity(),
                    unitPrice));
        });

        Order savedOrder = orderRepository.save(order);

        OrderCreatedEvent createdEvent = new OrderCreatedEvent(
                savedOrder.getId(),
                savedOrder.getPublicId(),
                savedOrder.getCustomerId(),
                savedOrder.getTotal(),
                savedOrder.getItems().size());
        eventPublisher.publish(createdEvent);

        OrderStatusChangedEvent statusEvent = new OrderStatusChangedEvent(
                savedOrder.getId(),
                savedOrder.getPublicId(),
                savedOrder.getCustomerId(),
                null,
                savedOrder.getStatus(),
                savedOrder.getTotal());
        eventPublisher.publish(statusEvent);

        return OrderResponseMapper.fromDomain(savedOrder);
    }
}
