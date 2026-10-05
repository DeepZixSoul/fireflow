package com.fireflow.core.util

data class PasswordValidationResult(
    val isValid: Boolean,
    val errors: List<String>
)

object PasswordValidator {

    private const val MIN_LENGTH = 8

    fun validate(password: String): PasswordValidationResult {
        val errors = mutableListOf<String>()

        if (password.length < MIN_LENGTH) {
            errors.add("Mínimo $MIN_LENGTH caracteres")
        }
        if (!password.any { it.isUpperCase() }) {
            errors.add("Al menos una mayúscula")
        }
        if (!password.any { it.isLowerCase() }) {
            errors.add("Al menos una minúscula")
        }
        if (!password.any { it.isDigit() }) {
            errors.add("Al menos un número")
        }

        return PasswordValidationResult(
            isValid = errors.isEmpty(),
            errors = errors
        )
    }
}
