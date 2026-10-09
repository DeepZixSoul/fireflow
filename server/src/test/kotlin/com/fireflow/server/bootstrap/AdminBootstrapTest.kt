package com.fireflow.server.bootstrap

import com.fireflow.server.TestSeed
import com.fireflow.server.config.DatabaseConnectionConfig
import com.fireflow.server.config.DatabaseManager
import com.fireflow.server.repositories.UserRepository
import com.fireflow.server.repositories.Users
import com.fireflow.server.utils.PasswordUtils
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class AdminBootstrapTest {

    private lateinit var userRepository: UserRepository

    @BeforeEach
    fun setup() {
        DatabaseManager.init(DatabaseConnectionConfig(
            url = "jdbc:h2:mem:admin_bootstrap_${System.nanoTime()};DB_CLOSE_DELAY=-1",
            driver = "org.h2.Driver",
            user = "sa",
            password = ""
        ))
        userRepository = UserRepository(DatabaseManager.getDatabase())
    }

    @AfterEach
    fun teardown() {
        DatabaseManager.close()
    }

    private fun envOf(vararg pairs: Pair<String, String>): (String) -> String? = { key ->
        pairs.toMap()[key]
    }

    @Test
    fun `creates initial admin when env provides a valid password`() {
        val created = AdminBootstrap.ensureAdminExists(userRepository, envOf(
            AdminBootstrap.PASSWORD_ENV to "Initial-Admin-1!"
        ))

        assertTrue(created)
        assertEquals(1L, userRepository.count())

        val admin = userRepository.findByUsername(AdminBootstrap.DEFAULT_USERNAME)!!
        assertEquals("admin", admin[Users.role])
        assertTrue(admin[Users.isActive])
        assertTrue(admin[Users.mustChangePassword])
        assertTrue(PasswordUtils.verifyPassword("Initial-Admin-1!", admin[Users.passwordHash]))
    }

    @Test
    fun `does not create any user when password env is missing`() {
        val created = AdminBootstrap.ensureAdminExists(userRepository, envOf())

        assertFalse(created)
        assertEquals(0L, userRepository.count())
    }

    @Test
    fun `rejects password that fails the policy`() {
        val created = AdminBootstrap.ensureAdminExists(userRepository, envOf(
            AdminBootstrap.PASSWORD_ENV to "weak"
        ))

        assertFalse(created)
        assertEquals(0L, userRepository.count())
    }

    @Test
    fun `keeps existing users untouched`() {
        TestSeed.seedAdmin(userRepository)

        val created = AdminBootstrap.ensureAdminExists(userRepository, envOf(
            AdminBootstrap.PASSWORD_ENV to "Another-Admin-9!"
        ))

        assertFalse(created)
        assertEquals(1L, userRepository.count())
        assertTrue(PasswordUtils.verifyPassword(TestSeed.ADMIN_PASSWORD,
            userRepository.findByUsername(TestSeed.ADMIN_USERNAME)!![Users.passwordHash]))
    }

    @Test
    fun `honours custom admin username from env`() {
        val created = AdminBootstrap.ensureAdminExists(userRepository, envOf(
            AdminBootstrap.USERNAME_ENV to "operator",
            AdminBootstrap.PASSWORD_ENV to "Initial-Admin-1!"
        ))

        assertTrue(created)
        assertNotNull(userRepository.findByUsername("operator"))
        assertNull(userRepository.findByUsername(AdminBootstrap.DEFAULT_USERNAME))
    }
}
