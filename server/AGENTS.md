# Server Agent Guidelines

## Stack Tecnológico

- **Runtime**: Kotlin/JVM 21
- **Framework**: Ktor 3.0.3 (Netty)
- **ORM**: Exposed 0.48.0
- **DB**: PostgreSQL 16
- **Migrations**: Flyway 10.20.1
- **Auth**: JWT (Bearer tokens, java-jwt 4.4.0)
- **Password**: BCrypt (jBCrypt 0.10.2, cost=10)
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
│   ├── db/migration/           # Flyway SQL files (V1-V5)
│   └── logback.xml             # Structured logging
├── src/test/kotlin/            # 70 tests (routes, services, repos, utils)
├── Dockerfile                  # Multi-stage, non-root (appuser:1001)
├── docker-compose.yml          # Ktor + PostgreSQL 16
└── build.gradle.kts
```

---

## API Endpoints

### POST /api/v1/auth/login

```json
Request:  { "username": "admin", "password": "<seed-password>" }
Response: { "token": "...", "expiresIn": 900000, "role": "admin", "username": "admin", "mustChangePassword": true }
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
- [ ] JWT expiration <= 30 min
- [ ] Logging sin PII

### Antes de cada Deploy

- [ ] HTTPS configurado
- [ ] .env en .gitignore
- [ ] Docker: non-root user
- [ ] Docker: health check OK (wget)
- [ ] Docker: mem_limit + cpus set
- [ ] CORS: ALLOWED_ORIGINS configured
- [ ] Rate limiting: 5 req/min on /login
- [ ] Flyway migrations probadas

---

## Variables de Entorno

```bash
# .env (NUNCA commitear)
JWT_SECRET=>=32 chars
DATABASE_URL=jdbc:postgresql://db:5432/fireflow
DB_USER=fireflow
DB_PASSWORD=>=16 chars
ALLOWED_ORIGINS=http://localhost:3000  # CORS whitelist
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

- (none — all resolved)
