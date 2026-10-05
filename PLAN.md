# FireFlow — Plan Maestro del Proyecto

> Última actualización: 2026-08-08
> Este documento es la fuente de verdad del proyecto. Si se hace compact, leer esto primero.

---

## 1. Qué es FireFlow

Aplicación Android de gestión de grupos de presión (sistemas de bombas) para empresas de mantenimiento industrial. Permite gestionar clientes, grupos de presión, revisiones, curvas de presión, fotos, informes PDF y exportación CSV. Incluye un servidor Ktor backend para sincronización offline-first.

**Application ID**: `com.fireflow`
**Min SDK**: 26 | **Target/Compile SDK**: 35
**Kotlin**: 2.1.0 | **JVM**: 17 (Android), 21 (Server)

---

## 2. Arquitectura — 18 módulos Android + 1 server

```
app/                          ← Entry point, composition root
core/                         ← Utilidades puras (DateUtils, PasswordValidator, GeocoderUtil)
common/                       ← Shared Compose components + theme
domain/                       ← Models, repository interfaces, StatusCalculator (SIN Android/Compose)
data/                         ← Repository impls, mappers, sync engine (Ktor, WorkManager, DataStore)
database/                     ← Room DB, DAOs, entities, migrations (SQLCipher encrypted)
security/                     ← PasswordHasher (Argon2id), SessionManager (DataStore)
feature_login/                ← Login + ChangePassword
feature_clientes/             ← Client CRUD (list, detail, form)
feature_grupos/               ← Pressure groups + motors + pressure measurements
feature_revisiones/           ← Revisions management + checklist
feature_curvas/               ← Pressure curves (read-only charts)
feature_informes/             ← PDF report generation
feature_exportaciones/        ← CSV export
feature_fotografias/          ← CameraX + photo gallery
feature_configuracion/        ← Settings (sync toggle, logout)
feature_historial/            ← History/search (cross-entity)
feature_conversiones/         ← Unit converter (flow + pressure)
server/                       ← Ktor backend (PROYECTO GRADLE SEPARADO)
```

### Dirección de dependencias (Clean Architecture)

```
app → common → domain
app → feature_* → domain, core, common (NUNCA → data)
data → domain, database, security, core
database → (nada)
domain → (nada)
core → (nada)
security → (nada)
```

**Regla**: Los feature modules NUNCA dependen de `:data`. El wiring de DI ocurre en `:data/di/RepositoryModule.kt` via `@Binds`.

---

## 3. Stack tecnológico

### Android

| Categoría | Librería | Versión |
|-----------|----------|---------|
| UI | Compose BOM, Material3 | 2025.03.00, 1.3.1 |
| Navigation | navigation-compose | 2.8.5 |
| DI | Hilt (android, compiler, work) | 2.53.1 |
| DB | Room + SQLCipher | 2.6.1, 4.6.1 |
| Network | Ktor Client (Android) | 3.0.3 |
| Background | WorkManager | 2.10.0 |
| Charts | Vico | 2.1.0 |
| Paging | Paging 3 | 3.3.4 |
| Images | Coil Compose | 2.7.0 |
| Camera | CameraX | 1.4.1 |
| Security | Argon2kt | 1.6.0 |
| Serialization | kotlinx-serialization-json | 1.7.3 |
| Testing | JUnit 4.13.2, MockK 1.13.14, Turbine 1.2.0, Robolectric 4.14.1 |

### Server

| Categoría | Librería | Versión |
|-----------|----------|---------|
| Framework | Ktor (Netty) | 3.0.3 |
| ORM | Exposed | 0.48.0 |
| DB | PostgreSQL Driver | 42.7.4 |
| Pool | HikariCP | 5.1.0 |
| Migrations | Flyway | 10.20.1 |
| Auth | java-jwt | 4.4.0 |
| Password | BCrypt (jBCrypt) | 0.10.2 |
| Serialization | kotlinx-serialization | 1.7.3 |
| Logging | Logback + logstash-logback-encoder | 1.4.14, 7.4 |

---

## 4. Estado actual del proyecto

### Lo que funciona ✅

- **216 tests pasando** (79 Android + 67 new feature tests + 80 Server)
- Android app completa: login, CRUD clientes/grupos/revisiones, curvas, fotos, PDF, CSV, historial, convertidor, ajustes
- Server completo: auth JWT, sync API (4 colecciones), health check, Docker multi-stage
- Sync offline-first: dirty tracking, parallel sync, server-wins conflict resolution
- SQLCipher encrypted database (DB version 7)
- Bottom Navigation Bar (4 tabs), status indicators, delete confirmations
- Camera integration (CameraX), photo gallery
- **FASE 1-6 completadas** (Clean Architecture, tests, UserRepository, CORS, limpieza, docs)
- **Auditoría de seguridad completada** — 25 vulnerabilidades resueltas (SEC-1 a SEC-6)

---

## 5. Decisiones técnicas clave

| Decisión | Valor | Por qué |
|----------|-------|---------|
| DB passphrase | `local.properties` → `BuildConfig` | Nunca hardcodeada en código fuente |
| Admin credentials | Seeded on first run, forced password change | Never hardcode in public repos |
| Exposed version | 0.48.0 (no 0.56.0) | 0.56.0 tiene breaking changes con `eq` y `selectAll().where{}` |
| Server port | 9090 | Evita conflictos con 8080 |
| Java version | 21 (no 25) | Kotlin 2.1.0 incompatible con Java 25 |
| H2 reserved words | Quote `"role"`, `"name"`, `"power"`, `"date"` | H2 los reserva, causa test failures |
| Timestamps | `BIGINT` (epoch millis) | Sync simplicity, avoids timezone issues |
| Conflict resolution | Server wins (LWW) | Contrato existente en Android |
| Material3 only | NO Material2 | 1.3.1 only |
| Domain purity | NO `@Immutable` in domain models | Domain module has NO Compose dependency |
| DI wiring | `RepositoryModule` in `:data` | All `@Binds` for repo interfaces live here |
| Outer Scaffold | `contentWindowInsets = WindowInsets(0,0,0,0)` | Prevents double insets |
| Status bar | Red via `window.statusBarColor` | `isAppearanceLightStatusBars = false` |

---

## 6. Comandos de build

```bash
# Android build
JAVA_HOME=/usr/lib/jvm/java-21-openjdk-amd64 ./gradlew assembleDebug

# Android tests
JAVA_HOME=/usr/lib/jvm/java-21-openjdk-amd64 ./gradlew testDebugUnitTest

# Server build
cd server && JAVA_HOME=/usr/lib/jvm/java-21-openjdk-amd64 ./gradlew build -x test

# Server tests
cd server && JAVA_HOME=/usr/lib/jvm/java-21-openjdk-amd64 ./gradlew test

# Server run locally
cd server && JAVA_HOME=/usr/lib/jvm/java-21-openjdk-amd64 ./gradlew run

# Docker
cd server && docker compose build && docker compose up -d
```

**APK output**: `app/build/outputs/apk/debug/app-debug.apk`

---

## 7. Plan de Refactorización — 6 Fases

### Estado: ✅ COMPLETADAS (6/6)

| Fase | Descripción | Estado | Tests |
|------|-------------|--------|-------|
| FASE 1 | Clean Architecture estricta — feature modules solo dependen de `:domain`, `:core`, `:common`. Módulo `:ui` eliminado. | ✅ COMPLETADA | — |
| FASE 2 | Tests de feature modules — 67 tests en `feature_clientes` (23), `feature_grupos` (26), `feature_revisiones` (18) | ✅ COMPLETADA | +67 |
| FASE 3 | Server UserRepository — tabla `Users` + queries extraídas de `AuthService` a `UserRepository` | ✅ COMPLETADA | — |
| FASE 4 | Server seguridad — CORS configurable, rate limiting 5 req/min, Docker healthcheck wget, mem_limit 512m | ✅ COMPLETADA | — |
| FASE 5 | Limpieza de código — StatusCalculator consolidado, dead code eliminado, wildcard imports corregidos | ✅ COMPLETADA | — |
| FASE 6 | Skills y documentación — DB version=7, endpoints actualizados, Known Issues resueltos | ✅ COMPLETADA | — |

---

## 8. Archivos clave del proyecto

### Android (por módulo)

| Módulo | Archivos principales |
|--------|---------------------|
| `app` | `FireFlowApplication.kt`, `AppInitializer.kt`, `MainActivity.kt` |
| `core` | `PasswordValidator.kt`, `DateUtils.kt`, `GeocoderUtil.kt` |
| `common` | `theme/Theme.kt`, `components/FireFlowTopBar.kt`, `components/StatusIndicator.kt`, `components/ConfirmDeleteDialog.kt` |
| `domain` | `model/*.kt` (12 models), `repository/*.kt` (9 interfaces), `datasource/RemoteDataSource.kt`, `util/StatusCalculator.kt` |
| `data` | `repository/*Impl.kt` (9), `mapper/*.kt` (8), `sync/SyncManager.kt`, `sync/SyncWorker.kt`, `di/RepositoryModule.kt` |
| `database` | `AppDatabase.kt`, `Migrations.kt` (v7), `dao/*.kt` (9), `entity/*.kt` (9), `di/DatabaseModule.kt` |
| `security` | `PasswordHasher.kt` (Argon2id), `SessionManager.kt` (DataStore) |
| `ui` | ELIMINADO — migrado a `app/` |

### Server

| Archivo | Propósito |
|---------|-----------|
| `Application.kt` | Entry point, module wiring |
| `config/ServerConfig.kt` | Env vars config |
| `config/DatabaseConfig.kt` | HikariCP + Flyway |
| `features/CORS.kt` | CORS configurable via ALLOWED_ORIGINS |
| `features/Security.kt` | JWT auth plugin |
| `features/StatusPages.kt` | Global error handler |
| `routes/AuthRoutes.kt` | POST /api/v1/auth/login |
| `routes/SyncRoutes.kt` | 4 sync endpoints |
| `routes/HealthRoutes.kt` | GET /health |
| `services/AuthService.kt` | Login + changePassword (usa UserRepository) |
| `services/SyncService.kt` | Generic sync<T>() |
| `repositories/*.kt` | Client, PressureGroup, Revision, Curve, User |
| `utils/JwtUtils.kt` | Token generation |
| `utils/PasswordUtils.kt` | BCrypt hashing |
| `Dockerfile` | Multi-stage, non-root, wget healthcheck |
| `docker-compose.yml` | Ktor + PostgreSQL |

---

## 9. Database schema (Android — Room v7)

```
clients (id PK)
pressure_groups (id PK, client_id FK → clients)
motors (id PK, group_id FK → pressure_groups)
revisions (id PK, group_id FK → pressure_groups)
pressure_measurements (id PK, motor_id FK → motors)
curve_points (id PK, revision_id FK → revisions, motor_id FK → motors)
checklist_items (id PK, revision_id FK → revisions)
photos (id PK, revision_id FK → revisions)
users (id PK, username UNIQUE)
```

Migraciones: `MIGRATION_1_2` → `MIGRATION_6_7` (7 versiones)

---

## 10. Server schema (PostgreSQL — Flyway V1-V5)

```
users (BIGSERIAL PK, username UNIQUE, password_hash, role, must_change_password)
clients (BIGINT PK, name, cif, address, ...)
pressure_groups (BIGINT PK, client_id FK → clients)
revisions (BIGINT PK, group_id FK → pressure_groups, checklist_results TEXT)
curve_points (BIGINT PK, revision_id FK → revisions, motor_id FK)
```

---

## 11. Known issues (documentados)

| Issue | Severidad | Ubicación | Estado |
|-------|-----------|-----------|--------|
| CORS `anyHost()` fallback when `ALLOWED_ORIGINS` not set in production | CRITICO | `server/features/CORS.kt` | ✅ RESUELTO — Solo permite en dev, rechaza en prod |
| Sin rate limiting per-user (solo per-IP) | ALTO | `server/services/AuthService.kt` | ✅ RESUELTO — Agregado brute force per-user con lockout 15min |
| Modelos duplicados server vs domain | BAJO | `server/models/*` vs `domain/model/*` | Aceptar (server models ≠ domain models) |
| `isDirty` en modelos server (no se usa) | BAJO | `server/models/*.kt` | Aceptar (sync contract) |

---

## 12. Cómo continuar después de un compact

1. **Leer este archivo completo** (PLAN.md)
2. **Leer `AGENTS.md`** para reglas del proyecto
3. **Leer `.agents/skills/fireflow-architecture/SKILL.md`** para convenciones
4. **Verificar estado**: `JAVA_HOME=/usr/lib/jvm/java-21-openjdk-amd64 ./gradlew assembleDebug` (Android) y `cd server && JAVA_HOME=/usr/lib/jvm/java-21-openjdk-amd64 ./gradlew test` (Server)
5. **Continuar la fase pendiente** más arriba en "Sección 7: Plan de Refactorización"
6. **Marcar fases completadas** actualizando el estado en la Sección 7

---

## 13. Hosting y deploy

- **Local LAN**: Server en `http://ip:9090` (sin HTTPS aún)
- **Docker**: `docker compose up -d` en `server/`
- **Android APK**: `app/build/outputs/apk/debug/app-debug.apk`
- **HTTPS**: Preparado (env vars SSL_* configuradas pero no activas). Ver checklist de migración en la sección anterior del `server/PLAN.md` viejo (fue eliminado, pero el checklist era: Let's Encrypt → keystore Java → env vars → docker-compose → Android baseUrl)

---

## 14. Auditoría de Seguridad

La auditoría completa de seguridad (25 vulnerabilidades resueltas, SEC-1 a SEC-6) y el backlog de mejoras están documentados en `SECURITY_AUDIT.md` (archivo privado, no incluido en el repositorio público).

