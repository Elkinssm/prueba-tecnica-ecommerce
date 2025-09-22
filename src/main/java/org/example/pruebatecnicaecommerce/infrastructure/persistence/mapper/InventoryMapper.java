package org.example.pruebatecnicaecommerce.infrastructure.persistence.mapper;

import org.example.pruebatecnicaecommerce.domain.model.inventory.Inventory;
import org.example.pruebatecnicaecommerce.infrastructure.persistence.inventory.InventoryEntity;

import java.util.UUID;

public class InventoryMapper {

    public static InventoryEntity toEntity(Inventory inventory) {
        InventoryEntity entity = new InventoryEntity();
        entity.setProductId(inventory.getProductId());
        entity.setProductCode(inventory.getProductCode());
        entity.setStock(inventory.getStock());
        entity.setVersion(inventory.getVersion());
        return entity;
    }

    public static Inventory toDomain(InventoryEntity entity) {
        return Inventory.restore(
                entity.getProductId(),
                entity.getProductCode(),
                entity.getStock(),
                entity.getVersion());
    }
}
