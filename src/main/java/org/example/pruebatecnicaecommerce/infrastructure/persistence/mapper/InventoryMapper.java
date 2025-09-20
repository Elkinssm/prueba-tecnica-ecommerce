package org.example.pruebatecnicaecommerce.infrastructure.persistence.mapper;

import org.example.pruebatecnicaecommerce.domain.inventory.Inventory;
import org.example.pruebatecnicaecommerce.infrastructure.persistence.inventory.InventoryEntity;


public class InventoryMapper {

    public static InventoryEntity toEntity(Inventory inventory) {
        InventoryEntity entity = new InventoryEntity();
        entity.setProductId(inventory.getProductId());
        entity.setStock(inventory.getStock());
        entity.setVersion(inventory.getVersion());
        return entity;
    }

    public static Inventory toDomain(InventoryEntity entity) {
        return Inventory.restore(
                entity.getProductId(),
                entity.getStock(),
                entity.getVersion()
        );
    }
}
