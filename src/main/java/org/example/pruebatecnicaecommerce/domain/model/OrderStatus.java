package org.example.pruebatecnicaecommerce.domain.model;

public enum OrderStatus {
    CREATED,PAID,SHIPPED,DELIVERED,CANCELLED;

    public boolean canTransitionTo(OrderStatus newStatus) {
        return switch (this) {
            case CREATED -> newStatus == PAID || newStatus == CANCELLED;
            case PAID -> newStatus == SHIPPED || newStatus == CANCELLED;
            case SHIPPED -> newStatus == DELIVERED;
            case DELIVERED -> false;
            case CANCELLED -> false;
            default -> false;
        };
    }
}
