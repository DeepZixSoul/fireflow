package com.fireflow.server.config

data class ServerConfig(
    val port: Int = 9090,
    val portSSL: Int = 8443,
    val database: DatabaseConnectionConfig = DatabaseConnectionConfig(),
    val jwt: JwtConfig = JwtConfig(),
    val ssl: SslConfig? = null
) {
    companion object {
        fun fromEnvironment(): ServerConfig {
            return ServerConfig(
                port = System.getenv("SERVER_PORT")?.toIntOrNull() ?: 9090,
                portSSL = System.getenv("SERVER_PORT_SSL")?.toIntOrNull() ?: 8443,
                database = DatabaseConnectionConfig.fromEnvironment(),
                jwt = JwtConfig.fromEnvironment(),
                ssl = SslConfig.fromEnvironment()
            )
        }
    }
}

data class DatabaseConnectionConfig(
    val url: String = "jdbc:postgresql://localhost:5432/fireflow_db",
    val driver: String = "org.postgresql.Driver",
    val user: String = "fireflow",
    val password: String = "",
    val maximumPoolSize: Int = 10,
    val minimumIdle: Int = 2,
    val idleTimeout: Long = 30000,
    val connectionTimeout: Long = 5000,
    val maxLifetime: Long = 1800000
) {
    companion object {
        fun fromEnvironment(): DatabaseConnectionConfig {
            val baseUrl = System.getenv("DATABASE_URL") ?: "jdbc:postgresql://localhost:5432/fireflow_db"
            val sslMode = System.getenv("DB_SSL_MODE")

            val url = if (!sslMode.isNullOrBlank() && !baseUrl.contains("sslmode=")) {
                "$baseUrl?sslmode=$sslMode"
            } else {
                baseUrl
            }

            return DatabaseConnectionConfig(
                url = url,
                user = System.getenv("DB_USER") ?: "fireflow",
                password = System.getenv("DB_PASSWORD") ?: ""
            )
        }
    }
}

data class JwtConfig(
    val secret: String = "",
    val issuer: String = "fireflow",
    val audience: String = "fireflow-api",
    val realm: String = "fireflow",
    val expirationMs: Long = 900000
) {
    companion object {
        fun fromEnvironment(): JwtConfig {
            val secret = System.getenv("JWT_SECRET") ?: ""
            require(secret.isNotBlank()) {
                "JWT_SECRET environment variable is required. Must be at least 32 characters."
            }
            require(secret.length >= 32) {
                "JWT_SECRET must be at least 32 characters long for HMAC256 security."
            }
            return JwtConfig(
                secret = secret,
                issuer = System.getenv("JWT_ISSUER") ?: "fireflow",
                audience = System.getenv("JWT_AUDIENCE") ?: "fireflow-api",
                realm = System.getenv("JWT_REALM") ?: "fireflow",
                expirationMs = System.getenv("JWT_EXPIRATION_MS")?.toLongOrNull() ?: 900000
            )
        }
    }
}
