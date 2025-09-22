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
