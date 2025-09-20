package org.example.pruebatecnicaecommerce.domain.model.Inventory;

import java.util.Optional;

public interface InventoryRepository {
    Optional<Inventory> findByProductId(String productId);
    void save(Inventory inventory);
}
