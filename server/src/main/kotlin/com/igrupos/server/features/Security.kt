package com.igrupos.server.features

import com.igrupos.server.config.JwtConfig
import com.igrupos.server.repositories.UserRepository
import com.igrupos.server.utils.JwtUtils
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.response.*
import org.slf4j.LoggerFactory

private val securityLogger = LoggerFactory.getLogger("Security")

fun Application.configureSecurity(jwtConfig: JwtConfig, userRepository: UserRepository) {
    JwtUtils.init(jwtConfig)

    install(Authentication) {
        jwt("auth-jwt") {
            realm = jwtConfig.realm
            verifier(JwtUtils.verifier)
            validate { credential ->
                val userId = credential.payload.subject.toLongOrNull()
                if (userId == null) {
                    securityLogger.warn("JWT validation failed: invalid subject")
                    return@validate null
                }

                val user = userRepository.findById(userId)
                if (user == null) {
                    securityLogger.warn("JWT validation failed: user not found (id=$userId)")
                    return@validate null
                }

                if (!user[com.igrupos.server.repositories.Users.isActive]) {
                    securityLogger.warn("JWT validation failed: user is inactive (id=$userId)")
                    return@validate null
                }

                val tokenPasswordChangedAt = credential.payload.getClaim("passwordChangedAt").asLong()
                val dbPasswordChangedAt = user[com.igrupos.server.repositories.Users.passwordChangedAt]

                if (tokenPasswordChangedAt != dbPasswordChangedAt) {
                    securityLogger.warn("JWT validation failed: password was changed since token was issued (id=$userId)")
                    return@validate null
                }

                JWTPrincipal(credential.payload)
            }
            challenge { _, _ ->
                call.respond(
                    HttpStatusCode.Unauthorized,
                    mapOf("error" to "Token inválido o expirado")
                )
            }
        }
    }
}
