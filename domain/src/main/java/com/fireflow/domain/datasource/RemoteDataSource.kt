package com.fireflow.domain.datasource

import com.fireflow.domain.model.Client
import com.fireflow.domain.model.CurvePoint
import com.fireflow.domain.model.PressureGroup
import com.fireflow.domain.model.Revision
import com.fireflow.domain.model.SyncResult

interface RemoteDataSource {
    suspend fun syncClients(clients: List<Client>, lastSync: Long): Result<SyncResult<Client>>
    suspend fun syncGroups(groups: List<PressureGroup>, lastSync: Long): Result<SyncResult<PressureGroup>>
    suspend fun syncRevisions(revisions: List<Revision>, lastSync: Long): Result<SyncResult<Revision>>
    suspend fun syncCurves(curves: List<CurvePoint>, lastSync: Long): Result<SyncResult<CurvePoint>>
}
