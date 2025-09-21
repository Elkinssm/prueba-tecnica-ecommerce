package org.example.pruebatecnicaecommerce.domain.model.order;

import lombok.Getter;
import org.example.pruebatecnicaecommerce.shared.error.InvalidOrderStateTransitionException;
import org.example.pruebatecnicaecommerce.shared.utils.PublicIdGenerator;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Getter
public class Order {
    private final UUID id;
    private final String publicId; // ID amigable para API
    private final UUID customerId;
    private OrderStatus status;
    private final Instant createdAt;
    private long version;
    private final List<OrderItem> items = new ArrayList<>();

    private Order(UUID id, String publicId, UUID customerId, OrderStatus status,
            Instant createdAt, long version) {
        this.id = id;
        this.publicId = publicId;
        this.customerId = customerId;
        this.status = status;
        this.createdAt = createdAt;
        this.version = version;
    }

    public static Order create(UUID customerId) {
        return new Order(
                UUID.randomUUID(),
                PublicIdGenerator.generateOrderId(), // ID amigable generado
                customerId,
                OrderStatus.CREATED,
                Instant.now(),
                0);
    }

    public static Order restore(UUID id, String publicId, UUID customerId, OrderStatus status,
            Instant createdAt, long version) {
        return new Order(id, publicId, customerId, status, createdAt, version);
    }

    public void addItem(OrderItem item) {
        if (status != OrderStatus.CREATED) {
            throw new InvalidOrderStateTransitionException(
                    status.name(),
                    "ADDING_ITEMS - Items can only be added when order is in CREATED state");
        }
        this.items.add(item);
    }

    public void restoreItem(OrderItem item) {
        this.items.add(item);
    }

    public void changeStatus(OrderStatus next) {
        if (!this.status.canTransitionTo(next)) {
            throw new InvalidOrderStateTransitionException(this.status.name(), next.name());
        }
        this.status = next;
    }

    public void pay() {
        changeStatus(OrderStatus.PAID);
    }

    public void ship() {
        changeStatus(OrderStatus.SHIPPED);
    }

    public void cancel() {
        changeStatus(OrderStatus.CANCELLED);
    }

    public BigDecimal getTotal() {
        return items.stream()
                .map(OrderItem::getTotalPrice)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
}
