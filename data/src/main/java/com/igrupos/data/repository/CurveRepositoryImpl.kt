package com.igrupos.data.repository

import com.igrupos.data.mapper.toDomain
import com.igrupos.data.mapper.toEntity
import com.igrupos.database.dao.CurvePointDao
import com.igrupos.domain.model.CurvePoint
import com.igrupos.domain.repository.CurveRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CurveRepositoryImpl @Inject constructor(
    private val curvePointDao: CurvePointDao
) : CurveRepository {

    override fun getCurvePointsByRevisionFlow(revisionId: Long): Flow<List<CurvePoint>> {
        return curvePointDao.getByRevision(revisionId).map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override fun getCurvePointsByRevisionAndMotorFlow(revisionId: Long, motorId: Long): Flow<List<CurvePoint>> {
        return curvePointDao.getByRevisionAndMotor(revisionId, motorId).map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override suspend fun getAllCurvePoints(): List<CurvePoint> {
        return curvePointDao.getAll().first().map { it.toDomain() }
    }

    override suspend fun getCurvePointsByRevision(revisionId: Long): Result<List<CurvePoint>> {
        return try {
            val points = curvePointDao.getByRevision(revisionId).first().map { it.toDomain() }
            Result.success(points)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getCurvePointsByRevisionAndMotor(revisionId: Long, motorId: Long): Result<List<CurvePoint>> {
        return try {
            val points = curvePointDao.getByRevisionAndMotor(revisionId, motorId).first().map { it.toDomain() }
            Result.success(points)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun saveCurvePoint(point: CurvePoint): Result<CurvePoint> {
        return try {
            val entity = point.copy(isDirty = true).toEntity()
            val id = curvePointDao.insert(entity)
            Result.success(point.copy(id = id, isDirty = true))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun saveCurvePoints(points: List<CurvePoint>): Result<List<CurvePoint>> {
        return try {
            val entities = points.map { it.copy(isDirty = true).toEntity() }
            val ids = curvePointDao.insertAll(entities)
            Result.success(points.mapIndexed { index, point -> point.copy(id = ids.getOrElse(index) { 0L }, isDirty = true) })
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun deleteCurvePointsByRevision(revisionId: Long): Result<Unit> {
        return try {
            curvePointDao.deleteByRevision(revisionId)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun deleteCurvePointsByRevisionAndMotor(revisionId: Long, motorId: Long): Result<Unit> {
        return try {
            curvePointDao.deleteByRevisionAndMotor(revisionId, motorId)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getDirtyCurvePoints(): List<CurvePoint> {
        return curvePointDao.getDirty().map { it.toDomain() }
    }

    override suspend fun markCurvePointsClean(ids: List<Long>) {
        curvePointDao.markClean(ids)
    }
}
