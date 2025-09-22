package org.example.pruebatecnicaecommerce.domain.model.inventory;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface InventoryRepository {
    Optional<Inventory> findByProductId(UUID productId);

    Optional<Inventory> findByProductCode(String productCode);

    List<Inventory> findAll();

    void save(Inventory inventory);
}
