package com.igrupos.server.features

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.plugins.cors.routing.*
import org.slf4j.LoggerFactory

fun Application.configureCORS() {
    val logger = LoggerFactory.getLogger("CORS")
    val allowedOriginsEnv = System.getenv("ALLOWED_ORIGINS")
    val isProduction = System.getenv("ENVIRONMENT") == "production"

    install(CORS) {
        if (allowedOriginsEnv.isNullOrBlank()) {
            if (isProduction) {
                logger.error("ALLOWED_ORIGINS not set in production — CORS will reject all origins")
            } else {
                logger.warn("ALLOWED_ORIGINS not set — allowing anyHost (development mode)")
                anyHost()
            }
        } else {
            val origins = allowedOriginsEnv.split(",").map { it.trim() }
            logger.info("CORS allowed origins: $origins")
            origins.forEach { origin ->
                val sanitized = origin.removeSuffix("/")
                allowHost(sanitized)
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
