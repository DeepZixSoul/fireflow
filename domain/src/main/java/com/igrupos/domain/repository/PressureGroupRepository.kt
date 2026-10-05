package com.igrupos.domain.repository

import com.igrupos.domain.model.PressureGroup
import kotlinx.coroutines.flow.Flow

interface PressureGroupRepository {
    suspend fun getGroupsByClient(clientId: Long): Result<List<PressureGroup>>
    suspend fun getAllGroups(): List<PressureGroup>
    suspend fun getGroupById(id: Long): Result<PressureGroup>
    suspend fun saveGroup(group: PressureGroup): Result<PressureGroup>
    suspend fun deleteGroup(id: Long): Result<Unit>
    suspend fun getDirtyGroups(): List<PressureGroup>
    suspend fun markGroupsClean(ids: List<Long>)
    fun getGroupsByClientFlow(clientId: Long): Flow<List<PressureGroup>>
}
