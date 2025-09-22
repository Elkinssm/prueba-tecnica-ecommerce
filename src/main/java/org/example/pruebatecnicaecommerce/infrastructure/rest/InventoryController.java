package org.example.pruebatecnicaecommerce.infrastructure.rest;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/inventory")
@RequiredArgsConstructor
public class InventoryController {

        @GetMapping
        public ResponseEntity<Map<String, String>> getAllInventory() {
                Map<String, String> response = new HashMap<>();
                response.put("message", "Inventory endpoint - functionality not implemented yet");
                return ResponseEntity.ok(response);
        }

        @GetMapping("/{productId}")
        public ResponseEntity<Map<String, String>> getInventoryByProductId(@PathVariable String productId) {
                Map<String, String> response = new HashMap<>();
                response.put("message", "Inventory by product ID endpoint - functionality not implemented yet");
                response.put("productId", productId);
                return ResponseEntity.ok(response);
        }
}