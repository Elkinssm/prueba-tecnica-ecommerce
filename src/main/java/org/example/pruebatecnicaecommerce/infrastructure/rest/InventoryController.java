package org.example.pruebatecnicaecommerce.infrastructure.rest;

import lombok.RequiredArgsConstructor;
import org.example.pruebatecnicaecommerce.domain.model.inventory.Inventory;
import org.example.pruebatecnicaecommerce.domain.model.inventory.InventoryRepository;
import org.example.pruebatecnicaecommerce.shared.utils.UuidUtils;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.Positive;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/inventory")
@RequiredArgsConstructor
@Tag(name = "Inventory", description = "Consultas relacionadas con el inventario disponible")
@SecurityRequirement(name = "bearerAuth")
public class InventoryController {

        private final InventoryRepository inventoryRepository;

        @GetMapping
        public ResponseEntity<List<Inventory>> getAllInventory() {
                List<Inventory> inventoryList = inventoryRepository.findAll();
                return ResponseEntity.ok(inventoryList);
        }

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

                inventory.release(quantity); // Corregido: usar quantity directamente
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
