package com.igrupos.security

import android.util.Log
import com.lambdapioneer.argon2kt.Argon2Kt
import com.lambdapioneer.argon2kt.Argon2Mode
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PasswordHasher @Inject constructor() {

    private val argon2 = Argon2Kt()

    fun hash(password: String): String {
        val hashResult = argon2.hash(
            mode = Argon2Mode.ARGON2_ID,
            password = password.toByteArray(),
            salt = generateSalt(),
            tCostInIterations = 3,
            mCostInKibibyte = 65536,
            parallelism = 4,
            hashLengthInBytes = 32
        )
        return hashResult.encodedOutputAsString()
    }

    fun verify(password: String, encodedHash: String): Boolean {
        return try {
            argon2.verify(
                mode = Argon2Mode.ARGON2_ID,
                encoded = encodedHash,
                password = password.toByteArray()
            )
        } catch (e: Exception) {
            Log.w(TAG, "Password verification failed: ${e.message}")
            false
        }
    }

    private fun generateSalt(): ByteArray {
        val salt = ByteArray(16)
        java.security.SecureRandom().nextBytes(salt)
        return salt
    }

    companion object {
        private const val TAG = "PasswordHasher"
    }
}
