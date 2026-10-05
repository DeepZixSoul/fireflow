---
name: "igrupos-build-quality"
description: "Build configuration: ProGuard rules, Room migrations, build variants, CI, API contract, versioning."
metadata:
  version: "1.0.0"
  category: "project"
  tags: ["igrupos", "build", "proguard", "gradle", "ci", "api-contract"]
  triggers:
    include: ["igrupos build", "proguard igrupos", "room migration igrupos", "api contract igrupos", "ci pipeline igrupos"]
  owners: ["@igrupos/maintainers"]
---

# IGrupos Build & Quality

## ProGuard Rules

File: `app/proguard-rules.pro`

**Must keep** (already configured):
- Hilt/DI: `@Keep` annotations, `@HiltAndroidApp`, `@Inject` constructors
- Room entities: `@Entity`, `@ColumnInfo` — Room uses reflection
- SQLCipher: `net.zetetic.database.sqlcipher.*` — native JNI bridge
- Ktor: `io.ktor.*` — reflection-based content negotiation
- Kotlin Serialization: `@Serializable` classes, serializers
- DataStore: `androidx.datastore.*`
- WorkManager: `androidx.work.*`

**Never add** generic `-keep class *` rules; keep specific to what reflection requires.

## Room Migrations

Current **version = 7** in `@Database`. Migrations registered in `DatabaseModule`:
`MIGRATION_1_2, MIGRATION_2_4, MIGRATION_3_4, MIGRATION_4_5, MIGRATION_5_6, MIGRATION_6_7`.

**Export schema** is enabled (`exportSchema = true`). Schema JSONs go to:
`database/schemas/` — must be committed to git for migration validation.

## Build Variants

| Variant | applicationIdSuffix | minifyEnabled | Uses |
|---|---|---|---|
| **debug** | `.debug` | false | Dev testing |
| **release** | (none) | true | Play Store |

Java target: 17. Min SDK: 26. Target SDK: 35. Gradle: 8.11.1.

## CI Pipeline (Planned)

Recommended GitHub Actions workflow:
```yaml
# .github/workflows/build.yml
on: [push, pull_request]
jobs:
  build:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v4
      - uses: actions/setup-java@v4
        with: { java-version: '21', distribution: 'temurin' }
      - uses: actions/android-sdk-setup@v1
      - run: ./gradlew assembleDebug lint test
```

**JAVA_HOME must be Java 21** (not 25). Gradle 8.11.1 does not support Java 25.

## API Contract

Sync endpoints (Ktor server, port 9090):

| Endpoint | Auth | Data | Method |
|---|---|---|---|
| `/api/v1/clients/sync` | Bearer JWT | `SyncRequest<Client>` → `SyncResponse<Client>` | POST |
| `/api/v1/groups/sync` | Bearer JWT | `SyncRequest<PressureGroup>` → `SyncResponse<PressureGroup>` | POST |
| `/api/v1/revisions/sync` | Bearer JWT | `SyncRequest<Revision>` → `SyncResponse<Revision>` | POST |
| `/api/v1/curves/sync` | Bearer JWT | `SyncRequest<CurvePoint>` → `SyncResponse<CurvePoint>` | POST |
| `/api/v1/auth/login` | None | `LoginRequest` → `LoginResponse` | POST |
| `/health` | None | — | GET |

Rate limiting: 5 req/min per IP on `/login`.

## Versioning

Follow semantic versioning in `defaultConfig`:
```kotlin
versionCode = 1  // increment per release
versionName = "1.0.0"  // major.minor.patch
```
