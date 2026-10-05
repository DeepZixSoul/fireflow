package com.igrupos.database.dao

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.igrupos.database.AppDatabase
import com.igrupos.database.entity.ClientEntity
import com.igrupos.database.entity.PressureGroupEntity
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import kotlinx.coroutines.runBlocking

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [26])
class PressureGroupDaoTest {

    private lateinit var db: AppDatabase
    private lateinit var groupDao: PressureGroupDao
    private lateinit var clientDao: ClientDao

    private var clientId = 0L

    @Before
    fun setup() {
        db = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            AppDatabase::class.java
        ).allowMainThreadQueries().build()
        groupDao = db.pressureGroupDao()
        clientDao = db.clientDao()
    }

    @After
    fun teardown() {
        db.close()
    }

    private fun ensureClient() {
        if (clientId == 0L) {
            clientId = runBlocking { clientDao.insert(ClientEntity(name = "Test Client", cif = "B12345678")) }
        }
    }

    private fun createGroup(
        brand: String = "Grundfos",
        serialNumber: String = "SN001"
    ) = PressureGroupEntity(
        clientId = clientId,
        brand = brand,
        model = "CR 32-4",
        serialNumber = serialNumber,
        power = "5.5kW"
    )

    @Test
    fun `insert and get by id`() = runTest {
        ensureClient()
        val entity = createGroup()
        val id = groupDao.insert(entity)
        val loaded = groupDao.getById(id)

        assertNotNull(loaded)
        assertEquals("Grundfos", loaded?.brand)
    }

    @Test
    fun `get by client returns only active groups`() = runTest {
        ensureClient()
        groupDao.insert(createGroup(brand = "Grundfos", serialNumber = "SN001"))
        groupDao.insert(createGroup(brand = "KSB", serialNumber = "SN002"))

        val groups = groupDao.getByClient(clientId).first()
        assertEquals(2, groups.size)
    }

    @Test
    fun `soft delete removes from active`() = runTest {
        ensureClient()
        val id = groupDao.insert(createGroup())
        groupDao.softDelete(id)

        val groups = groupDao.getByClient(clientId).first()
        assertTrue(groups.isEmpty())
    }

    @Test
    fun `get by id returns null for non-existent`() = runTest {
        assertNull(groupDao.getById(999L))
    }
}
