# Resumen técnico

Este documento describe la arquitectura y los componentes principales del servicio de ecommerce.

## Stack tecnológico

- **Java 17** + **Spring Boot 3.5**
- **Spring Data JPA** (PostgreSQL en producción, H2 en desarrollo/pruebas)
- **Flyway** para migraciones (`src/main/resources/db/migration`)
- **Spring Security** con JWT (`SecurityConfig`, `JwtAuthenticationFilter`)
- **Spring Retry** y wrappers de concurrencia (`ConcurrentOrderRepositoryWrapper`, `ConcurrentInventoryRepositoryWrapper`)
- **Redis** para cache distribuida (`RedisConfig`)
- **Swagger/OpenAPI** con `springdoc-openapi` (`OpenApiConfig`)

## Módulos

| Capa | Ubicación | Función |
| --- | --- | --- |
| Domain | `src/main/java/.../domain` | Entidades (`Order`, `Inventory`), reglas de negocio, puertos. |
| Application | `src/main/java/.../application/service` | Casos de uso: órdenes, inventario, autenticación. |
| Infrastructure | `src/main/java/.../infrastructure` | Adaptadores JPA, controladores REST, seguridad, cache. |
| Shared | `src/main/java/.../shared` | Excepciones y utilidades comunes. |
| Tests | `src/test/java/...` | Pruebas unitarias y de concurrencia. |

## Persistencia y concurrencia

- Entidades JPA en `infrastructure.persistence.order` e `infrastructure.persistence.inventory`.
- Wrappers de concurrencia aplican locking optimista + reintentos.
- `DeadlockHandlingService` maneja backoff ante deadlocks.
- Redis cachea consultas frecuentes; los adaptadores invalidan la cache cuando actualizan registros.

## API

- Controladores en `infrastructure.rest` con Swagger (`@Operation`, `@ApiResponses`).
- Swagger UI público: `http://localhost:8080/swagger-ui/index.html` (permitido en `SecurityConfig`).
- Colección Postman (`postman/Ecommerce_API.postman_collection.json`).

## Seguridad

- Roles y permisos configurados en `SecurityConfig`.
- Tokens emitidos vía `/auth/login` y `/auth/register`.
- Requiere `Authorization: Bearer <token>` en endpoints protegidos.

## Eventos

- Eventos (`OrderCreatedEvent`, `OrderStatusChangedEvent`) publicados mediante `EventPublisher`.
- `OrderEventListener` registra historial y notificaciones.

## Pruebas

- `mvn test` (JUnit 5 + Mockito).
- Cobertura con Jacoco (configurada en `pom.xml`).
- Pruebas de concurrencia (`OrderConcurrencyTest`).

## Flujo de desarrollo

1. Crear rama (`docs/gitflow.md`).
2. Ejecutar `mvn clean package` para compilar y generar documentación.
3. Levantar la app (`mvn spring-boot:run`).
4. Probar con Swagger/Postman.
5. Validar `mvn test` antes de subir cambios.

## Documentación automática de la API

Se integró Swagger/OpenAPI para explorar y probar endpoints.

**Cambios principales**
- Configuración de `springdoc-openapi` (dependencia, `OpenApiConfig`).
- Documentación de controladores (`OrderController`, `InventoryController`, etc.).
- Ajustes menores en endpoints para describir parámetros y respuestas.

**Pruebas**
- Acceso verificado a `http://localhost:8080/swagger-ui/index.html`.
- Endpoints documentados visibles y ejecutables (con token Bearer cuando es necesario).

## Formato de Pull Request

```
Se agregó documentación automática de la API con Swagger para facilitar la exploración y prueba de los endpoints disponibles.

**Cambios principales**
- Configuración de Swagger/OpenAPI en el proyecto.
- Documentación de los controladores REST principales.
- Ajustes menores en los endpoints para reflejar anotaciones correctas.

**Pruebas**
- Verificado acceso a la UI de Swagger (`/swagger-ui.html`).
- Confirmado que todos los endpoints documentados son visibles y probables desde la interfaz.
```

Ver `.github/pull_request_template.md` para el checklist completo.
