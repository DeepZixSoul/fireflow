package com.fireflow.server.routes

import com.fireflow.server.features.RateLimiter
import com.fireflow.server.services.AuthService
import com.fireflow.server.models.request.LoginRequest
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import org.slf4j.LoggerFactory

private val loginRateLimiter = RateLimiter(maxRequests = 5, windowMs = 60_000)

fun Route.authRoutes(authService: AuthService) {
    val logger = LoggerFactory.getLogger("AuthRoutes")

    route("/api/v1/auth") {
        post("/login") {
            try {
                val clientIp = call.request.local.remoteAddress

                if (!loginRateLimiter.isAllowed(clientIp)) {
                    logger.warn("Rate limit exceeded for IP: $clientIp")
                    call.respond(
                        HttpStatusCode.TooManyRequests,
                        mapOf("error" to "Demasiadas peticiones. Intenta de nuevo en 1 minuto.")
                    )
                    return@post
                }

                val request = call.receive<LoginRequest>()

                if (request.username.isBlank() || request.password.isBlank()) {
                    call.respond(
                        HttpStatusCode.BadRequest,
                        mapOf("error" to "Username y password son requeridos")
                    )
                    return@post
                }

                val result = authService.login(request)

                result.onSuccess { response ->
                    logger.info("Login successful")
                    call.respond(HttpStatusCode.OK, response)
                }

                result.onFailure {
                    logger.warn("Login failed")
                    call.respond(
                        HttpStatusCode.Unauthorized,
                        mapOf("error" to "Credenciales inválidas")
                    )
                }
            } catch (e: Exception) {
                logger.error("Login error", e)
                call.respond(
                    HttpStatusCode.InternalServerError,
                    mapOf("error" to "Error interno del servidor")
                )
            }
        }
    }
}
