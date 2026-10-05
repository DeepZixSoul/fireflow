package com.igrupos.domain.util

import com.igrupos.domain.model.ChecklistItem
import com.igrupos.domain.model.EntityStatus
import com.igrupos.domain.model.PressureMeasurement
import com.igrupos.domain.model.Revision

object StatusCalculator {

    fun revisionStatus(
        revision: Revision,
        enabledChecklistItems: List<ChecklistItem>,
        pressureMeasurement: PressureMeasurement?
    ): EntityStatus {
        val checklistResults = revision.checklistResults

        if (checklistResults.isEmpty()) return EntityStatus.RED

        if (pressureMeasurement == null ||
            (pressureMeasurement.pressureAt0 == null &&
                pressureMeasurement.pressureAt50 == null &&
                pressureMeasurement.pressureAt100 == null &&
                pressureMeasurement.pressureAt140 == null)
        ) {
            return EntityStatus.RED
        }

        val enabledLabels = enabledChecklistItems.map { it.label }.toSet()
        val resultLabels = checklistResults.keys

        if (!resultLabels.containsAll(enabledLabels)) return EntityStatus.RED

        val hasAnyFalse = checklistResults.values.any { !it }
        return if (hasAnyFalse) EntityStatus.YELLOW else EntityStatus.GREEN
    }

    fun simpleRevisionStatus(
        hasChecklist: Boolean,
        allChecklistTrue: Boolean,
        hasPressureData: Boolean
    ): EntityStatus {
        if (!hasChecklist || !hasPressureData) return EntityStatus.RED
        if (!allChecklistTrue) return EntityStatus.YELLOW
        return EntityStatus.GREEN
    }

    fun groupStatus(
        revisions: List<Revision>,
        enabledChecklistItems: List<ChecklistItem>,
        getMeasurementForRevision: (Revision) -> PressureMeasurement?
    ): EntityStatus {
        if (revisions.isEmpty()) return EntityStatus.YELLOW

        val latestRevision = revisions.maxByOrNull { it.date } ?: return EntityStatus.YELLOW

        return revisionStatus(latestRevision, enabledChecklistItems, getMeasurementForRevision(latestRevision))
    }

    fun simpleGroupStatus(groupStatuses: List<EntityStatus>): EntityStatus {
        if (groupStatuses.isEmpty()) return EntityStatus.GRAY
        if (groupStatuses.any { it == EntityStatus.RED }) return EntityStatus.RED
        if (groupStatuses.any { it == EntityStatus.GREEN }) return EntityStatus.GREEN
        return EntityStatus.YELLOW
    }

    fun clientStatus(groupsStatus: List<EntityStatus>): EntityStatus {
        if (groupsStatus.isEmpty()) return EntityStatus.GRAY

        if (groupsStatus.any { it == EntityStatus.RED }) return EntityStatus.RED
        if (groupsStatus.any { it == EntityStatus.YELLOW }) return EntityStatus.YELLOW
        return EntityStatus.GREEN
    }
}
