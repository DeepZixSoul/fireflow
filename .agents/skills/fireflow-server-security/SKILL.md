---
name: "fireflow-server-security"
description: "Server-side security patterns: Ktor hardening, JWT auth, Exposed safe queries, Docker security, OWASP Top 10 compliance."
metadata:
  version: "1.0.0"
  category: "security"
  tags: ["fireflow", "server", "ktor", "security", "jwt", "docker", "owasp"]
  triggers:
    include: ["fireflow server", "ktor security", "server hardening", "jwt fireflow", "docker security fireflow", "owasp fireflow"]
  owners: ["@fireflow/maintainers"]
---

# FireFlow Server Security

## OWASP Top 10 Compliance

### A01: Broken Access Control
- RBAC por rol (admin, technician, viewer)
- JWT con claims de rol
- Validación de permisos en cada endpoint
- Nunca confiar en el client-side para auth

### A02: Cryptographic Failures
- Passwords: Argon2id (tCost=3, mCost=64MB, parallelism=4)
- Tokens: HS256 con secret > 256 bits
- TLS 1.2+ obligatorio (Let's Encrypt)
- Nunca loggear secrets o passwords

### A03: Injection
- Exposed: queries parametrizadas SIEMPRE
- Nunca concatenar strings en queries
- Validar y sanitizar inputs antes de procesar
- Usar `Op.build` para queries dinámicas

### A04: Insecure Design
- Threat modeling antes de implementar
- Separación de responsabilidades
- Principio de mínimo privilegio
- Validación en capa de dominio

### A05: Security Misconfiguration
- Variables de entorno para secrets
- Nunca hardcodear credenciales
- Docker: non-root user, read-only fs
- CORS restrictivo (solo dominios permitidos)

### A06: Vulnerable Components
- Escanear dependencias con `gradle dependencyCheckAnalyze`
- Actualizar CVEs críticos inmediatamente
- Fijar versiones en `libs.versions.toml`
- Revisar changelogs antes de actualizar

### A07: Authentication Failures
- Rate limiting: 5 req/min por IP en login
- Lockout después de 5 intentos fallidos
- Tokens con expiración corta (15 min)
- Refresh token pattern

### A08: Data Integrity
- Signed JWT (HS256 o RS256)
- Migraciones versionadas (Flyway)
- Checksums en respuestas
- Audit log para operaciones críticas

### A09: Logging Failures
- Structured logging (JSON)
- Loggear: auth attempts, sync operations, errors
- Nunca loggear: passwords, tokens, PII
- Retención de logs: 30 días mínimo

### A10: SSRF
- Validar URLs de entrada
- Whitelist de dominios permitidos
- No seguir redirects en URLs de input
- Timeouts en llamadas externas

---

## Ktor Security Plugins

### Authentication (JWT)
```kotlin
install(Authentication) {
    jwt("auth-jwt") {
        realm = "fireflow"
        verifier(
            JWT.require(Algorithm.HMAC256(secret))
                .withIssuer("fireflow")
                .build()
        )
        validate { credential ->
            if (credential.payload.getClaim("role").asString() in listOf("admin", "technician")) {
                JWTPrincipal(credential.payload)
            } else null
        }
        challenge { _, _ ->
            call.respond(HttpStatusCode.Unauthorized, mapOf("error" to "Token inválido"))
        }
    }
}
```

### Rate Limiting
```kotlin
install(RateLimiting) {
    rateLimiter(limit = 5, rateWindow = Duration.ofMinutes(1))
}
```

### CORS
```kotlin
install(CORS) {
    allowHost("your-domain.com")
    allowHeader(HttpHeaders.ContentType)
    allowHeader(HttpHeaders.Authorization)
    allowMethod(HttpMethod.Post)
}
```

### StatusPages (Error Handling)
```kotlin
install(StatusPages) {
    exception<AuthenticationException> { call, _ ->
        call.respond(HttpStatusCode.Unauthorized, mapOf("error" to "No autorizado"))
    }
    exception<Throwable> { call, cause ->
        call.application.log.error("Unhandled exception", cause)
        call.respond(HttpStatusCode.InternalServerError, 
            mapOf("error" to "Error interno del servidor"))
    }
}
```

---

## Exposed Security Patterns

### SIEMPRE Queries Parametrizadas
```kotlin
// CORRECTO
ClientTable.select { ClientTable.id eq clientId }

// NUNCA
ClientTable.select { ClientTable.id.eq(clientId) }  // Aún parametrizado, pero preferir infix

// JAMÁS
transaction { 
    exec("SELECT * FROM clients WHERE id = $clientId")  // SQL INJECTION
}
```

### Transacciones Seguras
```kotlin
transaction {
    val client = ClientTable.insertAndGetId { ... }
    PressureGroupTable.insert { it[clientId] = client.value }
    // Si falla, se revierte todo
}
```

### Validación de Inputs
```kotlin
fun validateClient(client: Client): List<String> {
    val errors = mutableListOf<String>()
    if (client.name.isBlank()) errors.add("Nombre es requerido")
    if (!client.cif.matches(Regex("^[A-Z0-9]{9}$"))) errors.add("CIF inválido")
    if (client.email.isNotBlank() && !client.email.contains("@")) errors.add("Email inválido")
    return errors
}
```

---

## Docker Security

### Dockerfile Seguro
```dockerfile
# Multi-stage build
FROM eclipse-temurin:21-jdk AS builder
WORKDIR /app
COPY . .
RUN gradle shadowJar

# Runtime stage minimal
FROM eclipse-temurin:21-jre-alpine
RUN addgroup -S appgroup && adduser -S appuser -G appgroup
WORKDIR /app
COPY --from=builder /app/build/libs/*.jar app.jar
USER appuser
EXPOSE 8080
HEALTHCHECK --interval=30s --timeout=3s CMD wget -q --spider http://localhost:8080/health || exit 1
ENTRYPOINT ["java", "-jar", "app.jar"]
```

### docker-compose.yml Seguro
```yaml
services:
  server:
    build: .
    environment:
      - DATABASE_URL=jdbc:postgresql://db:5432/fireflow
      - JWT_SECRET=${JWT_SECRET}  # Nunca hardcodear
    depends_on:
      db:
        condition: service_healthy
    read_only: true
    tmpfs:
      - /tmp
    security_opt:
      - no-new-privileges:true

  db:
    image: postgres:16-alpine
    environment:
      - POSTGRES_DB=fireflow
      - POSTGRES_USER=${DB_USER}
      - POSTGRES_PASSWORD=${DB_PASSWORD}
    volumes:
      - pgdata:/var/lib/postgresql/data
    healthcheck:
      test: ["CMD-SHELL", "pg_isready -U ${DB_USER}"]
      interval: 5s
      timeout: 5s
      retries: 5

volumes:
  pgdata:
```

---

## JWT Implementation

### Token Generation
```kotlin
fun generateToken(userId: Long, role: String): String {
    return JWT.create()
        .withIssuer("fireflow")
        .withSubject(userId.toString())
        .withClaim("role", role)
        .withExpiresAt(Date(System.currentTimeMillis() + 15 * 60 * 1000)) // 15 min
        .sign(Algorithm.HMAC256(jwtSecret))
}
```

### Token Validation
```kotlin
fun validateToken(token: String): DecodedJWT? {
    return try {
        JWT.require(Algorithm.HMAC256(jwtSecret))
            .withIssuer("fireflow")
            .build()
            .verify(token)
    } catch (e: JWTVerificationException) {
        null
    }
}
```

---

## Environment Configuration

### application.conf
```hocon
ktor {
    deployment {
        port = 8080
    }
    security {
        jwtSecret = ${JWT_SECRET}
        jwtIssuer = "fireflow"
        jwtExpirationMs = 900000  # 15 minutes
    }
    database {
        url = ${DATABASE_URL}
        user = ${DB_USER}
        password = ${DB_PASSWORD}
    }
}
```

### .env.example (NUNCA commitear .env)
```bash
JWT_SECRET=your-256-bit-secret-here
DATABASE_URL=jdbc:postgresql://localhost:5432/fireflow
DB_USER=fireflow
DB_PASSWORD=change-me-in-production
```

---

## Pre-Deployment Checklist

- [ ] HTTPS configurado (Let's Encrypt o certificado válido)
- [ ] JWT_SECRET >= 256 bits
- [ ] DB_PASSWORD fuerte (16+ chars, mixed case, numbers, symbols)
- [ ] CORS configurado solo para dominios permitidos
- [ ] Rate limiting habilitado en auth endpoints
- [ ] Logging sin PII (passwords, tokens, emails)
- [ ] Docker: non-root user
- [ ] Docker: read-only filesystem
- [ ] Docker: health check configurado
- [ ] Flyway migrations tested
- [ ] Dependency scan sin CVEs críticos
- [ ] .env en .gitignore
- [ ] No secrets en código fuente
