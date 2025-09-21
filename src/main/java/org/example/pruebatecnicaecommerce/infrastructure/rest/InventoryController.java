package org.example.pruebatecnicaecommerce.infrastructure.rest;

import lombok.RequiredArgsConstructor;
import org.example.pruebatecnicaecommerce.domain.model.inventory.Inventory;
import org.example.pruebatecnicaecommerce.domain.model.inventory.InventoryRepository;
import org.example.pruebatecnicaecommerce.shared.utils.UuidUtils;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.constraints.Positive;
import java.util.UUID;

@RestController
@RequestMapping("/inventory")
@RequiredArgsConstructor
public class InventoryController {

        private final InventoryRepository inventoryRepository;

        @GetMapping("/{productId}")
        public ResponseEntity<Inventory> getByProductId(@PathVariable String productId) {
                UUID productUuid = UuidUtils.fromString(productId);
                return inventoryRepository.findByProductId(productUuid)
                                .map(ResponseEntity::ok)
                                .orElse(ResponseEntity.notFound().build());
        }

        @PostMapping("/{productId}/increase")
        public ResponseEntity<Inventory> increaseStock(
                        @PathVariable String productId,
                        @RequestParam @Positive(message = "Quantity must be positive") int quantity) {
                UUID productUuid = UuidUtils.fromString(productId);
                Inventory inventory = inventoryRepository.findByProductId(productUuid)
                                .orElseGet(() -> Inventory.create(productUuid, 0));

                inventory.release(-quantity);
                inventoryRepository.save(inventory);
                return ResponseEntity.ok(inventory);
        }

        @PostMapping("/{productId}/decrease")
        public ResponseEntity<Inventory> decreaseStock(
                        @PathVariable String productId,
                        @RequestParam @Positive(message = "Quantity must be positive") int quantity) {
                UUID productUuid = UuidUtils.fromString(productId);
                Inventory inventory = inventoryRepository.findByProductId(productUuid)
                                .orElseThrow(() -> new IllegalArgumentException("Product not found"));

                inventory.reserve(quantity);
                inventoryRepository.save(inventory);
                return ResponseEntity.ok(inventory);
        }
}
