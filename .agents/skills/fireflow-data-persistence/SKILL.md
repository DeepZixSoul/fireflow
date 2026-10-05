---
name: "fireflow-data-persistence"
description: "Room + SQLCipher encrypted database: setup, entities, DAOs, DataStore, JSON storage patterns."
metadata:
  version: "1.0.0"
  category: "project"
  tags: ["fireflow", "database", "sqlcipher", "room", "datastore"]
  triggers:
    include: ["fireflow database", "room sqlcipher", "entity definition fireflow", "dao fireflow", "json checklist room"]
  owners: ["@fireflow/maintainers"]
---

# FireFlow Data Persistence

## Database Setup (SQLCipher)

File: `database/src/main/java/.../di/DatabaseModule.kt`

```kotlin
// CRITICAL: Must call before building Room
System.loadLibrary("sqlcipher")

val passphrase = DB_PASSPHRASE.toByteArray()
val factory = SupportOpenHelperFactory(passphrase)

Room.databaseBuilder(context, AppDatabase::class.java, DB_NAME)
    .openHelperFactory(factory)
    .addCallback(seedCallback)
    .build()
```

**Rules:**
- `SupportOpenHelperFactory` takes `byte[]` (use `String.toByteArray()`)
- Always call `System.loadLibrary("sqlcipher")` before `Room.databaseBuilder()`
- Package: `net.zetetic.database.sqlcipher.SupportOpenHelperFactory`
- DB passphrase is read from `local.properties` via `BuildConfig.DB_PASSPHRASE` (never hardcoded)
- `SeedDataCallback` is a no-op (real seeding is in `AppInitializer`)

## Entities (9 tables)

| Entity | Table | Key FK |
|---|---|---|
| `UserEntity` | `users` | — |
| `ClientEntity` | `clients` | — |
| `PressureGroupEntity` | `pressure_groups` | `client_id` → Client |
| `MotorEntity` | `motors` | `group_id` → PressureGroup |
| `PressureMeasurementEntity` | `pressure_measurements` | `motor_id` → Motor |
| `RevisionEntity` | `revisions` | `group_id` → PressureGroup |
| `CurvePointEntity` | `curve_points` | `revision_id` → Revision |
| `PhotoEntity` | `photos` | `revision_id` → Revision |
| `ChecklistItemEntity` | `checklist_items` | — |

## Checklist Storage (JSON in RevisionEntity)

Checklist results are stored as a JSON string in `checklist_results` column:
```kotlin
@ColumnInfo(name = "checklist_results")
val checklistResults: String = "{}"  // Map<String, Boolean> serialized
```

**Serialization (in RevisionMapper)**:
```kotlin
val json = buildJsonObject {
    domain.checklistResults.forEach { (label, checked) ->
        put(label, checked)
    }
}.toString()
```

**Deserialization**:
```kotlin
val map = mutableMapOf<String, Boolean>()
val json = Json.parseToJsonElement(entity.checklistResults).jsonObject
json.forEach { (key, value) -> map[key] = value.jsonPrimitive.boolean }
```

## DAO Patterns

- **Base interface**: not used; each DAO defines its own CRUD
- **Upsert**: `@Insert(onConflict = OnConflictStrategy.REPLACE)` for simpler save
- **Queries**: suspend for mutations, `Flow<List<T>>` for reactive reads
- **Search**: `WHERE name LIKE '%' || :query || '%'` for simple text search

## DataStore (2 instances)

| Name | File | Purpose |
|---|---|---|
| `session` | `Context.sessionDataStore` | SessionManager: userId, username, displayName, role, token |
| `seed_state` | `Context.seedDataStore` | AppInitializer: `database_seeded` boolean flag |

Both defined as top-level extension properties:
```kotlin
private val Context.xxxDataStore by preferencesDataStore(name = "xxx")
```
