package com.igrupos.data.mapper

import com.igrupos.database.entity.PressureGroupEntity
import com.igrupos.domain.model.PressureGroup
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class PressureGroupMapperTest {

    private val testEntity = PressureGroupEntity(
        id = 2L,
        clientId = 1L,
        brand = "Grundfos",
        model = "CR 32-4",
        serialNumber = "SN123456",
        pumpNumber = "P001",
        manufacturer = "Grundfos",
        power = "5.5",
        installationDate = 1000L,
        maintenanceDate = 2000L,
        createdAt = 3000L,
        updatedAt = 4000L,
        isActive = true,
        isDirty = false
    )

    private val testDomain = PressureGroup(
        id = 2L,
        clientId = 1L,
        brand = "Grundfos",
        model = "CR 32-4",
        serialNumber = "SN123456",
        pumpNumber = "P001",
        manufacturer = "Grundfos",
        power = "5.5",
        installationDate = 1000L,
        maintenanceDate = 2000L,
        createdAt = 3000L,
        updatedAt = 4000L,
        isActive = true,
        isDirty = false
    )

    @Test
    fun `entity toDomain maps all fields`() {
        val domain = testEntity.toDomain()

        assertEquals(2L, domain.id)
        assertEquals(1L, domain.clientId)
        assertEquals("Grundfos", domain.brand)
        assertEquals("CR 32-4", domain.model)
        assertEquals("SN123456", domain.serialNumber)
        assertEquals("P001", domain.pumpNumber)
        assertEquals("Grundfos", domain.manufacturer)
        assertEquals("5.5", domain.power)
        assertEquals(1000L, domain.installationDate)
        assertEquals(2000L, domain.maintenanceDate)
        assertEquals(3000L, domain.createdAt)
        assertEquals(4000L, domain.updatedAt)
        assertTrue(domain.isActive)
        assertFalse(domain.isDirty)
    }

    @Test
    fun `domain toEntity maps all fields`() {
        val entity = testDomain.toEntity()

        assertEquals(2L, entity.id)
        assertEquals(1L, entity.clientId)
        assertEquals("Grundfos", entity.brand)
        assertEquals("CR 32-4", entity.model)
        assertEquals("SN123456", entity.serialNumber)
        assertEquals("P001", entity.pumpNumber)
        assertEquals("Grundfos", entity.manufacturer)
        assertEquals("5.5", entity.power)
        assertEquals(1000L, entity.installationDate)
        assertEquals(2000L, entity.maintenanceDate)
        assertEquals(3000L, entity.createdAt)
        assertEquals(4000L, entity.updatedAt)
        assertTrue(entity.isActive)
        assertFalse(entity.isDirty)
    }

    @Test
    fun `null dates map correctly`() {
        val entity = testEntity.copy(installationDate = null, maintenanceDate = null)
        val domain = entity.toDomain()

        assertNull(domain.installationDate)
        assertNull(domain.maintenanceDate)

        val roundTrip = domain.toEntity()
        assertNull(roundTrip.installationDate)
        assertNull(roundTrip.maintenanceDate)
    }

    @Test
    fun `roundtrip preserves dirty flag`() {
        val dirtyDomain = testDomain.copy(isDirty = true)
        val entity = dirtyDomain.toEntity()

        assertTrue(entity.isDirty)
        assertEquals(dirtyDomain, entity.toDomain())
    }

    @Test
    fun `inactive group maps correctly`() {
        val entity = testEntity.copy(isActive = false)
        val domain = entity.toDomain()

        assertFalse(domain.isActive)

        val roundTrip = domain.toEntity()
        assertFalse(roundTrip.isActive)
    }
}
