package com.fireflow.server.features

import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.server.response.respondText
import io.ktor.server.routing.*
import io.ktor.server.testing.*
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

class CORSTest {

    @Test
    fun `configured origin is allowed`() = testApplication {
        application {
            configureCORS(allowedOriginsEnv = "https://app.example.com", isProduction = true)
            routing { get("/health") { call.respondText("ok") } }
        }

        val response = client.get("/health") {
            header(HttpHeaders.Origin, "https://app.example.com")
        }

        assertEquals(
            "https://app.example.com",
            response.headers[HttpHeaders.AccessControlAllowOrigin]
        )
    }

    @Test
    fun `unknown origin gets no CORS headers`() = testApplication {
        application {
            configureCORS(allowedOriginsEnv = "https://app.example.com", isProduction = true)
            routing { get("/health") { call.respondText("ok") } }
        }

        val response = client.get("/health") {
            header(HttpHeaders.Origin, "https://evil.example.com")
        }

        assertNull(response.headers[HttpHeaders.AccessControlAllowOrigin])
    }

    @Test
    fun `production without ALLOWED_ORIGINS rejects every origin`() = testApplication {
        application {
            configureCORS(allowedOriginsEnv = null, isProduction = true)
            routing { get("/health") { call.respondText("ok") } }
        }

        val response = client.get("/health") {
            header(HttpHeaders.Origin, "https://app.example.com")
        }

        assertNull(response.headers[HttpHeaders.AccessControlAllowOrigin])
    }

    @Test
    fun `development fallback allows any origin`() = testApplication {
        application {
            configureCORS(allowedOriginsEnv = null, isProduction = false)
            routing { get("/health") { call.respondText("ok") } }
        }

        val response = client.get("/health") {
            header(HttpHeaders.Origin, "http://localhost:3000")
        }

        assertEquals(
            "http://localhost:3000",
            response.headers[HttpHeaders.AccessControlAllowOrigin]
        )
    }

    @Test
    fun `origin list ignores blank entries and trailing slashes`() = testApplication {
        application {
            configureCORS(
                allowedOriginsEnv = " https://app.example.com/, , http://localhost:3000 ",
                isProduction = true
            )
            routing { get("/health") { call.respondText("ok") } }
        }

        val allowed = client.get("/health") {
            header(HttpHeaders.Origin, "https://app.example.com")
        }
        val rejected = client.get("/health") {
            header(HttpHeaders.Origin, "https://evil.example.com")
        }

        assertEquals(
            "https://app.example.com",
            allowed.headers[HttpHeaders.AccessControlAllowOrigin]
        )
        assertNull(rejected.headers[HttpHeaders.AccessControlAllowOrigin])
    }
}
