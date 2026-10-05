package com.fireflow.data.sync

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.fireflow.domain.datasource.RemoteDataSource
import com.fireflow.domain.model.Client
import com.fireflow.domain.model.CurvePoint
import com.fireflow.domain.model.PressureGroup
import com.fireflow.domain.model.Revision
import com.fireflow.domain.repository.ClientRepository
import com.fireflow.domain.repository.CurveRepository
import com.fireflow.domain.repository.PressureGroupRepository
import com.fireflow.domain.repository.RevisionRepository
import com.fireflow.domain.repository.SyncManagerRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val Context.syncDataStore by preferencesDataStore(name = "sync_state")

@Singleton
class SyncManager @Inject constructor(
    @ApplicationContext private val context: Context,
    private val remoteDataSource: RemoteDataSource,
    private val networkMonitor: NetworkMonitor,
    private val serverConfig: ServerConfig,
    private val clientRepository: ClientRepository,
    private val groupRepository: PressureGroupRepository,
    private val revisionRepository: RevisionRepository,
    private val curveRepository: CurveRepository
) : SyncManagerRepository {
    private object Keys {
        val LAST_SYNC = longPreferencesKey("last_sync_timestamp")
        val SYNC_ENABLED = booleanPreferencesKey("sync_enabled")
    }

    override val lastSyncTime: Flow<Long> = context.syncDataStore.data.map { it[Keys.LAST_SYNC] ?: 0L }

    override val isSyncEnabled: Flow<Boolean> = context.syncDataStore.data.map { it[Keys.SYNC_ENABLED] ?: true }

    override suspend fun getServerUrl(): String = serverConfig.getServerUrl()

    override suspend fun getAuthToken(): String = serverConfig.getAuthToken()

    override suspend fun saveServerConfig(url: String, token: String) {
        serverConfig.saveConfig(url, token)
    }

    override suspend fun setSyncEnabled(enabled: Boolean) {
        context.syncDataStore.edit { it[Keys.SYNC_ENABLED] = enabled }
    }

    override suspend fun performSync(): Result<Unit> {
        val enabled = context.syncDataStore.data.first()[Keys.SYNC_ENABLED] ?: true
        if (!enabled) {
            return Result.failure(Exception("Sincronización desactivada"))
        }

        if (!networkMonitor.isOnline.first()) {
            return Result.failure(Exception("Sin conexión a internet"))
        }

        val baseUrl = serverConfig.getServerUrl()
        if (baseUrl.isBlank()) {
            return Result.failure(Exception("Servidor no configurado"))
        }

        return try {
            val lastSync = context.syncDataStore.data.first()[Keys.LAST_SYNC] ?: 0L
            val timestamp = System.currentTimeMillis()

            coroutineScope {
                val clientsDeferred = async { syncClients(lastSync) }
                val groupsDeferred = async { syncGroups(lastSync) }
                val revisionsDeferred = async { syncRevisions(lastSync) }
                val curvesDeferred = async { syncCurves(lastSync) }

                clientsDeferred.await()
                groupsDeferred.await()
                revisionsDeferred.await()
                curvesDeferred.await()
            }

            context.syncDataStore.edit { it[Keys.LAST_SYNC] = timestamp }

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private suspend fun syncClients(lastSync: Long) {
        val dirtyClients = clientRepository.getDirtyClients()
        if (dirtyClients.isEmpty()) return

        val result = remoteDataSource.syncClients(dirtyClients, lastSync)
        result.getOrNull()?.let { syncResult ->
            syncResult.data.forEach { remoteClient ->
                val localClient = clientRepository.getClientById(remoteClient.id).getOrNull()
                val resolved = resolveClientConflict(localClient, remoteClient)
                clientRepository.saveClient(resolved)
            }
            clientRepository.markClientsClean(dirtyClients.map { it.id })
        }
    }

    private suspend fun syncGroups(lastSync: Long) {
        val dirtyGroups = groupRepository.getDirtyGroups()
        if (dirtyGroups.isEmpty()) return

        val result = remoteDataSource.syncGroups(dirtyGroups, lastSync)
        result.getOrNull()?.let { syncResult ->
            syncResult.data.forEach { remoteGroup ->
                val localGroup = groupRepository.getGroupById(remoteGroup.id).getOrNull()
                val resolved = resolveGroupConflict(localGroup, remoteGroup)
                groupRepository.saveGroup(resolved)
            }
            groupRepository.markGroupsClean(dirtyGroups.map { it.id })
        }
    }

    private suspend fun syncRevisions(lastSync: Long) {
        val dirtyRevisions = revisionRepository.getDirtyRevisions()
        if (dirtyRevisions.isEmpty()) return

        val result = remoteDataSource.syncRevisions(dirtyRevisions, lastSync)
        result.getOrNull()?.let { syncResult ->
            syncResult.data.forEach { remoteRevision ->
                val localRevision = revisionRepository.getRevisionById(remoteRevision.id).getOrNull()
                val resolved = resolveRevisionConflict(localRevision, remoteRevision)
                revisionRepository.saveRevision(resolved)
            }
            revisionRepository.markRevisionsClean(dirtyRevisions.map { it.id })
        }
    }

    private suspend fun syncCurves(lastSync: Long) {
        val dirtyCurves = curveRepository.getDirtyCurvePoints()
        if (dirtyCurves.isEmpty()) return

        val result = remoteDataSource.syncCurves(dirtyCurves, lastSync)
        result.getOrNull()?.let { syncResult ->
            syncResult.data.forEach { remoteCurve ->
                curveRepository.saveCurvePoint(remoteCurve.copy(isDirty = false))
            }
            curveRepository.markCurvePointsClean(dirtyCurves.map { it.id })
        }
    }

    private fun resolveClientConflict(local: Client?, remote: Client): Client {
        if (local == null) return remote
        return if (remote.updatedAt >= local.updatedAt) remote else local
    }

    private fun resolveGroupConflict(local: PressureGroup?, remote: PressureGroup): PressureGroup {
        if (local == null) return remote
        return if (remote.updatedAt >= local.updatedAt) remote else local
    }

    private fun resolveRevisionConflict(local: Revision?, remote: Revision): Revision {
        if (local == null) return remote
        return if (remote.updatedAt >= local.updatedAt) remote else local
    }
}
