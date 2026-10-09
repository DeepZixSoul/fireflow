package com.fireflow.security

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Assume
import org.junit.Test

class PasswordHasherTest {

    private fun hasherOrNull(): PasswordHasher? = try {
        PasswordHasher()
    } catch (e: UnsatisfiedLinkError) {
        Assume.assumeNoException("Argon2 native library only loads on a device", e)
        null
    }

    @Test
    fun `hash and verify roundtrip`() {
        val hasher = hasherOrNull() ?: return
        val hash = try {
            hasher.hash("Adm1n1234")
        } catch (e: UnsatisfiedLinkError) {
            Assume.assumeNoException("Argon2 native library only loads on a device", e)
            return
        }

        assertTrue(hasher.verify("Adm1n1234", hash))
        assertFalse(hasher.verify("wrong-password", hash))
    }

    @Test
    fun `verify rejects malformed hash`() {
        val hasher = hasherOrNull() ?: return
        assertFalse(hasher.verify("Adm1n1234", "not-a-valid-hash"))
    }
}
