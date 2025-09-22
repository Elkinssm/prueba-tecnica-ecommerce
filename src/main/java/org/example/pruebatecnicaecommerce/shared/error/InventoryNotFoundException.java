package org.example.pruebatecnicaecommerce.shared.error;

import java.util.UUID;

/**
 * Exception thrown when an inventory/product is not found
 */
public class InventoryNotFoundException extends DomainException {

    public InventoryNotFoundException(UUID productId) {
        super("Inventory not found for product ID: " + productId);
    }
}