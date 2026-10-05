package com.fireflow.server.utils

import com.auth0.jwt.JWT
import com.auth0.jwt.JWTVerifier
import com.auth0.jwt.algorithms.Algorithm
import com.fireflow.server.config.JwtConfig
import org.slf4j.LoggerFactory
import java.util.Date
import java.util.UUID

object JwtUtils {
    private val logger = LoggerFactory.getLogger(JwtUtils::class.java)

    private lateinit var config: JwtConfig
    private lateinit var algorithm: Algorithm
    private lateinit var _verifier: JWTVerifier

    val verifier: JWTVerifier
        get() = _verifier

    fun init(jwtConfig: JwtConfig) {
        config = jwtConfig

        require(config.secret.length >= 32) {
            "JWT_SECRET must be at least 32 characters for HMAC256 security."
        }

        algorithm = Algorithm.HMAC256(config.secret)
        _verifier = JWT.require(algorithm)
            .withIssuer(config.issuer)
            .withAudience(config.audience)
            .build()

        logger.info("JWT configured with issuer=${config.issuer}, audience=${config.audience}")
    }

    fun generateToken(
        userId: Long,
        username: String,
        role: String,
        passwordChangedAt: Long = 0
    ): String {
        val now = System.currentTimeMillis()
        val expiresAt = Date(now + config.expirationMs)

        return JWT.create()
            .withIssuer(config.issuer)
            .withAudience(config.audience)
            .withSubject(userId.toString())
            .withJWTId(UUID.randomUUID().toString())
            .withClaim("username", username)
            .withClaim("role", role)
            .withClaim("passwordChangedAt", passwordChangedAt)
            .withIssuedAt(Date(now))
            .withExpiresAt(expiresAt)
            .sign(algorithm)
    }

    fun getExpirationMs(): Long {
        return config.expirationMs
    }
}
