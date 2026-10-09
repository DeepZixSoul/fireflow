package com.fireflow.server

import com.fireflow.server.repositories.UserRepository
import com.fireflow.server.repositories.Users
import com.fireflow.server.utils.PasswordUtils

/**
 * Shared credentials for tests.
 *
 * Migrations no longer create a default admin (V7__remove_default_admin.sql), so test
 * classes that need a user must seed it explicitly through this helper.
 */
object TestSeed {
    const val ADMIN_USERNAME = "admin"
    const val ADMIN_PASSWORD = "FireFlow-Test-Admin-1!"

    /**
     * Inserts the admin used by auth/sync tests and returns its actual id
     * (migrations consume the first identity value, so it is not always 1).
     */
    fun seedAdmin(userRepository: UserRepository): Long {
        userRepository.upsert(
            username = ADMIN_USERNAME,
            passwordHash = PasswordUtils.hashPassword(ADMIN_PASSWORD),
            role = "admin",
            isActive = true,
            mustChangePassword = false
        )
        return userRepository.findByUsername(ADMIN_USERNAME)!![Users.id]
    }
}
