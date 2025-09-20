package org.example.pruebatecnicaecommerce.domain.inventory;

import lombok.Getter;

@Getter
public class Inventory {
    private final String productId;
    private int stock;
    private long version;

    private Inventory(String productId, int stock, long version) {
        if (productId == null || productId.isBlank()) {
            throw new IllegalArgumentException("ProductId cannot be null or blank");
        }
        if (stock < 0) {
            throw new IllegalArgumentException("Stock cannot be negative");
        }
        this.productId = productId;
        this.stock = stock;
        this.version = version;
    }

    public static Inventory create(String productId, int initialStock) {
        return new Inventory(productId, initialStock, 0);
    }

    public void reserve(int quantity) {
        if (quantity <= 0) {
            throw new IllegalArgumentException("Quantity must be greater than zero");
        }
        if (stock < quantity) {
            throw new IllegalStateException("Not enough stock to reserve");
        }
        this.stock -= quantity;
    }

    public void release(int quantity) {
        if (quantity <= 0) {
            throw new IllegalArgumentException("Quantity must be greater than zero");
        }
        this.stock += quantity;
    }
}
