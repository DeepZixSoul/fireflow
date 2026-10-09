package com.fireflow.server.routes

import com.fireflow.server.TestSeed
import com.fireflow.server.config.DatabaseManager
import com.fireflow.server.config.DatabaseConnectionConfig
import com.fireflow.server.config.JwtConfig
import com.fireflow.server.repositories.UserRepository
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

    @BeforeAll
    fun setup() {
        DatabaseManager.init(DatabaseConnectionConfig(
            url = "jdbc:h2:mem:test_auth;DB_CLOSE_DELAY=-1",
            driver = "org.h2.Driver",
            user = "sa",
            password = ""
        ))
        JwtUtils.init(JwtConfig(secret = "test-secret-key-for-testing-only-1234567890"))
        val userRepository = UserRepository(DatabaseManager.getDatabase())
        authService = AuthService(userRepository)
        TestSeed.seedAdmin(userRepository)
    }

    @AfterAll
    fun teardown() {
        DatabaseManager.close()
    }

    @Test
    fun `login with valid credentials returns token`() = testApplication {
        application {
            install(ContentNegotiation) {
                json(Json { ignoreUnknownKeys = true })
            }
            routing {
                authRoutes(authService)
            }
        }

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
        application {
            install(ContentNegotiation) {
                json(Json { ignoreUnknownKeys = true })
            }
            routing {
                authRoutes(authService)
            }
        }

        val response = client.post("/api/v1/auth/login") {
            contentType(ContentType.Application.Json)
            setBody("""{"username":"admin","password":"wrongpassword"}""")
        }

        assertEquals(HttpStatusCode.Unauthorized, response.status)
    }

    @Test
    fun `login with empty body returns 400`() = testApplication {
        application {
            install(ContentNegotiation) {
                json(Json { ignoreUnknownKeys = true })
            }
            routing {
                authRoutes(authService)
            }
        }

        val response = client.post("/api/v1/auth/login") {
            contentType(ContentType.Application.Json)
            setBody("""{"username":"","password":""}""")
        }

        assertEquals(HttpStatusCode.BadRequest, response.status)
    }
}
