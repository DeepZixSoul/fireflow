package com.fireflow.server.services

import com.fireflow.server.models.Client
import com.fireflow.server.models.PressureGroup
import com.fireflow.server.models.Revision
import com.fireflow.server.models.CurvePoint
import com.fireflow.server.models.request.SyncRequest
import com.fireflow.server.models.response.SyncResponse
import com.fireflow.server.repositories.ClientRepository
import com.fireflow.server.repositories.PressureGroupRepository
import com.fireflow.server.repositories.RevisionRepository
import com.fireflow.server.repositories.CurveRepository
import org.slf4j.LoggerFactory

interface SyncRepository<T> {
    fun upsert(item: T)
    fun findAll(): List<T>
    fun findModifiedSince(timestamp: Long): List<T>
}

class SyncService(
    private val clientRepository: ClientRepository,
    private val groupRepository: PressureGroupRepository,
    private val revisionRepository: RevisionRepository,
    private val curveRepository: CurveRepository
) {
    private val logger = LoggerFactory.getLogger(SyncService::class.java)

    private val clientSyncRepo = object : SyncRepository<Client> {
        override fun upsert(item: Client) = clientRepository.upsert(item)
        override fun findAll() = clientRepository.findAll()
        override fun findModifiedSince(timestamp: Long) = clientRepository.findModifiedSince(timestamp)
    }

    private val groupSyncRepo = object : SyncRepository<PressureGroup> {
        override fun upsert(item: PressureGroup) = groupRepository.upsert(item)
        override fun findAll() = groupRepository.findAll()
        override fun findModifiedSince(timestamp: Long) = groupRepository.findModifiedSince(timestamp)
    }

    private val revisionSyncRepo = object : SyncRepository<Revision> {
        override fun upsert(item: Revision) = revisionRepository.upsert(item)
        override fun findAll() = revisionRepository.findAll()
        override fun findModifiedSince(timestamp: Long) = revisionRepository.findModifiedSince(timestamp)
    }

    private val curveSyncRepo = object : SyncRepository<CurvePoint> {
        override fun upsert(item: CurvePoint) = curveRepository.upsert(item)
        override fun findAll() = curveRepository.findAll()
        override fun findModifiedSince(timestamp: Long) = curveRepository.findModifiedSince(timestamp)
    }

    fun <T> sync(repository: SyncRepository<T>, request: SyncRequest<T>, entityName: String): SyncResponse<T> {
        logger.info("Syncing ${request.data.size} $entityName since ${request.lastSyncTimestamp}")

        for (item in request.data) {
            repository.upsert(item)
        }

        val serverData = if (request.lastSyncTimestamp > 0) {
            repository.findModifiedSince(request.lastSyncTimestamp)
        } else {
            repository.findAll()
        }

        val timestamp = System.currentTimeMillis()
        logger.info("$entityName sync complete: ${serverData.size} records returned")
        return SyncResponse(data = serverData, syncTimestamp = timestamp)
    }

    fun syncClients(request: SyncRequest<Client>): SyncResponse<Client> =
        sync(clientSyncRepo, request, "clients")

    fun syncGroups(request: SyncRequest<PressureGroup>): SyncResponse<PressureGroup> =
        sync(groupSyncRepo, request, "groups")

    fun syncRevisions(request: SyncRequest<Revision>): SyncResponse<Revision> =
        sync(revisionSyncRepo, request, "revisions")

    fun syncCurves(request: SyncRequest<CurvePoint>): SyncResponse<CurvePoint> =
        sync(curveSyncRepo, request, "curves")
}
