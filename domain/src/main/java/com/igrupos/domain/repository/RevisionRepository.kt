package com.igrupos.domain.repository

import com.igrupos.domain.model.Revision
import kotlinx.coroutines.flow.Flow

interface RevisionRepository {
    suspend fun getRevisionsByGroup(groupId: Long): Result<List<Revision>>
    suspend fun getRevisionsByGroupOneShot(groupId: Long): List<Revision>
    suspend fun getAllRevisions(): List<Revision>
    suspend fun getRevisionById(id: Long): Result<Revision>
    suspend fun getLastRevisionByGroup(groupId: Long): Result<Revision?>
    suspend fun saveRevision(revision: Revision): Result<Revision>
    suspend fun deleteRevision(id: Long): Result<Unit>
    suspend fun getDirtyRevisions(): List<Revision>
    suspend fun markRevisionsClean(ids: List<Long>)
    fun getRevisionsByGroupFlow(groupId: Long): Flow<List<Revision>>
    fun getAllRevisionsFlow(): Flow<List<Revision>>
}
