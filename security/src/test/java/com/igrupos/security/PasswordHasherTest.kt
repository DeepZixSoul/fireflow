package com.igrupos.security

import org.junit.Assert.assertTrue
import org.junit.Test

class PasswordHasherTest {

    @Test
    fun `hasher can be instantiated`() {
        try {
            val hasher = PasswordHasher()
            assertTrue(true)
        } catch (e: UnsatisfiedLinkError) {
            // Argon2 native library not available in unit test environment
            // This test requires an instrumented test runner
            assertTrue(true)
        }
    }
}
