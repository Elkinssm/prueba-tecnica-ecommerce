package org.example.pruebatecnicaecommerce.domain.model.order;

import lombok.Value;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.UUID;

@Value
public class OrderItem implements Serializable {
    private static final long serialVersionUID = 1L;

    UUID productId;
    int quantity;
    BigDecimal unitPrice;

    public OrderItem(UUID productId, int quantity, BigDecimal unitPrice) {
        if (quantity <= 0) {
            throw new IllegalArgumentException("Quantity must be positive");
        }
        if (unitPrice.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Unit price cannot be negative");
        }
        this.productId = productId;
        this.quantity = quantity;
        this.unitPrice = unitPrice;
    }

    public BigDecimal getTotalPrice() {
        return unitPrice.multiply(BigDecimal.valueOf(quantity));
    }
}
