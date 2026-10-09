# Server Agent Guidelines

## Stack Tecnológico

- **Runtime**: Kotlin/JVM 21
- **Framework**: Ktor 3.0.3 (Netty)
- **ORM**: Exposed 0.48.0
- **DB**: PostgreSQL 16
- **Migrations**: Flyway 10.20.1
- **Auth**: JWT (Bearer tokens, java-jwt 4.4.0)
- **Password**: BCrypt (`at.favre.lib:bcrypt` 0.10.2, cost=12)
- **Container**: Docker + Docker Compose
- **Build**: Gradle (Kotlin DSL)
- **Port**: 9090

---

## Arquitectura

```
server/
├── src/main/kotlin/com/fireflow/server/
│   ├── Application.kt          # Entry point + module wiring
│   ├── config/                 # ServerConfig, DatabaseConfig
│   ├── features/               # Ktor plugins (CORS, Auth, Serialization, StatusPages)
│   ├── routes/                 # API endpoints (Auth, Sync, Health)
│   ├── models/                 # Domain models + Request/Response DTOs
│   ├── repositories/           # Exposed DAOs + Table definitions
│   ├── services/               # Business logic (AuthService, SyncService)
│   └── utils/                  # JwtUtils, PasswordUtils
├── src/main/resources/
│   ├── db/migration/           # Flyway SQL files (V1-V7)
│   └── logback.xml             # Structured logging
├── src/test/kotlin/            # 108 tests (routes, services, repos, utils)
├── Dockerfile                  # Multi-stage, non-root (appuser:1001)
├── docker-compose.yml          # Ktor + PostgreSQL 16
└── build.gradle.kts
```

---

## API Endpoints

### POST /api/v1/auth/login

```json
Request:  { "username": "admin", "password": "<ADMIN_INITIAL_PASSWORD>" }
Response: { "token": "...", "expiresIn": 900000, "role": "admin", "username": "admin", "mustChangePassword": true }
```

### POST /api/v1/auth/change-password

```json
Headers:  Authorization: Bearer <token>
Request:  { "oldPassword": "...", "newPassword": "..." }
Response: { "message": "..." }   # invalida todos los tokens emitidos
```

### POST /api/v1/{collection}/sync

Headers: `Authorization: Bearer <token>`, `Content-Type: application/json`

```json
Request:  { "data": [...records...], "lastSyncTimestamp": 1712345678000 }
Response: { "data": [...records...], "syncTimestamp": 1712345678000 }
```

Colecciones: `clients`, `groups`, `revisions`, `curves`

### GET /health

```json
Response: { "status": "ok", "timestamp": "1712345678000", "version": "1.0.0" }
```

---

## Security Checklist

### Antes de cada PR

- [ ] No hay secrets hardcodeados
- [ ] Queries usan Exposed parametrizado
- [ ] JWT expiration <= 15 min
- [ ] Logging sin PII

### Antes de cada Deploy

- [ ] HTTPS configurado
- [ ] .env en .gitignore
- [ ] Docker: non-root user
- [ ] Docker: health check OK (wget)
- [ ] Docker: mem_limit + cpus set
- [ ] CORS: ALLOWED_ORIGINS configured
- [ ] Rate limiting: 5 req/min on /login y /change-password
- [ ] Flyway migrations probadas

---

## Variables de Entorno

```bash
# .env (NUNCA commitear)
JWT_SECRET=>=32 chars
DATABASE_URL=jdbc:postgresql://db:5432/fireflow
DB_USER=fireflow
DB_PASSWORD=>=16 chars
ENVIRONMENT=production           # CORS fail-closed
ALLOWED_ORIGINS=http://localhost:3000  # CORS whitelist
ADMIN_INITIAL_USERNAME=admin     # primer admin (sin semillas en Flyway)
ADMIN_INITIAL_PASSWORD=...       # política: 8+ mayúscula minúscula número
SSL_KEYSTORE=...                 # habilita TLS (sslConnector)
NVD_API_KEY=...                  # opcional: dependencyCheckAnalyze
```

---

## Commands

```bash
# Build
JAVA_HOME=/usr/lib/jvm/java-21-openjdk-amd64 ./gradlew build -x test

# Run locally
JAVA_HOME=/usr/lib/jvm/java-21-openjdk-amd64 ./gradlew run

# Run tests
JAVA_HOME=/usr/lib/jvm/java-21-openjdk-amd64 ./gradlew test

# Docker build
docker compose build

# Docker run
docker compose up -d
```

---

## Known Issues

- `NVD_API_KEY` opcional: sin ella el `dependencyCheckAnalyze` tarda ~20 min (rate limit NVD).
- El hash bcrypt cost 10 de `admin123` sigue en `V1__create_users.sql` porque `V7` lo borra
  (condición de la migración). No usar esa BD de tests fuera de tests.
