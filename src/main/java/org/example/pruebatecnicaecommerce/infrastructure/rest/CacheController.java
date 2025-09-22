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
import org.springframework.cache.CacheManager;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/admin/cache")
@RequiredArgsConstructor
@Tag(name = "Cache", description = "Operaciones administrativas para limpiar caches de la plataforma")
@SecurityRequirement(name = "bearerAuth")
public class CacheController {

    private final CacheManager cacheManager;

    @PostMapping("/clear")
    @Operation(
            summary = "Limpiar todas las caches",
            description = "Elimina el contenido de todas las caches configuradas en la aplicacion"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Caches limpiadas",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = Map.class))),
            @ApiResponse(responseCode = "403", description = "Sin permisos",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<Map<String, String>> clearAllCaches() {
        cacheManager.getCacheNames().forEach(cacheName -> {
            if (cacheManager.getCache(cacheName) != null) {
                cacheManager.getCache(cacheName).clear();
            }
        });

        Map<String, String> response = new HashMap<>();
        response.put("message", "All caches cleared successfully");
        return ResponseEntity.ok(response);
    }

    @PostMapping("/clear/{cacheName}")
    @Operation(
            summary = "Limpiar cache especifica",
            description = "Elimina el contenido de la cache indicada. Devuelve error si no existe"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Cache limpiada",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = Map.class))),
            @ApiResponse(responseCode = "400", description = "Cache no encontrada",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "403", description = "Sin permisos",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<Map<String, String>> clearCache(
            @Parameter(description = "Nombre de la cache a limpiar", required = true, example = "orders")
            @PathVariable String cacheName) {
        if (cacheManager.getCache(cacheName) != null) {
            cacheManager.getCache(cacheName).clear();
            Map<String, String> response = new HashMap<>();
            response.put("message", "Cache '" + cacheName + "' cleared successfully");
            return ResponseEntity.ok(response);
        } else {
            Map<String, String> response = new HashMap<>();
            response.put("error", "Cache '" + cacheName + "' not found");
            return ResponseEntity.badRequest().body(response);
        }
    }

    @GetMapping("/names")
    @Operation(
            summary = "Listar caches registradas",
            description = "Devuelve los nombres de todas las caches disponibles"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Caches listadas",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = Map.class))),
            @ApiResponse(responseCode = "403", description = "Sin permisos",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<Map<String, Object>> getCacheNames() {
        Map<String, Object> response = new HashMap<>();
        response.put("cacheNames", cacheManager.getCacheNames());
        return ResponseEntity.ok(response);
    }
}
