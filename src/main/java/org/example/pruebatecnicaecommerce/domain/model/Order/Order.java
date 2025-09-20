package org.example.pruebatecnicaecommerce.domain.model.Order;

import lombok.Getter;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Getter
public class Order {
    private final UUID id;
    private final String customerId;
    private OrderStatus status;
    private final Instant createdAt;
    private long version;
    private final List<OrderItem> items = new ArrayList<>();

    // Constructor privado → obliga a usar el factory
    private Order(UUID id, String customerId, OrderStatus status,
                  Instant createdAt, long version, List<OrderItem> items) {
        this.id = id;
        this.customerId = customerId;
        this.status = status;
        this.createdAt = createdAt;
        this.version = version;
        if (items != null) {
            this.items.addAll(items);
        }
    }

    public static Order create(String customerId) {
        return new Order(
                UUID.randomUUID(),
                customerId,
                OrderStatus.CREATED,
                Instant.now(),
                0,
                new ArrayList<>()
        );
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

    public void pay() { changeStatus(OrderStatus.PAID); }
    public void ship() { changeStatus(OrderStatus.SHIPPED); }
    public void cancel() { changeStatus(OrderStatus.CANCELLED); }

    public BigDecimal getTotal() {
        return items.stream()
                .map(OrderItem::getTotalPrice)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
}
