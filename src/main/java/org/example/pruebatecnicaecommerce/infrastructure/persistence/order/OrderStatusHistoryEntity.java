package org.example.pruebatecnicaecommerce.infrastructure.persistence.order;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.example.pruebatecnicaecommerce.domain.model.order.OrderStatus;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "order_status_history")
@Getter
@Setter
@NoArgsConstructor
public class OrderStatusHistoryEntity {

    @Id
    @Column(columnDefinition = "VARCHAR(36)")
    private UUID id;

    @Column(name = "order_id", columnDefinition = "VARCHAR(36)", nullable = false)
    private UUID orderId;

    @Enumerated(EnumType.STRING)
    @Column(name = "previous_status", length = 50)
    private OrderStatus previousStatus;

    @Enumerated(EnumType.STRING)
    @Column(name = "new_status", length = 50, nullable = false)
    private OrderStatus newStatus;

    @Column(name = "changed_at", nullable = false)
    private Instant changedAt;

    public OrderStatusHistoryEntity(UUID orderId, OrderStatus previousStatus, OrderStatus newStatus,
            Instant changedAt) {
        this.id = UUID.randomUUID();
        this.orderId = orderId;
        this.previousStatus = previousStatus;
        this.newStatus = newStatus;
        this.changedAt = changedAt;
    }
}
