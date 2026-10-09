package com.fireflow.server.routes

import com.fireflow.server.TestSeed
import com.fireflow.server.config.DatabaseManager
import com.fireflow.server.config.DatabaseConnectionConfig
import com.fireflow.server.config.JwtConfig
import com.fireflow.server.features.configureSecurity
import com.fireflow.server.features.configureSerialization
import com.fireflow.server.models.*
import com.fireflow.server.models.request.SyncRequest
import com.fireflow.server.repositories.ClientRepository
import com.fireflow.server.repositories.CurveRepository
import com.fireflow.server.repositories.PressureGroupRepository
import com.fireflow.server.repositories.RevisionRepository
import com.fireflow.server.repositories.UserRepository
import com.fireflow.server.services.SyncService
import com.fireflow.server.utils.JwtUtils
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.routing.*
import io.ktor.server.testing.*
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class SyncRoutesTest {

    private lateinit var syncService: SyncService
    private lateinit var token: String
    private val jwtSecret = "test-secret-key-for-testing-only-1234567890"

    @BeforeAll
    fun setup() {
        DatabaseManager.init(DatabaseConnectionConfig(
            url = "jdbc:h2:mem:test_sync_routes;DB_CLOSE_DELAY=-1",
            driver = "org.h2.Driver",
            user = "sa",
            password = ""
        ))

        val database = DatabaseManager.getDatabase()
        val clientRepo = ClientRepository(database)
        val groupRepo = PressureGroupRepository(database)
        val revisionRepo = RevisionRepository(database)
        val curveRepo = CurveRepository(database)

        syncService = SyncService(clientRepo, groupRepo, revisionRepo, curveRepo)

        JwtUtils.init(JwtConfig(secret = jwtSecret))
        val userRepository = UserRepository(database)
        val userId = TestSeed.seedAdmin(userRepository)
        token = JwtUtils.generateToken(userId, TestSeed.ADMIN_USERNAME, "admin")
    }

    @AfterAll
    fun teardown() {
        DatabaseManager.close()
    }

    private fun ApplicationTestBuilder.configureApp() {
        val database = DatabaseManager.getDatabase()
        val userRepository = UserRepository(database)
        application {
            configureSerialization()
            configureSecurity(JwtConfig(secret = jwtSecret), userRepository)
            routing {
                syncRoutes(syncService)
            }
        }
    }

    // --- Auth ---

    @Test
    fun `sync clients without auth returns 401`() = testApplication {
        configureApp()

        val response = client.post("/api/v1/clients/sync") {
            contentType(ContentType.Application.Json)
            setBody("""{"data":[],"lastSyncTimestamp":0}""")
        }

        assertEquals(HttpStatusCode.Unauthorized, response.status)
    }

    @Test
    fun `sync groups without auth returns 401`() = testApplication {
        configureApp()

        val response = client.post("/api/v1/groups/sync") {
            contentType(ContentType.Application.Json)
            setBody("""{"data":[],"lastSyncTimestamp":0}""")
        }

        assertEquals(HttpStatusCode.Unauthorized, response.status)
    }

    @Test
    fun `sync revisions without auth returns 401`() = testApplication {
        configureApp()

        val response = client.post("/api/v1/revisions/sync") {
            contentType(ContentType.Application.Json)
            setBody("""{"data":[],"lastSyncTimestamp":0}""")
        }

        assertEquals(HttpStatusCode.Unauthorized, response.status)
    }

    @Test
    fun `sync curves without auth returns 401`() = testApplication {
        configureApp()

        val response = client.post("/api/v1/curves/sync") {
            contentType(ContentType.Application.Json)
            setBody("""{"data":[],"lastSyncTimestamp":0}""")
        }

        assertEquals(HttpStatusCode.Unauthorized, response.status)
    }

    // --- Clients ---

    @Test
    fun `sync clients with auth returns 200`() = testApplication {
        configureApp()

        val response = client.post("/api/v1/clients/sync") {
            contentType(ContentType.Application.Json)
            header(HttpHeaders.Authorization, "Bearer $token")
            setBody("""{"data":[],"lastSyncTimestamp":0}""")
        }

        assertEquals(HttpStatusCode.OK, response.status)
        val body = response.bodyAsText()
        assertTrue(body.contains("data"))
        assertTrue(body.contains("syncTimestamp"))
    }

    @Test
    fun `sync clients with data returns synced records`() = testApplication {
        configureApp()

        val now = System.currentTimeMillis()
        val clientJson = """{"id":1100,"name":"Test Client","cif":"B11000000","address":"Address","province":"Madrid","contactPerson":"Person","phone":"+34600000000","email":"test@test.com","latitude":40.4168,"longitude":-3.7038,"notes":"notes","createdAt":$now,"updatedAt":$now,"isActive":true}"""

        val response = client.post("/api/v1/clients/sync") {
            contentType(ContentType.Application.Json)
            header(HttpHeaders.Authorization, "Bearer $token")
            setBody("""{"data":[$clientJson],"lastSyncTimestamp":0}""")
        }

        assertEquals(HttpStatusCode.OK, response.status)
        assertTrue(response.bodyAsText().contains("Test Client"))
    }

    // --- Groups ---

    @Test
    fun `sync groups with auth returns 200`() = testApplication {
        configureApp()

        val response = client.post("/api/v1/groups/sync") {
            contentType(ContentType.Application.Json)
            header(HttpHeaders.Authorization, "Bearer $token")
            setBody("""{"data":[],"lastSyncTimestamp":0}""")
        }

        assertEquals(HttpStatusCode.OK, response.status)
        val body = response.bodyAsText()
        assertTrue(body.contains("data"))
        assertTrue(body.contains("syncTimestamp"))
    }

    @Test
    fun `sync groups with data returns synced records`() = testApplication {
        configureApp()

        val now = System.currentTimeMillis()
        val clientJson = """{"id":1200,"name":"Group Client","cif":"B12000000","address":"Address","province":"Madrid","contactPerson":"Person","phone":"+34600000000","email":"test@test.com","latitude":40.4168,"longitude":-3.7038,"notes":"notes","createdAt":$now,"updatedAt":$now,"isActive":true}"""

        client.post("/api/v1/clients/sync") {
            contentType(ContentType.Application.Json)
            header(HttpHeaders.Authorization, "Bearer $token")
            setBody("""{"data":[$clientJson],"lastSyncTimestamp":0}""")
        }

        val groupJson = """{"id":1201,"clientId":1200,"brand":"Grundfos","model":"CR 32-4","serialNumber":"SN1201","pumpNumber":"P001","manufacturer":"Grundfos","power":"5.5","installationDate":null,"maintenanceDate":null,"createdAt":$now,"updatedAt":$now,"isActive":true}"""

        val response = client.post("/api/v1/groups/sync") {
            contentType(ContentType.Application.Json)
            header(HttpHeaders.Authorization, "Bearer $token")
            setBody("""{"data":[$groupJson],"lastSyncTimestamp":0}""")
        }

        assertEquals(HttpStatusCode.OK, response.status)
        assertTrue(response.bodyAsText().contains("Grundfos"))
    }

    // --- Revisions ---

    @Test
    fun `sync revisions with auth returns 200`() = testApplication {
        configureApp()

        val response = client.post("/api/v1/revisions/sync") {
            contentType(ContentType.Application.Json)
            header(HttpHeaders.Authorization, "Bearer $token")
            setBody("""{"data":[],"lastSyncTimestamp":0}""")
        }

        assertEquals(HttpStatusCode.OK, response.status)
    }

    @Test
    fun `sync revisions with data returns synced records`() = testApplication {
        configureApp()

        val now = System.currentTimeMillis()
        val clientJson = """{"id":1300,"name":"Rev Client","cif":"B13000000","address":"Address","province":"Madrid","contactPerson":"Person","phone":"+34600000000","email":"test@test.com","latitude":40.4168,"longitude":-3.7038,"notes":"notes","createdAt":$now,"updatedAt":$now,"isActive":true}"""
        client.post("/api/v1/clients/sync") {
            contentType(ContentType.Application.Json)
            header(HttpHeaders.Authorization, "Bearer $token")
            setBody("""{"data":[$clientJson],"lastSyncTimestamp":0}""")
        }

        val groupJson = """{"id":1301,"clientId":1300,"brand":"Grundfos","model":"CR 32-4","serialNumber":"SN1301","pumpNumber":"P001","manufacturer":"Grundfos","power":"5.5","installationDate":null,"maintenanceDate":null,"createdAt":$now,"updatedAt":$now,"isActive":true}"""
        client.post("/api/v1/groups/sync") {
            contentType(ContentType.Application.Json)
            header(HttpHeaders.Authorization, "Bearer $token")
            setBody("""{"data":[$groupJson],"lastSyncTimestamp":0}""")
        }

        val revisionJson = """{"id":1302,"groupId":1301,"date":$now,"technicianName":"Tech 1302","notes":"Revision notes","checklistResults":{"fuga":true,"ruido":false},"createdAt":$now,"updatedAt":$now}"""

        val response = client.post("/api/v1/revisions/sync") {
            contentType(ContentType.Application.Json)
            header(HttpHeaders.Authorization, "Bearer $token")
            setBody("""{"data":[$revisionJson],"lastSyncTimestamp":0}""")
        }

        assertEquals(HttpStatusCode.OK, response.status)
        assertTrue(response.bodyAsText().contains("Tech 1302"))
    }

    // --- Curves ---

    @Test
    fun `sync curves with auth returns 200`() = testApplication {
        configureApp()

        val response = client.post("/api/v1/curves/sync") {
            contentType(ContentType.Application.Json)
            header(HttpHeaders.Authorization, "Bearer $token")
            setBody("""{"data":[],"lastSyncTimestamp":0}""")
        }

        assertEquals(HttpStatusCode.OK, response.status)
    }

    @Test
    fun `sync curves with data returns synced records`() = testApplication {
        configureApp()

        val now = System.currentTimeMillis()
        val clientJson = """{"id":1400,"name":"Curve Client","cif":"B14000000","address":"Address","province":"Madrid","contactPerson":"Person","phone":"+34600000000","email":"test@test.com","latitude":40.4168,"longitude":-3.7038,"notes":"notes","createdAt":$now,"updatedAt":$now,"isActive":true}"""
        client.post("/api/v1/clients/sync") {
            contentType(ContentType.Application.Json)
            header(HttpHeaders.Authorization, "Bearer $token")
            setBody("""{"data":[$clientJson],"lastSyncTimestamp":0}""")
        }

        val groupJson = """{"id":1401,"clientId":1400,"brand":"Grundfos","model":"CR 32-4","serialNumber":"SN1401","pumpNumber":"P001","manufacturer":"Grundfos","power":"5.5","installationDate":null,"maintenanceDate":null,"createdAt":$now,"updatedAt":$now,"isActive":true}"""
        client.post("/api/v1/groups/sync") {
            contentType(ContentType.Application.Json)
            header(HttpHeaders.Authorization, "Bearer $token")
            setBody("""{"data":[$groupJson],"lastSyncTimestamp":0}""")
        }

        val revisionJson = """{"id":1402,"groupId":1401,"date":$now,"technicianName":"Tech","notes":"notes","checklistResults":{},"createdAt":$now,"updatedAt":$now}"""
        client.post("/api/v1/revisions/sync") {
            contentType(ContentType.Application.Json)
            header(HttpHeaders.Authorization, "Bearer $token")
            setBody("""{"data":[$revisionJson],"lastSyncTimestamp":0}""")
        }

        val curveJson = """{"id":1403,"revisionId":1402,"motorId":1,"flow":12.5,"pressure":4.5,"orderIndex":0}"""

        val response = client.post("/api/v1/curves/sync") {
            contentType(ContentType.Application.Json)
            header(HttpHeaders.Authorization, "Bearer $token")
            setBody("""{"data":[$curveJson],"lastSyncTimestamp":0}""")
        }

        assertEquals(HttpStatusCode.OK, response.status)
        assertTrue(response.bodyAsText().contains("12.5"))
    }

    // --- Invalid body ---

    @Test
    fun `sync clients with invalid body returns error`() = testApplication {
        configureApp()

        val response = client.post("/api/v1/clients/sync") {
            contentType(ContentType.Application.Json)
            header(HttpHeaders.Authorization, "Bearer $token")
            setBody("""{"invalid": true}""")
        }

        assertEquals(HttpStatusCode.InternalServerError, response.status)
    }
}
