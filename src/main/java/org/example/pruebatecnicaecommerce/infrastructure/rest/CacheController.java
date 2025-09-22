package org.example.pruebatecnicaecommerce.infrastructure.rest;

import lombok.RequiredArgsConstructor;
import org.springframework.cache.CacheManager;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/admin/cache")
@RequiredArgsConstructor
@Tag(name = "Cache", description = "Operaciones administrativas para limpiar caches de la plataforma")
@SecurityRequirement(name = "bearerAuth")
public class CacheController {

    private final CacheManager cacheManager;

    @DeleteMapping("/clear/{cacheName}")
    public ResponseEntity<String> clearCache(@PathVariable String cacheName) {
        var cache = cacheManager.getCache(cacheName);
        if (cache != null) {
            cache.clear();
            return ResponseEntity.ok("Cache '" + cacheName + "' cleared successfully");
        }
        return ResponseEntity.notFound().build();
    }

    @DeleteMapping("/clear/all")
    public ResponseEntity<String> clearAllCaches() {
        cacheManager.getCacheNames().forEach(cacheName -> {
            var cache = cacheManager.getCache(cacheName);
            if (cache != null) {
                cache.clear();
            }
        });
        return ResponseEntity.ok("All caches cleared successfully");
    }

    @GetMapping("/list")
    public ResponseEntity<?> listCaches() {
        return ResponseEntity.ok(cacheManager.getCacheNames());
    }
}
