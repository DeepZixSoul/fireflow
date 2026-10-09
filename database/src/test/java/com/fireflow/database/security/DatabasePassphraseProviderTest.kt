package com.fireflow.database.security

import org.junit.After
import org.junit.Assert.assertArrayEquals
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.io.File
import java.nio.file.Files

class DatabasePassphraseProviderTest {

    private val legacy = "legacy-apk-passphrase".toByteArray()
    private val storedPassphrase = "stored-passphrase-0123456789".toByteArray()

    private lateinit var dbFile: File

    @Before
    fun setup() {
        dbFile = Files.createTempFile("fireflow-test", ".db").toFile()
        dbFile.delete()
    }

    @After
    fun teardown() {
        dbFile.delete()
    }

    @Test
    fun `fresh install generates and stores a 32 byte passphrase`() {
        val store = FakeStore()
        val rekeyer = FakeRekeyer()

        val passphrase = provider(store, rekeyer).providePassphrase()

        assertEquals(32, passphrase.size)
        assertArrayEquals(passphrase, store.data)
        assertTrue(rekeyer.rekeys.isEmpty())
        assertTrue(rekeyer.canOpenCalls.isEmpty())
    }

    @Test
    fun `legacy install is rekeyed to a generated passphrase`() {
        dbFile.writeBytes(byteArrayOf(1))
        val store = FakeStore()
        val rekeyer = FakeRekeyer(openable = listOf(legacy))

        val passphrase = provider(store, rekeyer).providePassphrase()

        assertArrayEquals(passphrase, store.data)
        assertEquals(1, rekeyer.rekeys.size)
        assertArrayEquals(legacy, rekeyer.rekeys[0].first)
        assertArrayEquals(passphrase, rekeyer.rekeys[0].second)
    }

    @Test
    fun `stored passphrase that opens the database is reused`() {
        dbFile.writeBytes(byteArrayOf(1))
        val store = FakeStore(data = storedPassphrase)
        val rekeyer = FakeRekeyer(openable = listOf(storedPassphrase))

        val passphrase = provider(store, rekeyer).providePassphrase()

        assertArrayEquals(storedPassphrase, passphrase)
        assertTrue(rekeyer.rekeys.isEmpty())
        assertEquals(1, rekeyer.canOpenCalls.size)
    }

    @Test
    fun `interrupted migration retries the rekey with the stored passphrase`() {
        dbFile.writeBytes(byteArrayOf(1))
        val store = FakeStore(data = storedPassphrase)
        val rekeyer = FakeRekeyer(openable = listOf(legacy))

        val passphrase = provider(store, rekeyer).providePassphrase()

        assertArrayEquals(storedPassphrase, passphrase)
        assertEquals(1, rekeyer.rekeys.size)
        assertArrayEquals(legacy, rekeyer.rekeys[0].first)
        assertArrayEquals(storedPassphrase, rekeyer.rekeys[0].second)
    }

    @Test
    fun `stored passphrase without a database file is reused`() {
        val store = FakeStore(data = storedPassphrase)
        val rekeyer = FakeRekeyer()

        val passphrase = provider(store, rekeyer).providePassphrase()

        assertArrayEquals(storedPassphrase, passphrase)
        assertTrue(rekeyer.rekeys.isEmpty())
        assertTrue(rekeyer.canOpenCalls.isEmpty())
    }

    @Test
    fun `unreadable database surfaces the stored passphrase to Room`() {
        dbFile.writeBytes(byteArrayOf(1))
        val store = FakeStore(data = storedPassphrase)
        val rekeyer = FakeRekeyer(openable = emptyList())

        val passphrase = provider(store, rekeyer).providePassphrase()

        assertArrayEquals(storedPassphrase, passphrase)
        assertTrue(rekeyer.rekeys.isEmpty())
    }

    @Test
    fun `write is called before the rekey`() {
        dbFile.writeBytes(byteArrayOf(1))
        val events = mutableListOf<String>()
        val store = object : DbPassphraseStore {
            var value: ByteArray? = null
            override fun read(): ByteArray? = value
            override fun write(passphrase: ByteArray) {
                value = passphrase
                events.add("write")
            }
        }
        val rekeyer = object : DatabaseRekeyer {
            override fun canOpen(dbFile: File, passphrase: ByteArray) = false
            override fun rekey(dbFile: File, oldPassphrase: ByteArray, newPassphrase: ByteArray) {
                assertNotNull(store.value)
                events.add("rekey")
            }
        }

        provider(store, rekeyer).providePassphrase()

        assertEquals(listOf("write", "rekey"), events)
    }

    private fun provider(store: DbPassphraseStore, rekeyer: DatabaseRekeyer) =
        DatabasePassphraseProvider(
            dbFile = dbFile,
            store = store,
            legacyPassphrase = legacy,
            rekeyer = rekeyer
        )

    private class FakeStore(var data: ByteArray? = null) : DbPassphraseStore {
        override fun read(): ByteArray? = data
        override fun write(passphrase: ByteArray) {
            data = passphrase
        }
    }

    private class FakeRekeyer(
        private val openable: List<ByteArray> = emptyList()
    ) : DatabaseRekeyer {
        val rekeys = mutableListOf<Pair<ByteArray, ByteArray>>()
        val canOpenCalls = mutableListOf<ByteArray>()

        override fun canOpen(dbFile: File, passphrase: ByteArray): Boolean {
            canOpenCalls.add(passphrase)
            return openable.any { it.contentEquals(passphrase) }
        }

        override fun rekey(dbFile: File, oldPassphrase: ByteArray, newPassphrase: ByteArray) {
            rekeys.add(oldPassphrase to newPassphrase)
        }
    }
}
