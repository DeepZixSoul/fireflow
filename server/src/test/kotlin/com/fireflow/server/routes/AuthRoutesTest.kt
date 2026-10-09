package com.fireflow.server.routes

import com.fireflow.server.TestSeed
import com.fireflow.server.config.DatabaseConnectionConfig
import com.fireflow.server.config.DatabaseManager
import com.fireflow.server.config.JwtConfig
import com.fireflow.server.features.configureSecurity
import com.fireflow.server.features.configureSerialization
import com.fireflow.server.repositories.UserRepository
import com.fireflow.server.repositories.Users
import com.fireflow.server.services.AuthService
import com.fireflow.server.utils.JwtUtils
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.application.*
import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.server.routing.*
import io.ktor.server.testing.*
import kotlinx.serialization.json.Json
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class AuthRoutesTest {

    private lateinit var authService: AuthService
    private lateinit var userRepository: UserRepository
    private var userId: Long = 0
    private val jwtSecret = "test-secret-key-for-testing-only-1234567890"

    @BeforeAll
    fun setup() {
        DatabaseManager.init(DatabaseConnectionConfig(
            url = "jdbc:h2:mem:test_auth;DB_CLOSE_DELAY=-1",
            driver = "org.h2.Driver",
            user = "sa",
            password = ""
        ))
        JwtUtils.init(JwtConfig(secret = jwtSecret))
        userRepository = UserRepository(DatabaseManager.getDatabase())
        authService = AuthService(userRepository)
        userId = TestSeed.seedAdmin(userRepository)
    }

    @AfterAll
    fun teardown() {
        DatabaseManager.close()
    }

    private fun ApplicationTestBuilder.configureApp() {
        application {
            install(ContentNegotiation) {
                json(Json { ignoreUnknownKeys = true })
            }
            configureSecurity(JwtConfig(secret = jwtSecret), userRepository)
            routing {
                authRoutes(authService)
            }
        }
    }

    /** Token bound to the current passwordChangedAt claim. */
    private fun freshToken(): String {
        val user = userRepository.findByUsername(TestSeed.ADMIN_USERNAME)!!
        return JwtUtils.generateToken(
            userId = user[Users.id],
            username = user[Users.username],
            role = user[Users.role],
            passwordChangedAt = user[Users.passwordChangedAt]
        )
    }

    @Test
    fun `login with valid credentials returns token`() = testApplication {
        configureApp()

        val response = client.post("/api/v1/auth/login") {
            contentType(ContentType.Application.Json)
            setBody("""{"username":"${TestSeed.ADMIN_USERNAME}","password":"${TestSeed.ADMIN_PASSWORD}"}""")
        }

        assertEquals(HttpStatusCode.OK, response.status)
        val body = response.bodyAsText()
        assertTrue(body.contains("token"))
        assertTrue(body.contains("admin"))
    }

    @Test
    fun `login with invalid credentials returns 401`() = testApplication {
        configureApp()

        val response = client.post("/api/v1/auth/login") {
            contentType(ContentType.Application.Json)
            setBody("""{"username":"admin","password":"wrongpassword"}""")
        }

        assertEquals(HttpStatusCode.Unauthorized, response.status)
    }

    @Test
    fun `login with empty body returns 400`() = testApplication {
        configureApp()

        val response = client.post("/api/v1/auth/login") {
            contentType(ContentType.Application.Json)
            setBody("""{"username":"","password":""}""")
        }

        assertEquals(HttpStatusCode.BadRequest, response.status)
    }

    @Test
    fun `change-password without token returns 401`() = testApplication {
        configureApp()

        val response = client.post("/api/v1/auth/change-password") {
            contentType(ContentType.Application.Json)
            setBody("""{"oldPassword":"x","newPassword":"y"}""")
        }

        assertEquals(HttpStatusCode.Unauthorized, response.status)
    }

    @Test
    fun `change-password updates the password and revokes previous tokens`() = testApplication {
        configureApp()
        val staleToken = freshToken()

        val response = client.post("/api/v1/auth/change-password") {
            bearerAuth(staleToken)
            contentType(ContentType.Application.Json)
            setBody("""{"oldPassword":"${TestSeed.ADMIN_PASSWORD}","newPassword":"NewPass123"}""")
        }

        assertEquals(HttpStatusCode.OK, response.status)

        val replay = client.post("/api/v1/auth/change-password") {
            bearerAuth(staleToken)
            contentType(ContentType.Application.Json)
            setBody("""{"oldPassword":"NewPass123","newPassword":"NewPass456"}""")
        }
        assertEquals(HttpStatusCode.Unauthorized, replay.status)

        val restore = authService.changePassword(userId, "NewPass123", TestSeed.ADMIN_PASSWORD)
        assertTrue(restore.isSuccess)
    }

    @Test
    fun `change-password with wrong current password returns 400`() = testApplication {
        configureApp()

        val response = client.post("/api/v1/auth/change-password") {
            bearerAuth(freshToken())
            contentType(ContentType.Application.Json)
            setBody("""{"oldPassword":"WrongOld1","newPassword":"NewPass123"}""")
        }

        assertEquals(HttpStatusCode.BadRequest, response.status)
        assertTrue(response.bodyAsText().contains("contraseña actual"))
    }

    @Test
    fun `change-password with weak password returns 400`() = testApplication {
        configureApp()

        val response = client.post("/api/v1/auth/change-password") {
            bearerAuth(freshToken())
            contentType(ContentType.Application.Json)
            setBody("""{"oldPassword":"${TestSeed.ADMIN_PASSWORD}","newPassword":"ab1"}""")
        }

        assertEquals(HttpStatusCode.BadRequest, response.status)
        assertTrue(response.bodyAsText().contains("Password inválido"))
    }
}
