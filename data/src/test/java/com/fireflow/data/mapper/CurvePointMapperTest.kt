package com.fireflow.data.mapper

import com.fireflow.database.entity.CurvePointEntity
import com.fireflow.domain.model.CurvePoint
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class CurvePointMapperTest {

    private val testEntity = CurvePointEntity(
        id = 4L,
        revisionId = 3L,
        motorId = 100L,
        flow = 12.5,
        pressure = 4.5,
        orderIndex = 0,
        isDirty = false
    )

    private val testDomain = CurvePoint(
        id = 4L,
        revisionId = 3L,
        motorId = 100L,
        flow = 12.5,
        pressure = 4.5,
        orderIndex = 0,
        isDirty = false
    )

    @Test
    fun `entity toDomain maps all fields`() {
        val domain = testEntity.toDomain()

        assertEquals(4L, domain.id)
        assertEquals(3L, domain.revisionId)
        assertEquals(100L, domain.motorId)
        assertEquals(12.5, domain.flow, 0.001)
        assertEquals(4.5, domain.pressure, 0.001)
        assertEquals(0, domain.orderIndex)
        assertFalse(domain.isDirty)
    }

    @Test
    fun `domain toEntity maps all fields`() {
        val entity = testDomain.toEntity()

        assertEquals(4L, entity.id)
        assertEquals(3L, entity.revisionId)
        assertEquals(100L, entity.motorId)
        assertEquals(12.5, entity.flow, 0.001)
        assertEquals(4.5, entity.pressure, 0.001)
        assertEquals(0, entity.orderIndex)
        assertFalse(entity.isDirty)
    }

    @Test
    fun `roundtrip preserves all values`() {
        val roundTrip = testDomain.toEntity().toDomain()

        assertEquals(testDomain.id, roundTrip.id)
        assertEquals(testDomain.revisionId, roundTrip.revisionId)
        assertEquals(testDomain.motorId, roundTrip.motorId)
        assertEquals(testDomain.flow, roundTrip.flow, 0.001)
        assertEquals(testDomain.pressure, roundTrip.pressure, 0.001)
        assertEquals(testDomain.orderIndex, roundTrip.orderIndex)
        assertEquals(testDomain.isDirty, roundTrip.isDirty)
    }

    @Test
    fun `roundtrip preserves dirty flag`() {
        val dirtyDomain = testDomain.copy(isDirty = true)
        val roundTrip = dirtyDomain.toEntity().toDomain()

        assertTrue(roundTrip.isDirty)
    }

    @Test
    fun `zero values map correctly`() {
        val entity = testEntity.copy(flow = 0.0, pressure = 0.0, orderIndex = 0)
        val domain = entity.toDomain()

        assertEquals(0.0, domain.flow, 0.001)
        assertEquals(0.0, domain.pressure, 0.001)
        assertEquals(0, domain.orderIndex)
    }

    @Test
    fun `large values map correctly`() {
        val entity = testEntity.copy(flow = 9999.99, pressure = 999.99)
        val domain = entity.toDomain()

        assertEquals(9999.99, domain.flow, 0.001)
        assertEquals(999.99, domain.pressure, 0.001)
    }
}
