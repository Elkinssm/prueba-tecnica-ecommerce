package org.example.pruebatecnicaecommerce.domain.model.event;

import lombok.Getter;
import org.example.pruebatecnicaecommerce.domain.model.order.OrderStatus;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * Event published when an order status changes
 */
@Getter
public class OrderStatusChangedEvent extends BaseDomainEvent {
    private final String publicOrderId;
    private final UUID customerId;
    private final OrderStatus previousStatus;
    private final OrderStatus newStatus;
    private final BigDecimal orderTotal;

    public OrderStatusChangedEvent(UUID orderId, String publicOrderId, UUID customerId,
            OrderStatus previousStatus, OrderStatus newStatus, BigDecimal orderTotal) {
        super(orderId, "Order");
        this.publicOrderId = publicOrderId;
        this.customerId = customerId;
        this.previousStatus = previousStatus;
        this.newStatus = newStatus;
        this.orderTotal = orderTotal;
    }

    @Override
    public String getEventType() {
        return "OrderStatusChanged";
    }
}