package org.example.pruebatecnicaecommerce.domain.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Order {
    @Builder.Default
    private final List<OrderItem> orderItems = new ArrayList<>();
    private String id;
    private String customerId;
    private OrderStatus orderStatus;
    private Instant createdAt;
    private long version;

    public static Order create(String customerId) {
        return Order.builder()
                .id(UUID.randomUUID().toString())
                .customerId(customerId)
                .orderStatus(OrderStatus.CREATED)
                .createdAt(Instant.now())
                .version(0L)
                .orderItems(new ArrayList<>())
                .build();
    }

    public void addItem(OrderItem item) {
        if (orderStatus != OrderStatus.CREATED) {
            throw new IllegalStateException("Cannot add items once order is not CREATED");
        }
        orderItems.add(item);
    }


    public void pay() {
        transitionTo(OrderStatus.PAID);
    }

    public void ship() {
        transitionTo(OrderStatus.SHIPPED);
    }

    public void cancel() {
        transitionTo(OrderStatus.CANCELLED);
    }

    private void transitionTo(OrderStatus target) {
        if (!orderStatus.canTransitionTo(target)) {
            throw new IllegalStateException("Invalid transition from " + orderStatus + " to " + target);
        }
        this.orderStatus = target;
        this.version++;
    }

    public BigDecimal calculateTotal() {
        return orderItems.stream()
                .map(OrderItem::getTotalPrice)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
}
