package org.example.pruebatecnicaecommerce.infrastructure.persistence.adapter;

import lombok.RequiredArgsConstructor;

import org.example.pruebatecnicaecommerce.domain.model.inventory.Inventory;
import org.example.pruebatecnicaecommerce.domain.model.inventory.InventoryRepository;
import org.example.pruebatecnicaecommerce.infrastructure.persistence.inventory.InventoryEntity;
import org.example.pruebatecnicaecommerce.infrastructure.persistence.inventory.JpaInventoryRepository;
import org.example.pruebatecnicaecommerce.infrastructure.persistence.mapper.InventoryMapper;

import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
@RequiredArgsConstructor
public class InventoryJpaRepositoryAdapter implements InventoryRepository {

    private final JpaInventoryRepository jpaRepository;

    @Override
    public Optional<Inventory> findByProductId(UUID productId) {
        return jpaRepository.findByProductId(productId)
                .map(InventoryMapper::toDomain);
    }

    @Override
    public void save(Inventory inventory) {
        InventoryEntity entity = jpaRepository.findByProductId(inventory.getProductId())
                .orElseGet(() -> {
                    InventoryEntity newEntity = new InventoryEntity();
                    newEntity.setProductId(inventory.getProductId());
                    return newEntity;
                });

        entity.setStock(inventory.getStock());
        entity.setVersion(inventory.getVersion());

        jpaRepository.save(entity);
    }
}
