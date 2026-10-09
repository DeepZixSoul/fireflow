package com.fireflow.server.routes

import com.fireflow.server.features.RateLimiter
import com.fireflow.server.models.request.ChangePasswordRequest
import com.fireflow.server.models.request.LoginRequest
import com.fireflow.server.services.AuthService
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.authenticate
import io.ktor.server.auth.principal
import io.ktor.server.auth.jwt.JWTPrincipal
import io.ktor.server.plugins.BadRequestException
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.serialization.SerializationException
import org.slf4j.LoggerFactory

private val loginRateLimiter = RateLimiter(maxRequests = 5, windowMs = 60_000)
private val changePasswordRateLimiter = RateLimiter(maxRequests = 5, windowMs = 60_000)

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

        authenticate("auth-jwt") {
            post("/change-password") {
                val clientIp = call.request.local.remoteAddress

                if (!changePasswordRateLimiter.isAllowed(clientIp)) {
                    logger.warn("Rate limit exceeded on change-password for IP: $clientIp")
                    call.respond(
                        HttpStatusCode.TooManyRequests,
                        mapOf("error" to "Demasiadas peticiones. Intenta de nuevo en 1 minuto.")
                    )
                    return@post
                }

                val userId = call.principal<JWTPrincipal>()?.payload?.subject?.toLongOrNull()
                if (userId == null) {
                    logger.warn("change-password: missing user id in token")
                    call.respond(HttpStatusCode.Unauthorized, mapOf("error" to "Token inválido"))
                    return@post
                }

                val request = try {
                    call.receive<ChangePasswordRequest>()
                } catch (e: SerializationException) {
                    logger.warn("change-password: malformed body")
                    call.respond(
                        HttpStatusCode.BadRequest,
                        mapOf("error" to "Body inválido: se esperan oldPassword y newPassword")
                    )
                    return@post
                } catch (e: BadRequestException) {
                    logger.warn("change-password: unreadable body")
                    call.respond(
                        HttpStatusCode.BadRequest,
                        mapOf("error" to "Body inválido: se esperan oldPassword y newPassword")
                    )
                    return@post
                }

                if (request.oldPassword.isBlank() || request.newPassword.isBlank()) {
                    call.respond(
                        HttpStatusCode.BadRequest,
                        mapOf("error" to "oldPassword y newPassword son requeridos")
                    )
                    return@post
                }

                authService.changePassword(userId, request.oldPassword, request.newPassword)
                    .onSuccess {
                        // The password change invalidates every previously issued token.
                        logger.info("change-password successful (id=$userId)")
                        call.respond(HttpStatusCode.OK, mapOf("message" to "Contraseña actualizada"))
                    }
                    .onFailure { error ->
                        logger.warn("change-password failed (id=$userId)")
                        call.respond(
                            HttpStatusCode.BadRequest,
                            mapOf("error" to (error.message ?: "Error al cambiar la contraseña"))
                        )
                    }
            }
        }
    }
}
