package com.igrupos.server.services

import com.igrupos.server.config.DatabaseManager
import com.igrupos.server.config.DatabaseConnectionConfig
import com.igrupos.server.models.*
import com.igrupos.server.models.request.SyncRequest
import com.igrupos.server.repositories.ClientRepository
import com.igrupos.server.repositories.CurveRepository
import com.igrupos.server.repositories.PressureGroupRepository
import com.igrupos.server.repositories.RevisionRepository
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class SyncServiceTest {

    private lateinit var syncService: SyncService

    @BeforeAll
    fun setup() {
        DatabaseManager.init(DatabaseConnectionConfig(
            url = "jdbc:h2:mem:test_sync_service;DB_CLOSE_DELAY=-1",
            driver = "org.h2.Driver",
            user = "sa",
            password = ""
        ))

        val database = DatabaseManager.getDatabase()
        syncService = SyncService(
            ClientRepository(database),
            PressureGroupRepository(database),
            RevisionRepository(database),
            CurveRepository(database)
        )
    }

    @AfterAll
    fun teardown() {
        DatabaseManager.close()
    }

    private fun testClient(id: Long = 100L) = Client(
        id = id, name = "Client $id", cif = "B${id}00000",
        address = "Address", province = "Madrid", contactPerson = "Person",
        phone = "+34600000000", email = "test@test.com",
        latitude = 40.4168, longitude = -3.7038, notes = "notes",
        createdAt = System.currentTimeMillis(), updatedAt = System.currentTimeMillis(),
        isActive = true
    )

    private fun testGroup(id: Long = 200L, clientId: Long = 100L) = PressureGroup(
        id = id, clientId = clientId, brand = "Grundfos", model = "CR 32-4",
        serialNumber = "SN$id", pumpNumber = "P001", manufacturer = "Grundfos",
        power = "5.5", installationDate = System.currentTimeMillis(),
        maintenanceDate = System.currentTimeMillis(),
        createdAt = System.currentTimeMillis(), updatedAt = System.currentTimeMillis(),
        isActive = true
    )

    private fun testRevision(id: Long = 300L, groupId: Long = 200L) = Revision(
        id = id, groupId = groupId, date = System.currentTimeMillis(),
        technicianName = "Tech $id", notes = "Revision notes",
        checklistResults = mapOf("fuga" to true, "ruido" to false),
        createdAt = System.currentTimeMillis(), updatedAt = System.currentTimeMillis()
    )

    private fun testCurve(id: Long = 400L, revisionId: Long = 300L) = CurvePoint(
        id = id, revisionId = revisionId, motorId = 1L,
        flow = 12.5, pressure = 4.5, orderIndex = 0
    )

    // --- Clients ---

    @Test
    fun `sync clients with empty data returns valid response`() {
        val request = SyncRequest<Client>(data = emptyList(), lastSyncTimestamp = 0)
        val response = syncService.syncClients(request)

        assertNotNull(response)
        assertTrue(response.syncTimestamp > 0)
    }

    @Test
    fun `sync clients with data stores and returns them`() {
        val client = testClient(id = 600L)
        val request = SyncRequest(data = listOf(client), lastSyncTimestamp = 0)
        val response = syncService.syncClients(request)

        assertNotNull(response)
        assertTrue(response.data.isNotEmpty())
        assertTrue(response.syncTimestamp > 0)
        assertEquals("Client 600", response.data.first { it.id == 600L }.name)
    }

    @Test
    fun `sync clients upsert updates existing record`() {
        val client = testClient(id = 601L)
        syncService.syncClients(SyncRequest(data = listOf(client), lastSyncTimestamp = 0))

        val updated = client.copy(name = "Updated Client")
        val response = syncService.syncClients(SyncRequest(data = listOf(updated), lastSyncTimestamp = 0))

        val found = response.data.find { it.id == 601L }
        assertNotNull(found)
        assertEquals("Updated Client", found!!.name)
    }

    @Test
    fun `sync clients with future timestamp returns no new records`() {
        val client = testClient(id = 602L)
        syncService.syncClients(SyncRequest(data = listOf(client), lastSyncTimestamp = 0))

        val futureTimestamp = System.currentTimeMillis() + 60000
        val response = syncService.syncClients(SyncRequest(data = emptyList(), lastSyncTimestamp = futureTimestamp))

        val found = response.data.find { it.id == 602L }
        assertNull(found)
    }

    // --- Groups ---

    @Test
    fun `sync groups with empty data returns valid response`() {
        val request = SyncRequest<PressureGroup>(data = emptyList(), lastSyncTimestamp = 0)
        val response = syncService.syncGroups(request)

        assertNotNull(response)
        assertTrue(response.syncTimestamp > 0)
    }

    @Test
    fun `sync groups stores and returns them`() {
        val client = testClient(id = 700L)
        syncService.syncClients(SyncRequest(data = listOf(client), lastSyncTimestamp = 0))

        val group = testGroup(id = 701L, clientId = 700L)
        val response = syncService.syncGroups(SyncRequest(data = listOf(group), lastSyncTimestamp = 0))

        assertTrue(response.data.isNotEmpty())
        assertNotNull(response.data.find { it.id == 701L })
    }

    @Test
    fun `sync groups upsert updates existing record`() {
        val client = testClient(id = 710L)
        syncService.syncClients(SyncRequest(data = listOf(client), lastSyncTimestamp = 0))

        val group = testGroup(id = 711L, clientId = 710L)
        syncService.syncGroups(SyncRequest(data = listOf(group), lastSyncTimestamp = 0))

        val updated = group.copy(brand = "Wilo")
        val response = syncService.syncGroups(SyncRequest(data = listOf(updated), lastSyncTimestamp = 0))

        val found = response.data.find { it.id == 711L }
        assertNotNull(found)
        assertEquals("Wilo", found!!.brand)
    }

    // --- Revisions ---

    @Test
    fun `sync revisions with empty data returns valid response`() {
        val request = SyncRequest<Revision>(data = emptyList(), lastSyncTimestamp = 0)
        val response = syncService.syncRevisions(request)

        assertNotNull(response)
        assertTrue(response.syncTimestamp > 0)
    }

    @Test
    fun `sync revisions stores and returns them`() {
        val client = testClient(id = 800L)
        syncService.syncClients(SyncRequest(data = listOf(client), lastSyncTimestamp = 0))
        val group = testGroup(id = 801L, clientId = 800L)
        syncService.syncGroups(SyncRequest(data = listOf(group), lastSyncTimestamp = 0))

        val revision = testRevision(id = 802L, groupId = 801L)
        val response = syncService.syncRevisions(SyncRequest(data = listOf(revision), lastSyncTimestamp = 0))

        assertTrue(response.data.isNotEmpty())
        assertNotNull(response.data.find { it.id == 802L })
    }

    @Test
    fun `sync revisions upsert updates existing record`() {
        val client = testClient(id = 810L)
        syncService.syncClients(SyncRequest(data = listOf(client), lastSyncTimestamp = 0))
        val group = testGroup(id = 811L, clientId = 810L)
        syncService.syncGroups(SyncRequest(data = listOf(group), lastSyncTimestamp = 0))

        val revision = testRevision(id = 812L, groupId = 811L)
        syncService.syncRevisions(SyncRequest(data = listOf(revision), lastSyncTimestamp = 0))

        val updated = revision.copy(technicianName = "Updated Tech", notes = "Updated notes")
        val response = syncService.syncRevisions(SyncRequest(data = listOf(updated), lastSyncTimestamp = 0))

        val found = response.data.find { it.id == 812L }
        assertNotNull(found)
        assertEquals("Updated Tech", found!!.technicianName)
    }

    // --- Curves ---

    @Test
    fun `sync curves with empty data returns valid response`() {
        val request = SyncRequest<CurvePoint>(data = emptyList(), lastSyncTimestamp = 0)
        val response = syncService.syncCurves(request)

        assertNotNull(response)
        assertTrue(response.syncTimestamp > 0)
    }

    @Test
    fun `sync curves stores and returns them`() {
        val client = testClient(id = 900L)
        syncService.syncClients(SyncRequest(data = listOf(client), lastSyncTimestamp = 0))
        val group = testGroup(id = 901L, clientId = 900L)
        syncService.syncGroups(SyncRequest(data = listOf(group), lastSyncTimestamp = 0))
        val revision = testRevision(id = 902L, groupId = 901L)
        syncService.syncRevisions(SyncRequest(data = listOf(revision), lastSyncTimestamp = 0))

        val curve = testCurve(id = 903L, revisionId = 902L)
        val response = syncService.syncCurves(SyncRequest(data = listOf(curve), lastSyncTimestamp = 0))

        assertTrue(response.data.isNotEmpty())
        assertEquals(12.5, response.data.first { it.id == 903L }.flow, 0.001)
    }

    @Test
    fun `sync curves upsert updates existing record`() {
        val client = testClient(id = 910L)
        syncService.syncClients(SyncRequest(data = listOf(client), lastSyncTimestamp = 0))
        val group = testGroup(id = 911L, clientId = 910L)
        syncService.syncGroups(SyncRequest(data = listOf(group), lastSyncTimestamp = 0))
        val revision = testRevision(id = 912L, groupId = 911L)
        syncService.syncRevisions(SyncRequest(data = listOf(revision), lastSyncTimestamp = 0))

        val curve = testCurve(id = 913L, revisionId = 912L)
        syncService.syncCurves(SyncRequest(data = listOf(curve), lastSyncTimestamp = 0))

        val updated = curve.copy(flow = 25.0, pressure = 8.0)
        val response = syncService.syncCurves(SyncRequest(data = listOf(updated), lastSyncTimestamp = 0))

        val found = response.data.find { it.id == 913L }
        assertNotNull(found)
        assertEquals(25.0, found!!.flow, 0.001)
    }

    // --- Full sync chain ---

    @Test
    fun `full sync chain stores all entities`() {
        val client = testClient(id = 1000L)
        syncService.syncClients(SyncRequest(data = listOf(client), lastSyncTimestamp = 0))

        val group = testGroup(id = 1001L, clientId = 1000L)
        syncService.syncGroups(SyncRequest(data = listOf(group), lastSyncTimestamp = 0))

        val revision = testRevision(id = 1002L, groupId = 1001L)
        syncService.syncRevisions(SyncRequest(data = listOf(revision), lastSyncTimestamp = 0))

        val curve = testCurve(id = 1003L, revisionId = 1002L)
        val curveResponse = syncService.syncCurves(SyncRequest(data = listOf(curve), lastSyncTimestamp = 0))

        assertNotNull(curveResponse.data.find { it.id == 1003L })
    }
}
