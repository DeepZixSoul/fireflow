package com.igrupos.database.dao

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.igrupos.database.AppDatabase
import com.igrupos.database.entity.ClientEntity
import com.igrupos.database.entity.PressureGroupEntity
import com.igrupos.database.entity.RevisionEntity
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import kotlinx.coroutines.runBlocking

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [26])
class RevisionDaoTest {

    private lateinit var db: AppDatabase
    private lateinit var revisionDao: RevisionDao
    private lateinit var clientDao: ClientDao
    private lateinit var groupDao: PressureGroupDao

    private var groupId = 0L

    @Before
    fun setup() {
        db = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            AppDatabase::class.java
        ).allowMainThreadQueries().build()
        revisionDao = db.revisionDao()
        clientDao = db.clientDao()
        groupDao = db.pressureGroupDao()
    }

    @After
    fun teardown() {
        db.close()
    }

    private fun ensureGroup() {
        if (groupId == 0L) {
            groupId = runBlocking {
                val clientId = clientDao.insert(ClientEntity(name = "Test Client", cif = "B12345678"))
                groupDao.insert(PressureGroupEntity(clientId = clientId, brand = "Test"))
            }
        }
    }

    private fun createRevision(
        date: Long = System.currentTimeMillis(),
        technicianName: String = "Tecnico 1"
    ) = RevisionEntity(
        groupId = groupId,
        date = date,
        technicianName = technicianName,
        notes = "Observaciones de prueba"
    )

    @Test
    fun `insert and get by id`() = runTest {
        ensureGroup()
        val entity = createRevision()
        val id = revisionDao.insert(entity)
        val loaded = revisionDao.getById(id)

        assertNotNull(loaded)
        assertEquals("Tecnico 1", loaded?.technicianName)
    }

    @Test
    fun `get by group ordered by date desc`() = runTest {
        ensureGroup()
        val now = System.currentTimeMillis()
        revisionDao.insert(createRevision(date = now - 100000, technicianName = "Old"))
        revisionDao.insert(createRevision(date = now, technicianName = "New"))

        val revisions = revisionDao.getByGroup(groupId).first()
        assertEquals(2, revisions.size)
        assertEquals("New", revisions[0].technicianName)
    }

    @Test
    fun `get last by group returns most recent`() = runTest {
        ensureGroup()
        val now = System.currentTimeMillis()
        revisionDao.insert(createRevision(date = now - 100000, technicianName = "Old"))
        revisionDao.insert(createRevision(date = now, technicianName = "New"))

        val last = revisionDao.getLastByGroup(groupId)
        assertNotNull(last)
        assertEquals("New", last?.technicianName)
    }

    @Test
    fun `search by technician name`() = runTest {
        ensureGroup()
        revisionDao.insert(createRevision(technicianName = "Juan Garcia"))
        revisionDao.insert(createRevision(technicianName = "Maria Lopez"))

        val results = revisionDao.search("Juan").first()
        assertEquals(1, results.size)
        assertEquals("Juan Garcia", results[0].technicianName)
    }

    @Test
    fun `delete by id removes revision`() = runTest {
        ensureGroup()
        val id = revisionDao.insert(createRevision())
        revisionDao.deleteById(id)

        assertNull(revisionDao.getById(id))
    }
}
