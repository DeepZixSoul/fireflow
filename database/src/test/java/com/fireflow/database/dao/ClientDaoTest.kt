package com.fireflow.database.dao

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.fireflow.database.AppDatabase
import com.fireflow.database.entity.ClientEntity
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

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [26])
class ClientDaoTest {

    private lateinit var db: AppDatabase
    private lateinit var dao: ClientDao

    @Before
    fun setup() {
        db = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            AppDatabase::class.java
        ).allowMainThreadQueries().build()
        dao = db.clientDao()
    }

    @After
    fun teardown() {
        db.close()
    }

    private fun createClient(
        name: String = "Empresa Test",
        cif: String = "B12345678"
    ) = ClientEntity(
        name = name,
        cif = cif,
        address = "Calle Test 1",
        province = "Madrid"
    )

    @Test
    fun `insert and get by id`() = runTest {
        val entity = createClient()
        val id = dao.insert(entity)
        val loaded = dao.getById(id)

        assertNotNull(loaded)
        assertEquals("Empresa Test", loaded?.name)
    }

    @Test
    fun `get all active returns only active clients`() = runTest {
        dao.insert(createClient(name = "Active", cif = "B11111111"))
        dao.insert(createClient(name = "Also Active", cif = "B22222222"))

        val allActive = dao.getAllActive().first()
        assertEquals(2, allActive.size)
    }

    @Test
    fun `soft delete removes from active list`() = runTest {
        val id = dao.insert(createClient())
        dao.softDelete(id)

        val allActive = dao.getAllActive().first()
        assertTrue(allActive.isEmpty())
    }

    @Test
    fun `search by name`() = runTest {
        dao.insert(createClient(name = "Empresa Alpha", cif = "B11111111"))
        dao.insert(createClient(name = "Beta Corp", cif = "B22222222"))

        val results = dao.search("Alpha").first()
        assertEquals(1, results.size)
        assertEquals("Empresa Alpha", results[0].name)
    }

    @Test
    fun `search by cif`() = runTest {
        dao.insert(createClient(name = "Client A", cif = "B12345678"))

        val results = dao.search("12345").first()
        assertEquals(1, results.size)
    }
}
