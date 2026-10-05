package com.fireflow.domain.util

import com.fireflow.domain.model.ChecklistItem
import com.fireflow.domain.model.EntityStatus
import com.fireflow.domain.model.PressureMeasurement
import com.fireflow.domain.model.Revision
import org.junit.Assert.assertEquals
import org.junit.Test

class StatusCalculatorTest {

    private val defaultChecklistItems = listOf(
        ChecklistItem(id = 1, label = "Item 1", isEnabled = true, orderIndex = 0),
        ChecklistItem(id = 2, label = "Item 2", isEnabled = true, orderIndex = 1),
        ChecklistItem(id = 3, label = "Item 3", isEnabled = true, orderIndex = 2)
    )

    private fun createRevision(
        checklistResults: Map<String, Boolean> = emptyMap()
    ) = Revision(
        id = 1L,
        groupId = 1L,
        date = System.currentTimeMillis(),
        technicianName = "Test Tech",
        notes = "",
        checklistResults = checklistResults,
        createdAt = System.currentTimeMillis(),
        updatedAt = System.currentTimeMillis()
    )

    private fun createMeasurement(
        pressureAt0: Double? = null,
        pressureAt50: Double? = null,
        pressureAt100: Double? = null,
        pressureAt140: Double? = null
    ) = PressureMeasurement(
        id = 1L,
        motorId = 1L,
        year = 2024,
        pressureAt0 = pressureAt0,
        pressureAt50 = pressureAt50,
        pressureAt100 = pressureAt100,
        pressureAt140 = pressureAt140,
        createdAt = System.currentTimeMillis(),
        updatedAt = System.currentTimeMillis()
    )

    // revisionStatus tests

    @Test
    fun `revisionStatus - empty checklist returns RED`() {
        val revision = createRevision(checklistResults = emptyMap())
        val measurement = createMeasurement(pressureAt0 = 1.0)

        val status = StatusCalculator.revisionStatus(revision, defaultChecklistItems, measurement)
        assertEquals(EntityStatus.RED, status)
    }

    @Test
    fun `revisionStatus - null measurement returns RED`() {
        val revision = createRevision(
            checklistResults = mapOf("Item 1" to true, "Item 2" to true, "Item 3" to true)
        )

        val status = StatusCalculator.revisionStatus(revision, defaultChecklistItems, null)
        assertEquals(EntityStatus.RED, status)
    }

    @Test
    fun `revisionStatus - measurement with no pressures returns RED`() {
        val revision = createRevision(
            checklistResults = mapOf("Item 1" to true, "Item 2" to true, "Item 3" to true)
        )
        val measurement = createMeasurement()

        val status = StatusCalculator.revisionStatus(revision, defaultChecklistItems, measurement)
        assertEquals(EntityStatus.RED, status)
    }

    @Test
    fun `revisionStatus - missing enabled checklist item returns RED`() {
        val revision = createRevision(
            checklistResults = mapOf("Item 1" to true, "Item 2" to true)
        )
        val measurement = createMeasurement(pressureAt0 = 1.0)

        val status = StatusCalculator.revisionStatus(revision, defaultChecklistItems, measurement)
        assertEquals(EntityStatus.RED, status)
    }

    @Test
    fun `revisionStatus - all checklist true + pressures filled returns GREEN`() {
        val revision = createRevision(
            checklistResults = mapOf("Item 1" to true, "Item 2" to true, "Item 3" to true)
        )
        val measurement = createMeasurement(pressureAt0 = 1.0, pressureAt50 = 2.0)

        val status = StatusCalculator.revisionStatus(revision, defaultChecklistItems, measurement)
        assertEquals(EntityStatus.GREEN, status)
    }

    @Test
    fun `revisionStatus - checklist with false returns YELLOW`() {
        val revision = createRevision(
            checklistResults = mapOf("Item 1" to true, "Item 2" to false, "Item 3" to true)
        )
        val measurement = createMeasurement(pressureAt0 = 1.0)

        val status = StatusCalculator.revisionStatus(revision, defaultChecklistItems, measurement)
        assertEquals(EntityStatus.YELLOW, status)
    }

    @Test
    fun `revisionStatus - only one pressure value is enough`() {
        val revision = createRevision(
            checklistResults = mapOf("Item 1" to true, "Item 2" to true, "Item 3" to true)
        )
        val measurement = createMeasurement(pressureAt140 = 3.0)

        val status = StatusCalculator.revisionStatus(revision, defaultChecklistItems, measurement)
        assertEquals(EntityStatus.GREEN, status)
    }

    @Test
    fun `revisionStatus - extra false result returns YELLOW`() {
        val revision = createRevision(
            checklistResults = mapOf(
                "Item 1" to true,
                "Item 2" to true,
                "Item 3" to true,
                "Extra Item" to false
            )
        )
        val measurement = createMeasurement(pressureAt0 = 1.0)

        val status = StatusCalculator.revisionStatus(revision, defaultChecklistItems, measurement)
        assertEquals(EntityStatus.YELLOW, status)
    }

    // groupStatus tests

    @Test
    fun `groupStatus - empty revisions returns YELLOW`() {
        val status = StatusCalculator.groupStatus(
            revisions = emptyList(),
            enabledChecklistItems = defaultChecklistItems,
            getMeasurementForRevision = { null }
        )
        assertEquals(EntityStatus.YELLOW, status)
    }

    @Test
    fun `groupStatus - uses latest revision only`() {
        val oldRevision = createRevision(
            checklistResults = mapOf("Item 1" to true, "Item 2" to true, "Item 3" to true)
        ).copy(id = 1L, date = 1000L)

        val latestRevision = createRevision(
            checklistResults = mapOf("Item 1" to true, "Item 2" to false, "Item 3" to true)
        ).copy(id = 2L, date = 2000L)

        val status = StatusCalculator.groupStatus(
            revisions = listOf(oldRevision, latestRevision),
            enabledChecklistItems = defaultChecklistItems,
            getMeasurementForRevision = { createMeasurement(pressureAt0 = 1.0) }
        )
        assertEquals(EntityStatus.YELLOW, status)
    }

    @Test
    fun `groupStatus - latest revision all true returns GREEN`() {
        val revision = createRevision(
            checklistResults = mapOf("Item 1" to true, "Item 2" to true, "Item 3" to true)
        ).copy(date = 2000L)

        val status = StatusCalculator.groupStatus(
            revisions = listOf(revision),
            enabledChecklistItems = defaultChecklistItems,
            getMeasurementForRevision = { createMeasurement(pressureAt0 = 1.0) }
        )
        assertEquals(EntityStatus.GREEN, status)
    }

    // clientStatus tests

    @Test
    fun `clientStatus - empty groups returns GRAY`() {
        val status = StatusCalculator.clientStatus(emptyList())
        assertEquals(EntityStatus.GRAY, status)
    }

    @Test
    fun `clientStatus - all green returns GREEN`() {
        val status = StatusCalculator.clientStatus(
            listOf(EntityStatus.GREEN, EntityStatus.GREEN, EntityStatus.GREEN)
        )
        assertEquals(EntityStatus.GREEN, status)
    }

    @Test
    fun `clientStatus - any RED returns RED`() {
        val status = StatusCalculator.clientStatus(
            listOf(EntityStatus.GREEN, EntityStatus.RED, EntityStatus.GREEN)
        )
        assertEquals(EntityStatus.RED, status)
    }

    @Test
    fun `clientStatus - any YELLOW returns YELLOW (no RED)`() {
        val status = StatusCalculator.clientStatus(
            listOf(EntityStatus.GREEN, EntityStatus.YELLOW, EntityStatus.GREEN)
        )
        assertEquals(EntityStatus.YELLOW, status)
    }

    @Test
    fun `clientStatus - mix of RED and YELLOW returns RED`() {
        val status = StatusCalculator.clientStatus(
            listOf(EntityStatus.YELLOW, EntityStatus.RED)
        )
        assertEquals(EntityStatus.RED, status)
    }
}
