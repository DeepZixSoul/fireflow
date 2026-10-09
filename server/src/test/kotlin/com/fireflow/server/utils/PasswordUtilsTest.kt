package com.fireflow.server.utils

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

class PasswordUtilsTest {

    @Test
    fun `hashPassword generates valid bcrypt hash`() {
        val hash = PasswordUtils.hashPassword("admin123")

        assertNotNull(hash)
        assertTrue(hash.startsWith("\$2a\$") || hash.startsWith("\$2b\$"))
        assertTrue(hash.length > 50)
    }

    @Test
    fun `verifyPassword returns true for correct password`() {
        val hash = PasswordUtils.hashPassword("admin123")

        assertTrue(PasswordUtils.verifyPassword("admin123", hash))
    }

    @Test
    fun `verifyPassword returns false for wrong password`() {
        val hash = PasswordUtils.hashPassword("admin123")

        assertFalse(PasswordUtils.verifyPassword("wrongpassword", hash))
    }

    @Test
    fun `hashPassword produces different hashes for same input`() {
        val hash1 = PasswordUtils.hashPassword("admin123")
        val hash2 = PasswordUtils.hashPassword("admin123")

        assertNotEquals(hash1, hash2)
        assertTrue(PasswordUtils.verifyPassword("admin123", hash1))
        assertTrue(PasswordUtils.verifyPassword("admin123", hash2))
    }

    @Test
    fun `validatePassword accepts valid password`() {
        val result = PasswordUtils.validatePassword("MyStr0ngPass")
        assertTrue(result.isValid)
        assertTrue(result.errors.isEmpty())
    }

    @Test
    fun `validatePassword rejects too short password`() {
        val result = PasswordUtils.validatePassword("Ab1")
        assertFalse(result.isValid)
        assertTrue(result.errors.any { it.contains("8 caracteres") })
    }

    @Test
    fun `validatePassword rejects password without digit`() {
        val result = PasswordUtils.validatePassword("NoDigitHere")
        assertFalse(result.isValid)
        assertTrue(result.errors.any { it.contains("número") })
    }

    @Test
    fun `validatePassword rejects password without uppercase`() {
        val result = PasswordUtils.validatePassword("admin123")
        assertFalse(result.isValid)
        assertTrue(result.errors.any { it.contains("mayúscula") })
    }

    @Test
    fun `validatePassword rejects password without lowercase`() {
        val result = PasswordUtils.validatePassword("ADMIN123")
        assertFalse(result.isValid)
        assertTrue(result.errors.any { it.contains("minúscula") })
    }

    @Test
    fun `validatePassword reports all errors at once`() {
        val result = PasswordUtils.validatePassword("ab")
        assertFalse(result.isValid)
        assertTrue(result.errors.size >= 2)
    }
}
