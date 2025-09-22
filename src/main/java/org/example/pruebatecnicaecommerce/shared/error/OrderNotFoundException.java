package org.example.pruebatecnicaecommerce.shared.error;

import java.util.UUID;

/**
 * Exception thrown when an order is not found
 */
public class OrderNotFoundException extends DomainException {

    public OrderNotFoundException(UUID orderId) {
        super("Order not found with ID: " + orderId);
    }

    public OrderNotFoundException(String publicOrderId) {
        super("Order not found with public ID: " + publicOrderId);
    }
}