package org.example.pruebatecnicaecommerce.domain.model.inventory;



import java.util.Optional;
import java.util.UUID;

public interface InventoryRepository {
    Optional<Inventory> findByProductId(UUID productId);
    void save(Inventory inventory);
}


