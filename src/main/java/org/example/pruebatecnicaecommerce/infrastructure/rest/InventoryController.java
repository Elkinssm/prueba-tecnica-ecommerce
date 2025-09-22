package org.example.pruebatecnicaecommerce.infrastructure.rest;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.HashMap;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.example.pruebatecnicaecommerce.shared.error.ErrorResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/inventory")
@RequiredArgsConstructor
@Tag(name = "Inventory", description = "Consultas relacionadas con el inventario disponible")
@SecurityRequirement(name = "bearerAuth")
public class InventoryController {

    @GetMapping
    @Operation(
            summary = "Consultar inventario completo",
            description = "Devuelve un resumen del inventario registrado en el sistema. Actualmente retorna un mensaje placeholder"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Inventario recuperado",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = Map.class))),
            @ApiResponse(responseCode = "401", description = "No autenticado",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<Map<String, String>> getAllInventory() {
        Map<String, String> response = new HashMap<>();
        response.put("message", "Inventory endpoint - functionality not implemented yet");
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{productId}")
    @Operation(
            summary = "Consultar inventario por producto",
            description = "Devuelve el detalle de inventario asociado a un producto especifico. Actualmente retorna un mensaje placeholder"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Inventario recuperado",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = Map.class))),
            @ApiResponse(responseCode = "401", description = "No autenticado",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "Producto no encontrado",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<Map<String, String>> getInventoryByProductId(
            @Parameter(description = "Identificador del producto", required = true, example = "SKU-12345")
            @PathVariable String productId) {
        Map<String, String> response = new HashMap<>();
        response.put("message", "Inventory by product ID endpoint - functionality not implemented yet");
        response.put("productId", productId);
        return ResponseEntity.ok(response);
    }
}
