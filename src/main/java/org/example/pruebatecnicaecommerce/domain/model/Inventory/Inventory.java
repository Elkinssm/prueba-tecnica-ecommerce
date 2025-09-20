package org.example.pruebatecnicaecommerce.domain.model.Inventory;

import lombok.Getter;

@Getter
public class Inventory {
    private final String productId;
    private int stock;

    public Inventory(String productId, int stock) {
        if (stock < 0) {
            throw new IllegalArgumentException("Stock cannot be negative");
        }
        this.productId = productId;
        this.stock = stock;
    }

    public void reserve(int quantity) {
        if (quantity <= 0) {
            throw new IllegalArgumentException("Quantity must be positive");
        }
        if (stock < quantity) {
            throw new IllegalStateException("Insufficient stock for product " + productId);
        }
        stock -= quantity;
    }

    public void release(int quantity) {
        if (quantity <= 0) {
            throw new IllegalArgumentException("Quantity must be positive");
        }
        stock += quantity;
    }
}