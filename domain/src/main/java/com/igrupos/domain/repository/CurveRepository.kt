package com.igrupos.domain.repository

import com.igrupos.domain.model.CurvePoint
import kotlinx.coroutines.flow.Flow

interface CurveRepository {
    suspend fun getCurvePointsByRevision(revisionId: Long): Result<List<CurvePoint>>
    suspend fun getCurvePointsByRevisionAndMotor(revisionId: Long, motorId: Long): Result<List<CurvePoint>>
    suspend fun getAllCurvePoints(): List<CurvePoint>
    suspend fun saveCurvePoint(point: CurvePoint): Result<CurvePoint>
    suspend fun saveCurvePoints(points: List<CurvePoint>): Result<List<CurvePoint>>
    suspend fun deleteCurvePointsByRevision(revisionId: Long): Result<Unit>
    suspend fun deleteCurvePointsByRevisionAndMotor(revisionId: Long, motorId: Long): Result<Unit>
    suspend fun getDirtyCurvePoints(): List<CurvePoint>
    suspend fun markCurvePointsClean(ids: List<Long>)
    fun getCurvePointsByRevisionFlow(revisionId: Long): Flow<List<CurvePoint>>
    fun getCurvePointsByRevisionAndMotorFlow(revisionId: Long, motorId: Long): Flow<List<CurvePoint>>
}
