package com.igrupos.core.util

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class PasswordValidatorTest {

    @Test
    fun `valid password passes all checks`() {
        val result = PasswordValidator.validate("Admin123")
        assertTrue(result.isValid)
        assertTrue(result.errors.isEmpty())
    }

    @Test
    fun `password too short fails`() {
        val result = PasswordValidator.validate("Ad1")
        assertFalse(result.isValid)
        assertTrue(result.errors.any { it.contains("8 caracteres") })
    }

    @Test
    fun `password without uppercase fails`() {
        val result = PasswordValidator.validate("admin123")
        assertFalse(result.isValid)
        assertTrue(result.errors.any { it.contains("mayúscula") })
    }

    @Test
    fun `password without lowercase fails`() {
        val result = PasswordValidator.validate("ADMIN123")
        assertFalse(result.isValid)
        assertTrue(result.errors.any { it.contains("minúscula") })
    }

    @Test
    fun `password without digit fails`() {
        val result = PasswordValidator.validate("AdminABC")
        assertFalse(result.isValid)
        assertTrue(result.errors.any { it.contains("número") })
    }

    @Test
    fun `password with multiple issues reports all errors`() {
        val result = PasswordValidator.validate("abc")
        assertFalse(result.isValid)
        assertEquals(3, result.errors.size)
    }

    @Test
    fun `exactly 8 char password with all rules passes`() {
        val result = PasswordValidator.validate("Abcdef1g")
        assertTrue(result.isValid)
    }

    @Test
    fun `long complex password passes`() {
        val result = PasswordValidator.validate("MyStr0ng!Passw0rd")
        assertTrue(result.isValid)
    }
}
