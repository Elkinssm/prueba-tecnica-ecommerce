package org.example.pruebatecnicaecommerce.infrastructure.persistence.inventory;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

@Entity
@Table(name = "inventory", indexes = {
        @Index(name = "idx_inventory_product", columnList = "product_id", unique = true)
})
@Getter
@Setter
@NoArgsConstructor
public class InventoryEntity {

    @Id
    @GeneratedValue
    @Column(columnDefinition = "VARCHAR(36)")
    private UUID id;

    @Column(name = "product_id", columnDefinition = "VARCHAR(36)", nullable = false, unique = true)
    private UUID productId;

    @Column(nullable = false)
    private int stock;

    @Version
    private Long version;

}