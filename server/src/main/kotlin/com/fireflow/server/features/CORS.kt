package com.fireflow.server.features

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.plugins.cors.routing.*
import org.slf4j.LoggerFactory

/**
 * Configures CORS with an explicit allow-list of full origins
 * (e.g. `https://app.example.com,http://localhost:3000`).
 *
 * Fails closed in production when no origin is configured: only exact
 * origins from [allowedOriginsEnv] are accepted. The `anyHost()` fallback
 * exists for local development only.
 */
fun Application.configureCORS(
    allowedOriginsEnv: String? = System.getenv("ALLOWED_ORIGINS"),
    isProduction: Boolean = System.getenv("ENVIRONMENT") == "production"
) {
    val logger = LoggerFactory.getLogger("CORS")
    val allowedOrigins = allowedOriginsEnv
        ?.split(",")
        ?.map { it.trim().removeSuffix("/") }
        ?.filter { it.isNotEmpty() }
        .orEmpty()

    install(CORS) {
        when {
            allowedOrigins.isNotEmpty() -> {
                logger.info("CORS allowed origins: $allowedOrigins")
                allowOrigins { origin -> origin in allowedOrigins }
            }

            isProduction -> {
                logger.error("ALLOWED_ORIGINS not set in production — CORS rejects all origins")
            }

            else -> {
                logger.warn("ALLOWED_ORIGINS not set — allowing anyHost (development mode)")
                anyHost()
            }
        }

        allowHeader(HttpHeaders.ContentType)
        allowHeader(HttpHeaders.Authorization)
        allowHeader(HttpHeaders.Accept)
        allowMethod(HttpMethod.Post)
        allowMethod(HttpMethod.Get)
        allowMethod(HttpMethod.Put)
        allowMethod(HttpMethod.Delete)
        allowMethod(HttpMethod.Options)
        allowCredentials = true
        maxAgeInSeconds = 3600
    }
}
