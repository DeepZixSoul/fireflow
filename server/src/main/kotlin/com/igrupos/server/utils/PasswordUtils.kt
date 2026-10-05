package com.igrupos.server.utils

import at.favre.lib.crypto.bcrypt.BCrypt

object PasswordUtils {

    private const val COST_FACTOR = 12

    private const val MIN_PASSWORD_LENGTH = 8

    data class PasswordValidationResult(
        val isValid: Boolean,
        val errors: List<String>
    )

    fun validatePassword(password: String): PasswordValidationResult {
        val errors = mutableListOf<String>()

        if (password.length < MIN_PASSWORD_LENGTH) {
            errors.add("Mínimo $MIN_PASSWORD_LENGTH caracteres")
        }
        if (!password.any { it.isDigit() }) {
            errors.add("Al menos un número")
        }

        return PasswordValidationResult(
            isValid = errors.isEmpty(),
            errors = errors
        )
    }

    fun hashPassword(password: String): String {
        return BCrypt.withDefaults()
            .hashToString(COST_FACTOR, password.toCharArray())
    }

    fun verifyPassword(password: String, hash: String): Boolean {
        return BCrypt.verifyer()
            .verify(password.toCharArray(), hash)
            .verified
    }
}
