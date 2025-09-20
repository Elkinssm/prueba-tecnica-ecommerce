package org.example.pruebatecnicaecommerce.infrastructure.persistence.adapter;

import lombok.RequiredArgsConstructor;
import org.example.pruebatecnicaecommerce.domain.inventory.Inventory;

import org.example.pruebatecnicaecommerce.domain.model.inventory.InventoryRepository;
import org.example.pruebatecnicaecommerce.infrastructure.persistence.inventory.JpaInventoryRepository;
import org.example.pruebatecnicaecommerce.infrastructure.persistence.mapper.InventoryMapper;

import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class InventoryJpaRepositoryAdapter implements InventoryRepository {


      private final JpaInventoryRepository jpaRepository;

    @Override
    public Optional<Inventory> findByProductId(String productId) {
        return jpaRepository.findById(productId).map(InventoryMapper::toDomain);
    }

    @Override
    public void save(Inventory inventory) {
        jpaRepository.save(InventoryMapper.toEntity(inventory));
    }
}
