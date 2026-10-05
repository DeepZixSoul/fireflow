package com.igrupos.database.dao

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.igrupos.database.AppDatabase
import com.igrupos.database.entity.ClientEntity
import com.igrupos.database.entity.MotorEntity
import com.igrupos.database.entity.PressureGroupEntity
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
class MotorDaoTest {

    private lateinit var db: AppDatabase
    private lateinit var motorDao: MotorDao
    private lateinit var groupDao: PressureGroupDao
    private lateinit var clientDao: ClientDao

    private var groupId = 0L

    @Before
    fun setup() {
        db = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            AppDatabase::class.java
        ).allowMainThreadQueries().build()
        motorDao = db.motorDao()
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

    private fun createMotor(
        motorType: String = "ELECTRIC",
        nominalFlow: Double = 100.0,
        manometricHeight: Double = 50.0
    ) = MotorEntity(
        groupId = groupId,
        motorType = motorType,
        nominalFlow = nominalFlow,
        manometricHeight = manometricHeight
    )

    @Test
    fun `insert and get by id`() = runTest {
        ensureGroup()
        val entity = createMotor()
        val id = motorDao.insert(entity)
        val loaded = motorDao.getById(id)

        assertNotNull(loaded)
        assertEquals("ELECTRIC", loaded?.motorType)
        assertEquals(100.0, loaded?.nominalFlow ?: 0.0, 0.01)
    }

    @Test
    fun `get by group ordered by type`() = runTest {
        ensureGroup()
        motorDao.insert(createMotor(motorType = "DIESEL"))
        motorDao.insert(createMotor(motorType = "ELECTRIC"))

        val motors = motorDao.getByGroup(groupId).first()
        assertEquals(2, motors.size)
        assertEquals("DIESEL", motors[0].motorType)
        assertEquals("ELECTRIC", motors[1].motorType)
    }

    @Test
    fun `delete by group removes all motors`() = runTest {
        ensureGroup()
        motorDao.insert(createMotor(motorType = "ELECTRIC"))
        motorDao.insert(createMotor(motorType = "DIESEL"))

        motorDao.deleteByGroup(groupId)

        val motors = motorDao.getByGroup(groupId).first()
        assertEquals(0, motors.size)
    }

    @Test
    fun `get by id returns null for non-existent`() = runTest {
        assertNull(motorDao.getById(999L))
    }
}
