package org.example.pruebatecnicaecommerce.infrastructure.rest;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.Positive;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.example.pruebatecnicaecommerce.domain.model.inventory.Inventory;
import org.example.pruebatecnicaecommerce.domain.model.inventory.InventoryRepository;
import org.example.pruebatecnicaecommerce.shared.error.ErrorResponse;
import org.example.pruebatecnicaecommerce.shared.utils.UuidUtils;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/inventory")
@RequiredArgsConstructor
@Tag(name = "Inventory", description = "Operaciones para consultar y ajustar niveles de inventario")
@SecurityRequirement(name = "bearerAuth")
public class InventoryController {

    private final InventoryRepository inventoryRepository;

    @GetMapping
    @Operation(
            summary = "Listar inventario",
            description = "Devuelve todos los registros de inventario con su stock actual"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Inventario recuperado",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = Inventory[].class))),
            @ApiResponse(responseCode = "401", description = "No autenticado",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<List<Inventory>> getAllInventory() {
        List<Inventory> inventoryList = inventoryRepository.findAll();
        return ResponseEntity.ok(inventoryList);
    }

    @GetMapping("/{productId}")
    @Operation(
            summary = "Consultar inventario por producto",
            description = "Devuelve el registro de inventario asociado a un producto"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Inventario recuperado",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = Inventory.class))),
            @ApiResponse(responseCode = "401", description = "No autenticado",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "Producto no encontrado",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<Inventory> getByProductId(
            @Parameter(description = "UUID del producto", required = true,
                    example = "550e8400-e29b-41d4-a716-446655440000")
            @PathVariable String productId) {
        UUID productUuid = UuidUtils.fromString(productId);
        return inventoryRepository.findByProductId(productUuid)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping("/{productId}/increase")
    @Operation(
            summary = "Incrementar stock",
            description = "Incrementa el stock del producto indicado"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Stock actualizado",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = Inventory.class))),
            @ApiResponse(responseCode = "400", description = "Solicitud inválida",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "401", description = "No autenticado",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<Inventory> increaseStock(
            @Parameter(description = "UUID del producto", required = true,
                    example = "550e8400-e29b-41d4-a716-446655440000")
            @PathVariable String productId,
            @Parameter(description = "Cantidad a sumar", required = true, example = "5")
            @RequestParam @Positive(message = "Quantity must be positive") int quantity) {
        UUID productUuid = UuidUtils.fromString(productId);
        Inventory inventory = inventoryRepository.findByProductId(productUuid)
                .orElseGet(() -> Inventory.create(productUuid, 0));

        inventory.release(quantity);
        inventoryRepository.save(inventory);
        return ResponseEntity.ok(inventory);
    }

    @PostMapping("/{productId}/decrease")
    @Operation(
            summary = "Reducir stock",
            description = "Reduce el stock del producto validando la disponibilidad"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Stock actualizado",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = Inventory.class))),
            @ApiResponse(responseCode = "400", description = "Solicitud inválida",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "401", description = "No autenticado",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "Producto no encontrado",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<Inventory> decreaseStock(
            @Parameter(description = "UUID del producto", required = true,
                    example = "550e8400-e29b-41d4-a716-446655440000")
            @PathVariable String productId,
            @Parameter(description = "Cantidad a restar", required = true, example = "2")
            @RequestParam @Positive(message = "Quantity must be positive") int quantity) {
        UUID productUuid = UuidUtils.fromString(productId);
        Inventory inventory = inventoryRepository.findByProductId(productUuid)
                .orElseThrow(() -> new IllegalArgumentException("Product not found"));

        inventory.reserve(quantity);
        inventoryRepository.save(inventory);
        return ResponseEntity.ok(inventory);
    }
}
