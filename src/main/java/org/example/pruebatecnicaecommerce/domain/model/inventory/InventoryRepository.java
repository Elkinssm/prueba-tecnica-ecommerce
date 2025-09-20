package org.example.pruebatecnicaecommerce.domain.model.inventory;

import org.example.pruebatecnicaecommerce.domain.inventory.Inventory;

import java.util.Optional;

public interface InventoryRepository {
    Optional<Inventory> findByProductId(String productId);
    void save(Inventory inventory);
}
