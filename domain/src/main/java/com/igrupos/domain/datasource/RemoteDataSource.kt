package com.igrupos.domain.datasource

import com.igrupos.domain.model.Client
import com.igrupos.domain.model.CurvePoint
import com.igrupos.domain.model.PressureGroup
import com.igrupos.domain.model.Revision
import com.igrupos.domain.model.SyncResult

interface RemoteDataSource {
    suspend fun syncClients(clients: List<Client>, lastSync: Long): Result<SyncResult<Client>>
    suspend fun syncGroups(groups: List<PressureGroup>, lastSync: Long): Result<SyncResult<PressureGroup>>
    suspend fun syncRevisions(revisions: List<Revision>, lastSync: Long): Result<SyncResult<Revision>>
    suspend fun syncCurves(curves: List<CurvePoint>, lastSync: Long): Result<SyncResult<CurvePoint>>
}
