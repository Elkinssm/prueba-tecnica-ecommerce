package org.example.pruebatecnicaecommerce.domain.model.event;

import lombok.Getter;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * Event published when a new order is created
 */
@Getter
public class OrderCreatedEvent extends BaseDomainEvent {
    private final String publicOrderId;
    private final UUID customerId;
    private final BigDecimal orderTotal;
    private final int itemCount;

    public OrderCreatedEvent(UUID orderId, String publicOrderId, UUID customerId,
            BigDecimal orderTotal, int itemCount) {
        super(orderId, "Order");
        this.publicOrderId = publicOrderId;
        this.customerId = customerId;
        this.orderTotal = orderTotal;
        this.itemCount = itemCount;
    }

    @Override
    public String getEventType() {
        return "OrderCreated";
    }
}