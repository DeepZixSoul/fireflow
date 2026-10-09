---
name: "fireflow-sync-security"
description: "Password hashing (Argon2), session management, root detection, Ktor sync service, WorkManager sync scheduling."
metadata:
  version: "1.0.0"
  category: "project"
  tags: ["fireflow", "security", "sync", "argon2", "ktor", "workmanager"]
  triggers:
    include: ["fireflow security", "fireflow sync", "password hashing fireflow", "session manager fireflow", "sync worker fireflow", "offline sync fireflow"]
  owners: ["@fireflow/maintainers"]
---

# FireFlow Sync & Security

## Password Hashing (Argon2)

File: `security/src/main/java/.../PasswordHasher.kt`

Uses `Argon2Kt` (v1.6.0, `com.lambdapioneer:argon2kt`):

```kotlin
private val argon2 = Argon2Kt()

fun hash(password: String): String {
    val result = argon2.hash(
        mode = Argon2Mode.ARGON2_ID,
        password = password.toByteArray(),
        salt = generateSalt(),  // 16 bytes, SecureRandom
        tCostInIterations = 3,
        mCostInKibibyte = 65536,
        parallelism = 4,
        hashLengthInBytes = 32
    )
    return result.encodedOutputAsString()
}

fun verify(password: String, encodedHash: String): Boolean {
    return try {
        argon2.verify(
            mode = Argon2Mode.ARGON2_ID,
            encoded = encodedHash,
            password = password.toByteArray()
        )
    } catch (e: Exception) { false }
}
```

**Gotcha**: Argon2KT v1.6.0 changed API — class is `Argon2Kt` (was `Argon2Kotlin`), params `tCostInIterations`/`mCostInKibibyte` (was `iterations`/`memoryInKib`).

## Session Management

File: `security/src/main/java/.../SessionManager.kt`

- `@Singleton class SessionManager @Inject constructor(@ApplicationContext)`
- Stores in EncryptedSharedPreferences (`fireflow_secure_session`): userId, username, displayName, role
- Provides `Flow<Long?>` for reactive user ID observation
- Functions: `saveSession()`, `clearSession()`, `isLoggedIn()`, `getUserId()`

## Root Detection

File: `security/src/main/java/.../RootDetector.kt`

Checks: build tags (`test-keys`), known root apps, su binary presence. Integrated en `MainActivity`: diálogo no bloqueante una vez por sesión.

## Offline Sync (Ktor + WorkManager)

### SyncService (`data/src/main/java/.../sync/SyncService.kt`)
- Ktor `HttpClient` configured in `SyncModule`
- Endpoints (POST): clients, pressure_groups, revisions, curve_points
- Each returns `Result<Unit>` with error handling
- **Certificate pinning** en release (pin-set generado en build); cleartext solo en debug

### SyncManager (`data/src/main/java/.../sync/SyncManager.kt`)
- Orchestrates full/partial sync
- Reads pending changes from Room tables
- Posts to SyncService endpoints
- Updates `sync_state` DataStore with last sync timestamp
- Conflict resolution: server wins (simplistic; refine for production)

### SyncWorker (`data/src/main/java/.../sync/SyncWorker.kt`)
- `WorkManager` periodic work (15 minutes)
- Constraint: `NetworkType.CONNECTED`
- Scheduled from `AppInitializer` on every app startup
- Uses Hilt `HiltWorkerFactory` via `Configuration.Provider` in `FireFlowApplication`

### NetworkMonitor (`data/src/main/java/.../sync/NetworkMonitor.kt`)
- `@Singleton @Inject constructor(@ApplicationContext)`
- Provides `Flow<Boolean>` via `ConnectivityManager` callback
- Used by SyncManager before initiating sync

## Critical Security Notes

- **DB passphrase**: aleatoria en Android Keystore con rekey desde `DB_PASSPHRASE` legacy (`database/security/`)
- **Primer arranque**: sin credenciales en el APK; `SetupScreen` crea el administrador en el dispositivo
- **Certificate pinning**: pin-set en release (`SERVER_PIN_*` en `app/build.gradle.kts`); cleartext solo debug
- **HTTPS**: release exige TLS; el servidor expone `sslConnector` cuando hay keystore
