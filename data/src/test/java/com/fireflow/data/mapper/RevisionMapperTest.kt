package com.fireflow.data.mapper

import com.fireflow.database.entity.RevisionEntity
import com.fireflow.domain.model.Revision
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class RevisionMapperTest {

    private val testEntity = RevisionEntity(
        id = 3L,
        groupId = 2L,
        date = 1700000000000L,
        technicianName = "Técnico Test",
        notes = "Revisión de prueba",
        checklistResults = """{"fuga":true,"ruido":false}""",
        createdAt = 1000L,
        updatedAt = 2000L,
        isDirty = false
    )

    private val testDomain = Revision(
        id = 3L,
        groupId = 2L,
        date = 1700000000000L,
        technicianName = "Técnico Test",
        notes = "Revisión de prueba",
        checklistResults = mapOf("fuga" to true, "ruido" to false),
        createdAt = 1000L,
        updatedAt = 2000L,
        isDirty = false
    )

    @Test
    fun `entity toDomain parses checklist JSON`() {
        val domain = testEntity.toDomain()

        assertEquals(3L, domain.id)
        assertEquals(2L, domain.groupId)
        assertEquals(1700000000000L, domain.date)
        assertEquals("Técnico Test", domain.technicianName)
        assertEquals("Revisión de prueba", domain.notes)
        assertEquals(2, domain.checklistResults.size)
        assertTrue(domain.checklistResults["fuga"]!!)
        assertFalse(domain.checklistResults["ruido"]!!)
        assertEquals(1000L, domain.createdAt)
        assertEquals(2000L, domain.updatedAt)
        assertFalse(domain.isDirty)
    }

    @Test
    fun `domain toEntity serializes checklist to JSON`() {
        val entity = testDomain.toEntity()

        assertEquals(3L, entity.id)
        assertEquals(2L, entity.groupId)
        assertEquals(1700000000000L, entity.date)
        assertEquals("Técnico Test", entity.technicianName)
        assertEquals("Revisión de prueba", entity.notes)
        assertTrue(entity.checklistResults.contains("fuga"))
        assertTrue(entity.checklistResults.contains("ruido"))
        assertEquals(1000L, entity.createdAt)
        assertEquals(2000L, entity.updatedAt)
        assertFalse(entity.isDirty)
    }

    @Test
    fun `empty checklist JSON produces empty map`() {
        val entity = testEntity.copy(checklistResults = "{}")
        val domain = entity.toDomain()

        assertTrue(domain.checklistResults.isEmpty())
    }

    @Test
    fun `invalid JSON produces empty map`() {
        val entity = testEntity.copy(checklistResults = "not-json")
        val domain = entity.toDomain()

        assertTrue(domain.checklistResults.isEmpty())
    }

    @Test
    fun `roundtrip preserves all values`() {
        val roundTrip = testDomain.toEntity().toDomain()

        assertEquals(testDomain.id, roundTrip.id)
        assertEquals(testDomain.groupId, roundTrip.groupId)
        assertEquals(testDomain.date, roundTrip.date)
        assertEquals(testDomain.technicianName, roundTrip.technicianName)
        assertEquals(testDomain.notes, roundTrip.notes)
        assertEquals(testDomain.checklistResults, roundTrip.checklistResults)
        assertEquals(testDomain.createdAt, roundTrip.createdAt)
        assertEquals(testDomain.updatedAt, roundTrip.updatedAt)
        assertEquals(testDomain.isDirty, roundTrip.isDirty)
    }

    @Test
    fun `roundtrip preserves dirty flag`() {
        val dirtyDomain = testDomain.copy(isDirty = true)
        val roundTrip = dirtyDomain.toEntity().toDomain()

        assertTrue(roundTrip.isDirty)
    }

    @Test
    fun `large checklist roundtrips correctly`() {
        val largeChecklist = mapOf(
            "check1" to true, "check2" to false, "check3" to true,
            "check4" to true, "check5" to false
        )
        val domain = testDomain.copy(checklistResults = largeChecklist)
        val roundTrip = domain.toEntity().toDomain()

        assertEquals(5, roundTrip.checklistResults.size)
        assertEquals(largeChecklist, roundTrip.checklistResults)
    }
}
