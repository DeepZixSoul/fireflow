package com.igrupos.data.mapper

import com.igrupos.database.entity.ClientEntity
import com.igrupos.domain.model.Client
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class ClientMapperTest {

    private val testEntity = ClientEntity(
        id = 1L,
        name = "Test Client",
        cif = "B12345678",
        address = "Calle Mayor 1",
        province = "Madrid",
        contactPerson = "Juan García",
        phone = "600123456",
        email = "test@example.com",
        latitude = 40.4168,
        longitude = -3.7038,
        notes = "Notas de prueba",
        createdAt = 1000L,
        updatedAt = 2000L,
        isActive = true,
        isDirty = false
    )

    private val testDomain = Client(
        id = 1L,
        name = "Test Client",
        cif = "B12345678",
        address = "Calle Mayor 1",
        province = "Madrid",
        contactPerson = "Juan García",
        phone = "600123456",
        email = "test@example.com",
        latitude = 40.4168,
        longitude = -3.7038,
        notes = "Notas de prueba",
        createdAt = 1000L,
        updatedAt = 2000L,
        isActive = true,
        isDirty = false
    )

    @Test
    fun `entity toDomain maps all fields correctly`() {
        val domain = testEntity.toDomain()

        assertEquals(1L, domain.id)
        assertEquals("Test Client", domain.name)
        assertEquals("B12345678", domain.cif)
        assertEquals("Calle Mayor 1", domain.address)
        assertEquals("Madrid", domain.province)
        assertEquals("Juan García", domain.contactPerson)
        assertEquals("600123456", domain.phone)
        assertEquals("test@example.com", domain.email)
        assertEquals(40.4168, domain.latitude!!, 0.001)
        assertEquals(-3.7038, domain.longitude!!, 0.001)
        assertEquals("Notas de prueba", domain.notes)
        assertEquals(1000L, domain.createdAt)
        assertEquals(2000L, domain.updatedAt)
        assertTrue(domain.isActive)
        assertFalse(domain.isDirty)
    }

    @Test
    fun `domain toEntity maps all fields correctly`() {
        val entity = testDomain.toEntity()

        assertEquals(1L, entity.id)
        assertEquals("Test Client", entity.name)
        assertEquals("B12345678", entity.cif)
        assertEquals("Calle Mayor 1", entity.address)
        assertEquals("Madrid", entity.province)
        assertEquals("Juan García", entity.contactPerson)
        assertEquals("600123456", entity.phone)
        assertEquals("test@example.com", entity.email)
        assertEquals(40.4168, entity.latitude!!, 0.001)
        assertEquals(-3.7038, entity.longitude!!, 0.001)
        assertEquals("Notas de prueba", entity.notes)
        assertEquals(1000L, entity.createdAt)
        assertEquals(2000L, entity.updatedAt)
        assertTrue(entity.isActive)
        assertFalse(entity.isDirty)
    }

    @Test
    fun `entity with null coordinates maps correctly`() {
        val entity = testEntity.copy(latitude = null, longitude = null)
        val domain = entity.toDomain()

        assertNull(domain.latitude)
        assertNull(domain.longitude)

        val roundTrip = domain.toEntity()
        assertNull(roundTrip.latitude)
        assertNull(roundTrip.longitude)
    }

    @Test
    fun `entity roundtrip preserves isDirty`() {
        val dirtyEntity = testEntity.copy(isDirty = true)
        val domain = dirtyEntity.toDomain()

        assertTrue(domain.isDirty)

        val roundTrip = domain.toEntity()
        assertTrue(roundTrip.isDirty)
    }

    @Test
    fun `inactive entity maps correctly`() {
        val entity = testEntity.copy(isActive = false)
        val domain = entity.toDomain()

        assertFalse(domain.isActive)

        val roundTrip = domain.toEntity()
        assertFalse(roundTrip.isActive)
    }
}
