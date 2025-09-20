package org.example.pruebatecnicaecommerce.infrastructure.persistence.inventory;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "inventory")
@Getter
@Setter
@NoArgsConstructor
public class InventoryEntity {
    @Id
    private String productId;

    private int stock;

    @Version
    private long version;
}
