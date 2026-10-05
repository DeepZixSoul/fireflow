---
name: "fireflow-architecture"
description: "Project architecture: 19-module Clean Architecture with strict layer separation and dependency rules."
metadata:
  version: "1.0.0"
  category: "project"
  tags: ["fireflow", "architecture", "clean-architecture", "modules"]
  triggers:
    include: ["fireflow architecture", "module structure", "project layers", "clean architecture fireflow"]
  owners: ["@fireflow/maintainers"]
---

# FireFlow Architecture

## Module Map (19 modules)

```
app/                        → AGP application, Hilt entry point, Configuration.Provider
ui/                         → Navigation graph (FireFlowNavHost), shared widgets
common/                     → Theme (FireFlowTheme, colors, typography), constants
domain/                     → Pure Kotlin: models, repository interfaces, use cases (future)
data/                       → Repository implementations, mappers, sync layer, DI modules
database/                   → Room + SQLCipher: AppDatabase, entities, DAOs, Converters
security/                   → Argon2 hashing, SessionManager, RootDetector
core/                       → Shared utilities (DateUtils)
feature_login/              → LoginView(odel), LoginScreen
feature_clientes/           → CRUD clients
feature_grupos/             → CRUD pressure groups
feature_revisiones/         → CRUD revisions, checklist
feature_curvas/             → Curves entry, comparison
feature_informes/           → PDF reports (PdfGenerator)
feature_exportaciones/      → CSV/Excel export
feature_fotografias/        → Photo capture, gallery
feature_configuracion/      → Settings (dark mode, sync toggle)
feature_historial/          → Search revisions history
feature_conversiones/       → Unit converter (flow, pressure)
```

## Layer Dependency Rules

- **`app`** → depends on ALL modules (orchestrator)
- **`ui`** → depends on ALL feature modules + `common`
- **Feature modules** → depend on `domain`, `core`, `data`, `common`. NEVER depend on other features.
- **`data`** → depends on `domain`, `database`, `security`, `core`
- **`database`** → depends ONLY on `core`. No Hilt (has its own DI module).
- **`security`** → depends on `core`. No Hilt (has its own DI module).
- **`domain`** → pure Kotlin. Zero dependencies (no Android, no Hilt).
- **`core`** → pure Android utilities. Lightweight.
- **`common`** → Compose theme + constants. No business logic.

## Key Conventions

- Each feature module: `build.gradle.kts` depends on `:domain`, `:core`, `:data`, `:common`
- ViewModel → Repository → DAO (never ViewModel → DAO directly)
- `@HiltViewModel` with `@Inject constructor`
- `SavedStateHandle` navigation args: always use `savedStateHandle.get<String>("key")?.toLongOrNull()`
- Hilt modules in `:data/di/`, `:database/di/`, `:security/di/`
- DI: `@Module @InstallIn(SingletonComponent::class)` for singletons; builders for feature modules

## When NOT to do

- Do NOT add Android dependencies to `:domain`
- Do NOT reference feature A from feature B
- Do NOT create circular dependencies between modules
- Do NOT put business logic in UI layer
- Do NOT use `savedStateHandle["key"] ?: -1L` (will crash: String→Long cast)
