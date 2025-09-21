package org.example.pruebatecnicaecommerce.domain.model.inventory;

import lombok.Getter;
import org.example.pruebatecnicaecommerce.shared.error.InsufficientStockException;

import java.util.UUID;

@Getter
public class Inventory {
    private final UUID productId;
    private int stock;
    private long version;

    private Inventory(UUID productId, int stock, long version) {
        if (stock < 0)
            throw new IllegalArgumentException("Stock cannot be negative");
        this.productId = productId;
        this.stock = stock;
        this.version = version;
    }

    public static Inventory create(UUID productId, int initialStock) {
        return new Inventory(productId, initialStock, 0);
    }

    public static Inventory restore(UUID productId, int stock, long version) {
        return new Inventory(productId, stock, version);
    }

    public void reserve(int quantity) {
        if (quantity <= 0) {
            throw new IllegalArgumentException("Quantity must be greater than zero");
        }
        if (stock < quantity) {
            throw new InsufficientStockException(productId.toString(), quantity, stock);
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
