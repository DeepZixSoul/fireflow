package com.fireflow.server.utils

import com.fireflow.server.config.JwtConfig
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class JwtUtilsTest {

    @BeforeAll
    fun setup() {
        JwtUtils.init(JwtConfig(secret = "test-secret-key-for-testing-only-1234567890"))
    }

    @Test
    fun `generateToken returns non-empty token`() {
        val token = JwtUtils.generateToken(1L, "admin", "admin")
        assertTrue(token.isNotBlank())
    }

    @Test
    fun `generated token can be verified`() {
        val token = JwtUtils.generateToken(1L, "admin", "admin")
        val decoded = JwtUtils.verifier.verify(token)

        assertEquals("1", decoded.subject)
        assertEquals("admin", decoded.getClaim("username").asString())
        assertEquals("admin", decoded.getClaim("role").asString())
    }

    @Test
    fun `token contains passwordChangedAt claim`() {
        val token = JwtUtils.generateToken(1L, "admin", "admin", passwordChangedAt = 1000L)
        val decoded = JwtUtils.verifier.verify(token)

        assertEquals(1000L, decoded.getClaim("passwordChangedAt").asLong())
    }

    @Test
    fun `token has expiration`() {
        val token = JwtUtils.generateToken(1L, "admin", "admin")
        val decoded = JwtUtils.verifier.verify(token)

        assertNotNull(decoded.expiresAt)
        assertTrue(decoded.expiresAt.time > System.currentTimeMillis())
    }

    @Test
    fun `getExpirationMs returns configured value`() {
        val expiration = JwtUtils.getExpirationMs()
        assertTrue(expiration > 0)
    }

    @Test
    fun `tampered token fails verification`() {
        val token = JwtUtils.generateToken(1L, "admin", "admin")
        val tampered = token.dropLast(5) + "XXXXX"

        assertThrows(Exception::class.java) {
            JwtUtils.verifier.verify(tampered)
        }
    }

    @Test
    fun `token contains jti claim`() {
        val token = JwtUtils.generateToken(1L, "admin", "admin")
        val decoded = JwtUtils.verifier.verify(token)

        assertNotNull(decoded.id)
        assertTrue(decoded.id.isNotBlank())
    }

    @Test
    fun `each token has unique jti`() {
        val token1 = JwtUtils.generateToken(1L, "admin", "admin")
        val token2 = JwtUtils.generateToken(1L, "admin", "admin")

        val jti1 = JwtUtils.verifier.verify(token1).id
        val jti2 = JwtUtils.verifier.verify(token2).id

        assertNotEquals(jti1, jti2)
    }

    @Test
    fun `token with default passwordChangedAt has value 0`() {
        val token = JwtUtils.generateToken(1L, "admin", "admin")
        val decoded = JwtUtils.verifier.verify(token)

        assertEquals(0L, decoded.getClaim("passwordChangedAt").asLong())
    }
}
