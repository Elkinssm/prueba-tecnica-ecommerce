## Prueba Técnica Ecommerce

Aplicación de ecommerce construida con Java 17 y Spring Boot 3.5, organizada por capas (Domain, Application, Infrastructure) con énfasis en concurrencia, consistencia de datos, eventos de dominio, cache distribuida y autenticación JWT.

### Stack tecnológico

- Java 17 + Spring Boot 3.5
- Spring Data JPA (PostgreSQL en producción, H2 en desarrollo/pruebas)
- Flyway para migraciones (carpeta `src/main/resources/db/migration`)
- Spring Security con JWT (filtro y configuración en `infrastructure.config`)
- Concurrencia: locking optimista + reintentos (`ConcurrentOrderRepositoryWrapper`, `ConcurrentInventoryRepositoryWrapper`), `@Version`, backoff ante deadlocks
- Redis para cache distribuida (config, `@Cacheable`/`@CacheEvict` en adaptadores)
- Testing: JUnit 5 + Mockito + JaCoCo

### Arquitectura (resumen)

Ver `docs/architecture.md`. Diagrama directo a continuación:

![Arquitectura - Flujo principal](docs/flow-architecture.png)

#### Arquitectura (documento completo)

<details>
<summary>Ver contenido de docs/architecture.md</summary>

# Visión general de la arquitectura

## Contexto

La aplicación de ecommerce está organizada en capas: controladores REST, servicios de aplicación, lógica de dominio y adaptadores de infraestructura. El siguiente diagrama resume los componentes principales y sus interacciones.

```
+-------------------+         +-------------------------+
|     REST API      |<------->|     Servicios de        |
| (Controllers)     |         |     Aplicación          |
+---------+---------+         +-----------+-------------+
          |                               |
          v                               v
+---------+---------+         +-----------+-------------+
|  Infraestructura  |<------->|      Dominio            |
| (Adapters, Config)|         | (Entidades, Puertos)    |
+---------+---------+         +-----------+-------------+
          |                               |
          v                               v
+---------+---------+         +-----------+-------------+
| Persistencia (JPA)|         |  Eventos / Mensajería   |
| Redis Cache       |         |  (Spring Events)        |
+-------------------+         +-------------------------+
```

## Diagrama de componentes (PlantUML)

![Diagrama de componentes](docs/flow-architecture.png)

## Flujo de datos

1. **Request**: llega a los controladores REST (documentados con Swagger).
2. **Caso de uso**: los servicios de aplicación coordinan la lógica de negocio.
3. **Dominio**: las entidades aplican invariantes y exponen puertos para persistencia.
4. **Infraestructura**: los adaptadores implementan los puertos usando JPA y gestionan el locking optimista.
5. **Persistencia/Caché**: PostgreSQL almacena el estado; Redis sirve para lecturas recurrentes.
6. **Eventos**: los eventos de dominio notifican cambios de estado a otros componentes.

</details>

### Manejo de concurrencia

La plataforma protege los agregados críticos mediante una combinación de bloqueo optimista y reintentos exponenciales:

- `ConcurrencyConfig` habilita `@EnableRetry`, `@EnableAsync` y `@EnableTransactionManagement` para que las anotaciones de reintentos y transacciones funcionen en toda la aplicación.
- Los wrappers `ConcurrentOrderRepositoryWrapper` y `ConcurrentInventoryRepositoryWrapper` delegan en los repositorios JPA, validan versiones (`@Version`) y aplican reintentos automáticos ante `OptimisticLockingFailureException` con backoff exponencial.
- `DeadlockHandlingService` encapsula operaciones críticas con reintentos adicionales y ordenamiento de bloqueos para minimizar condiciones de interbloqueo y permite ejecutar lotes con commit escalonado.

Los servicios de aplicación (`CreateOrderService`, `PayOrderService`, etc.) utilizan estos componentes para mantener la consistencia del stock y los pedidos aun bajo alta concurrencia.

### Ciclo de vida de pedidos

El agregado `Order` gobierna las transiciones válidas a través de la enumeración `OrderStatus`, que actúa como una máquina de estados finita:

1. **CREATED** → puede pasar a **PAID** o **CANCELLED** según el pago o una cancelación explícita.
2. **PAID** → transiciona a **SHIPPED** cuando se completa el envío o a **CANCELLED** si se revierte.
3. **SHIPPED** → sólo permite el cambio a **DELIVERED** cuando se confirma la entrega.
4. **DELIVERED**/**CANCELLED** → estados terminales sin transiciones adicionales.

Las validaciones de `Order` y `OrderStatus` impiden movimientos no válidos y registran cada cambio a través de servicios de aplicación como `ShipOrderService` y `CancelOrderService` antes de persistir la versión más reciente del agregado.

### Flujo de eventos y notificaciones

Los eventos de dominio capturan hitos relevantes del ciclo de vida y disparan integraciones secundarias:

- El agregado publica `OrderCreatedEvent` y `OrderStatusChangedEvent` mediante el puerto `EventPublisher`. La infraestructura implementa este puerto con `SpringEventPublisher`, delegando en el `ApplicationEventPublisher` de Spring.
- `OrderEventListener` reacciona a estos eventos para persistir el historial (`OrderHistoryService`) y orquestar notificaciones a través de `NotificationService` (implementación mock para desarrollo/pruebas).

Este flujo desacopla las integraciones asíncronas del procesamiento principal del pedido y permite añadir canales externos sin modificar el dominio.

### Puesta en marcha rápida

1. Pre-requisitos: JDK 17, Docker y Docker Compose.
2. Iniciar servicios de soporte (Redis y RedisInsight):
   ```bash
   docker compose up -d
   ```
3. Ejecutar la aplicación con Maven:
   ```bash
   mvn spring-boot:run
   ```
4. Explorar la API: `http://localhost:8080/swagger-ui.html` (documentación OpenAPI).

#### Endpoints base (API v1)

- `POST /api/v1/auth/login`
- `POST /api/v1/auth/register`
- `GET /api/v1/inventory`
- `GET /api/v1/inventory/{productId}`
- `POST /api/v1/inventory/{productId}/increase?quantity=5`
- `POST /api/v1/inventory/{productId}/decrease?quantity=2`
- `GET /api/v1/orders`
- `POST /api/v1/orders`
- `GET /api/v1/orders/{publicId}`
- `GET /api/v1/orders/{publicId}/status`
- `POST /api/v1/orders/{publicId}/pay`
- `POST /api/v1/orders/{publicId}/ship`
- `POST /api/v1/orders/{publicId}/cancel`
- `GET /api/v1/orders/{publicId}/history`

Para ejecutar los tests de regresión utiliza `mvn test`.

### Resumen técnico (documento completo)

<details>
<summary>Ver contenido de docs/technical-overview.md</summary>

# Resumen técnico

Este documento describe la arquitectura y los componentes principales del servicio de ecommerce.

## Stack tecnológico (ver sección principal del README)

## Módulos

| Capa           | Ubicación                               | Función                                                       |
| -------------- | --------------------------------------- | ------------------------------------------------------------- |
| Domain         | `src/main/java/.../domain`              | Entidades (`Order`, `Inventory`), reglas de negocio, puertos. |
| Application    | `src/main/java/.../application/service` | Casos de uso: órdenes, inventario, autenticación.             |
| Infrastructure | `src/main/java/.../infrastructure`      | Adaptadores JPA, controladores REST, seguridad, cache.        |
| Shared         | `src/main/java/.../shared`              | Excepciones y utilidades comunes.                             |
| Tests          | `src/test/java/...`                     | Pruebas unitarias y de concurrencia.                          |

## Persistencia y concurrencia

- Entidades JPA en `infrastructure.persistence.order` e `infrastructure.persistence.inventory`.
- Wrappers de concurrencia aplican locking optimista + reintentos.
- `DeadlockHandlingService` maneja backoff ante deadlocks.
- Redis cachea consultas frecuentes; los adaptadores invalidan la cache cuando actualizan registros.

## API

- Controladores en `infrastructure.rest` con Swagger (`@Operation`, `@ApiResponses`).
- Swagger UI público: `http://localhost:8080/swagger-ui.html` (permitido en `SecurityConfig`).
- Colección Postman (`postman/Ecommerce_API_Workflow.postman_collection.json`).

## Seguridad

- Roles y permisos configurados en `SecurityConfig`.
- Tokens emitidos vía `/api/v1/auth/login` y `/api/v1/auth/register`.
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

- Acceso verificado a `http://localhost:8080/swagger-ui.html`.
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

</details>

#### Modelo de ramas (GitFlow - documento completo)

<details>
<summary>Ver contenido de docs/gitflow.md</summary>

# Modelo de ramas (GitFlow)

Este proyecto utiliza un flujo de trabajo basado en GitFlow. Las siguientes ramas son permanentes:

- `main`: contiene el código listo para producción. Solo se fusionan versiones etiquetadas (releases).
- `develop`: rama de integración donde se agregan los cambios aprobados antes de preparar un release.

Cuando comienza una tarea, se crea una rama temporal desde `develop` y se elimina al finalizar:

- `feature/<tema>`: desarrollo de nuevas funcionalidades o documentación.
- `fix/<incidente>`: correcciones encontradas durante QA o desarrollo.
- `release/<versión>`: preparación del lanzamiento (incremento de versión, pruebas de humo, notas de cambio).
- `hotfix/<incidente>`: correcciones urgentes que parten de `main`. Tras validarse se fusionan en `main` y `develop`.

## Flujo recomendado

1. Actualiza tus referencias: `git fetch origin`.
2. Cambia a `develop`: `git checkout develop`.
3. Crea tu rama: `git checkout -b feature/<tema>`.
4. Haz commits pequeños usando mensajes convencionales (`feat:`, `fix:`, `docs:`, etc.).
5. Rebasea contra `origin/develop` de forma periódica:
   ```bash
   git fetch origin
   git rebase origin/develop
   ```
6. Abre un Pull Request hacia `develop`, ejecuta las pruebas y solicita revisión.
7. Al fusionar, usa squash o rebase merge (según las reglas del repo) y elimina la rama temporal.

## Releases

1. Crea `release/<versión>` desde `develop` cuando no haya nuevas funcionalidades por agregar.
2. Realiza los ajustes de versión, documentación y prueba completa.
3. Fusiona la rama de release en `main`, etiqueta la versión y luego regresa los cambios a `develop`.

## Hotfixes

1. Crea `hotfix/<incidente>` desde `main`.
2. Aplica la corrección y sube el parche de versión si corresponde.
3. Fusiona el hotfix en `main` (etiqueta la versión) y luego en `develop`.

Mantén un historial lineal utilizando `git pull --rebase` y rebaseando tu rama antes de abrir el Pull Request.

</details>

- REST API (controladores en `infrastructure.rest`)
- Servicios de aplicación (casos de uso en `application.service`)
- Dominio (entidades y lógica de negocio en `domain`)
- Infraestructura (JPA, seguridad, cache, eventos)
- Persistencia/Caché: JPA + Redis
- Eventos: `OrderCreatedEvent`, `OrderStatusChangedEvent` publicados con `EventPublisher` y escuchados por `OrderEventListener` (historial y notificaciones)

### Persistencia y concurrencia

- Entidades JPA y `@Version` para locking optimista
- Wrappers concurrentes con reintentos ante `OptimisticLockException`
- Manejo de deadlocks con backoff exponencial
- Cache Redis para lecturas frecuentes; invalidación en operaciones de escritura

### Seguridad (JWT)

- Endpoints públicos: `/api/v1/auth/register`, `/api/v1/auth/login`
- Endpoints protegidos requieren `Authorization: Bearer <token>`
- Roles y permisos definidos en `SecurityConfig`

### Migraciones (Flyway)

- Archivos en `src/main/resources/db/migration`
- Ejemplos relevantes:
  - `V3__add_product_code_and_public_id.sql`: agrega `product_code` a `inventory` y `public_id` a `orders`
  - `V7__fix_product_code_update.sql`: corrige `product_code` para registros existentes (H2-friendly)
  - `V8__create_order_status_history_table.sql`: historial de cambios de estado de órdenes

### Cache distribuida (Redis)

Para desarrollo local puedes levantar Redis con Docker Compose:

```bash
docker compose up -d
```

Herramientas de administración de cache: `CacheController` (`/admin/cache`) para limpiar caches específicos o todos.

### API

- Controladores en `infrastructure.rest` (`OrderController`, `AuthController`, etc.)
- Nota sobre documentación OpenAPI/Swagger: existe soporte y anotaciones en controladores. UI disponible en `http://localhost:8080/swagger-ui.html`.

#### Colección Postman

- Colección: `postman/Ecommerce_API_Workflow.postman_collection.json`
- Cómo usar:
  1. Importa la colección en Postman.
  2. Configura el Environment con `baseUrl = http://localhost:8080/api/v1`.
  3. Ejecuta `1.1 Login administrador` o `1.3 Login usuario` para obtener el token.
  4. La colección añade el header `Authorization: Bearer {{authToken}}` automáticamente en las requests protegidas.

### Cómo ejecutar

Requisitos: Java 17, Maven, Docker (opcional para Redis).

1. Compilar y correr pruebas

```bash
mvn clean test
```

2. Ejecutar la aplicación

```bash
mvn spring-boot:run
```

3. Flujo básico

- Registro/Login: `POST /api/v1/auth/register` y `POST /api/v1/auth/login`
- Usa el token en `Authorization: Bearer <token>` para invocar endpoints protegidos

### Pruebas

- Ejecutar todo: `mvn test`
- Cobertura: generada con JaCoCo (ver `target/site/jacoco/index.html`)
- Pruebas de concurrencia: `OrderConcurrencyTest`

### Flujo de desarrollo (GitFlow + Conventional Commits)

Ver `docs/gitflow.md`.

- Ramas:
  - `main`: producción
  - `develop`: integración
  - `feature/<tema>`, `fix/<incidente>`, `release/<versión>`, `hotfix/<incidente>`
- Commits: usar mensajes convencionales (`feat:`, `fix:`, `docs:`, `refactor:`, `test:`, `chore:`)

### Troubleshooting

- Flyway: errores de versiones duplicadas → unificar migraciones conflictivas
- Redis: valores stale → limpiar caché (`/admin/cache/clear`)
- Historial de órdenes vacío/incompleto → validar publicación/escucha de `OrderStatusChangedEvent` y la tabla `order_status_history`
- Swagger 500 / `NoSuchMethodError` → revisar compatibilidad de versión `springdoc-openapi`

### Recursos adicionales

- Documentación técnica: `docs/technical-overview.md`
- Arquitectura: `docs/architecture.md`, `docs/flow-architecture.png`, `docs/architecture.plantuml`
- Flujo Git: `docs/gitflow.md`

---

Si necesitas habilitar Swagger, ejecutar pruebas de carga/concurrencia o ajustar las políticas de cache, revisa las secciones respectivas y los módulos de `infrastructure`/`application` implicados.
