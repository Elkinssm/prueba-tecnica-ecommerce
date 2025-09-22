package org.example.pruebatecnicaecommerce.infrastructure.persistence.adapter;

import lombok.RequiredArgsConstructor;

import org.example.pruebatecnicaecommerce.domain.model.inventory.Inventory;
import org.example.pruebatecnicaecommerce.domain.model.inventory.InventoryRepository;
import org.example.pruebatecnicaecommerce.infrastructure.persistence.inventory.JpaInventoryRepository;
import org.example.pruebatecnicaecommerce.infrastructure.persistence.mapper.InventoryMapper;

import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Repository
@RequiredArgsConstructor
public class InventoryJpaRepositoryAdapter implements InventoryRepository {

    private final JpaInventoryRepository jpaRepository;
    private final ConcurrentInventoryRepositoryWrapper concurrentWrapper;

    @Override
    @Cacheable(value = "inventory", key = "#productId")
    public Optional<Inventory> findByProductId(UUID productId) {
        return jpaRepository.findByProductId(productId)
                .map(InventoryMapper::toDomain);
    }

    @Override
    @Cacheable(value = "inventory", key = "#productCode")
    public Optional<Inventory> findByProductCode(String productCode) {
        return jpaRepository.findByProductCode(productCode)
                .map(InventoryMapper::toDomain);
    }

    @Override
    @Cacheable(value = "allInventory")
    public List<Inventory> findAll() {
        return jpaRepository.findAll()
                .stream()
                .map(InventoryMapper::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    @CacheEvict(value = { "inventory", "allInventory" }, key = "#inventory.productId", allEntries = true)
    public void save(Inventory inventory) {
        // Use concurrent wrapper for save operations with optimistic locking
        concurrentWrapper.saveWithOptimisticLocking(inventory);
    }
}
