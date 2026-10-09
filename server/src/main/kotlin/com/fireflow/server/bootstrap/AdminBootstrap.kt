package com.fireflow.server.bootstrap

import com.fireflow.server.repositories.UserRepository
import com.fireflow.server.utils.PasswordUtils
import org.slf4j.LoggerFactory

/**
 * Creates the first admin account when the database has no users at all.
 *
 * No default credentials ship with the server: the password is read from the
 * [PASSWORD_ENV] environment variable. If it is missing or does not meet the password
 * policy, no account is created and login stays disabled (fail closed).
 */
object AdminBootstrap {
    private val logger = LoggerFactory.getLogger(AdminBootstrap::class.java)

    const val USERNAME_ENV = "ADMIN_INITIAL_USERNAME"
    const val PASSWORD_ENV = "ADMIN_INITIAL_PASSWORD"
    const val DEFAULT_USERNAME = "admin"

    /**
     * @param env environment lookup, injectable for tests
     * @return true when an initial admin was created
     */
    fun ensureAdminExists(
        userRepository: UserRepository,
        env: (String) -> String? = System::getenv
    ): Boolean {
        if (userRepository.count() > 0) {
            return false
        }

        val username = env(USERNAME_ENV)?.takeIf { it.isNotBlank() } ?: DEFAULT_USERNAME
        val password = env(PASSWORD_ENV)?.takeIf { it.isNotBlank() }

        if (password == null) {
            logger.error(
                "No users in database and $PASSWORD_ENV is not set. " +
                    "Set it to create the first admin account; login stays disabled until then."
            )
            return false
        }

        val validation = PasswordUtils.validatePassword(password)
        if (!validation.isValid) {
            logger.error(
                "$PASSWORD_ENV does not meet the password policy " +
                    "(${validation.errors.joinToString(", ")}). No admin account created."
            )
            return false
        }

        userRepository.upsert(
            username = username,
            passwordHash = PasswordUtils.hashPassword(password),
            role = "admin",
            isActive = true,
            mustChangePassword = true
        )
        logger.info(
            "Created initial admin account '$username' (value supplied via $PASSWORD_ENV). " +
                "It must be changed on first login."
        )
        return true
    }
}
