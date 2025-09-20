package org.example.pruebatecnicaecommerce.domain.model.Order;

import lombok.Builder;
import lombok.Getter;
import lombok.Singular;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Builder
@Getter
public class Order {
    private final UUID id;
    private final String customerId;
    private OrderStatus status;
    private final Instant createdAt;
    private long version;

    @Builder.Default
    private final List<OrderItem> items = new ArrayList<>();

    @Builder
    public Order(UUID id, String customerId, OrderStatus status,
                 Instant createdAt, long version,
                 @Singular List<OrderItem> items) {
        this.id = id != null ? id : UUID.randomUUID();
        this.customerId = customerId;
        this.status = status != null ? status : OrderStatus.CREATED;
        this.createdAt = createdAt != null ? createdAt : Instant.now();
        this.version = version;
        if (items != null) {
            this.items.addAll(items);
        }
    }

    public void addItem(OrderItem item) {
        if (status != OrderStatus.CREATED) {
            throw new IllegalStateException("Cannot add items once order is not in CREATED status");
        }
        this.items.add(item);
    }

    public void changeStatus(OrderStatus next) {
        if (!this.status.canTransitionTo(next)) {
            throw new IllegalStateException(
                    "Invalid transition from " + this.status + " to " + next
            );
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
