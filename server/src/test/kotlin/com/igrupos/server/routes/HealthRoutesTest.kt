package com.igrupos.server.routes

import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.application.*
import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.server.routing.*
import io.ktor.server.testing.*
import kotlinx.serialization.json.Json
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

class HealthRoutesTest {

    @Test
    fun `GET health returns ok status`() = testApplication {
        application {
            install(ContentNegotiation) {
                json(Json { ignoreUnknownKeys = true })
            }
            routing {
                healthRoutes()
            }
        }

        val response = client.get("/health")

        assertEquals(HttpStatusCode.OK, response.status)
        val body = response.bodyAsText()
        assertTrue(body.contains("ok"))
        assertTrue(body.contains("igrupos-server"))
        assertTrue(body.contains("timestamp"))
    }
}
