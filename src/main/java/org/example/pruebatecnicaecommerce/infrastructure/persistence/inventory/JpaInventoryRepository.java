package org.example.pruebatecnicaecommerce.infrastructure.persistence.inventory;

import org.springframework.data.jpa.repository.JpaRepository;

public interface JpaInventoryRepository extends JpaRepository<InventoryEntity, String> {

}
