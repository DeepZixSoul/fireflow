package com.igrupos.database.dao

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.igrupos.database.AppDatabase
import com.igrupos.database.entity.UserEntity
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
class UserDaoTest {

    private lateinit var db: AppDatabase
    private lateinit var dao: UserDao

    @Before
    fun setup() {
        db = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            AppDatabase::class.java
        ).allowMainThreadQueries().build()
        dao = db.userDao()
    }

    @After
    fun teardown() {
        db.close()
    }

    private fun createUser(
        username: String = "testuser",
        displayName: String = "Test User",
        email: String = "test@test.com",
        passwordHash: String = "hash123",
        role: String = "TECHNICIAN",
        isActive: Boolean = true,
        mustChangePassword: Boolean = false
    ) = UserEntity(
        username = username,
        displayName = displayName,
        email = email,
        passwordHash = passwordHash,
        role = role,
        isActive = isActive,
        mustChangePassword = mustChangePassword
    )

    @Test
    fun `insert and get by username`() = runTest {
        val entity = createUser(username = "admin")
        val id = dao.insert(entity)
        val loaded = dao.getByUsername("admin")

        assertNotNull(loaded)
        assertEquals(id, loaded?.id)
        assertEquals("admin", loaded?.username)
    }

    @Test
    fun `get by username returns null for non-existent`() = runTest {
        val result = dao.getByUsername("nonexistent")
        assertNull(result)
    }

    @Test
    fun `get by id`() = runTest {
        val entity = createUser()
        val id = dao.insert(entity)
        val loaded = dao.getById(id)

        assertNotNull(loaded)
        assertEquals("testuser", loaded?.username)
    }

    @Test
    fun `update password changes hash`() = runTest {
        val entity = createUser(passwordHash = "old_hash")
        val id = dao.insert(entity)

        dao.updatePassword(id, "new_hash", System.currentTimeMillis())
        val loaded = dao.getById(id)

        assertEquals("new_hash", loaded?.passwordHash)
    }

    @Test
    fun `clear must change password`() = runTest {
        val entity = createUser(mustChangePassword = true)
        val id = dao.insert(entity)

        dao.clearMustChangePassword(id, System.currentTimeMillis())
        val loaded = dao.getById(id)

        assertEquals(false, loaded?.mustChangePassword)
    }

    @Test
    fun `insert multiple users`() = runTest {
        val ids = dao.insertAll(
            listOf(
                createUser(username = "user1"),
                createUser(username = "user2"),
                createUser(username = "user3")
            )
        )

        assertEquals(3, ids.size)
        assertNotNull(dao.getByUsername("user1"))
        assertNotNull(dao.getByUsername("user2"))
        assertNotNull(dao.getByUsername("user3"))
    }
}
