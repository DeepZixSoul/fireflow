package com.fireflow.data.repository

import com.fireflow.data.mapper.toDomain
import com.fireflow.data.mapper.toEntity
import com.fireflow.database.dao.ClientDao
import com.fireflow.domain.model.Client
import com.fireflow.domain.repository.ClientRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ClientRepositoryImpl @Inject constructor(
    private val clientDao: ClientDao
) : ClientRepository {

    override suspend fun getAllClients(): List<Client> {
        return clientDao.getAllActive().first().map { it.toDomain() }
    }

    override suspend fun getClients(): Result<List<Client>> {
        return try {
            val clients = clientDao.getAllActive().map { entities ->
                entities.map { it.toDomain() }
            }.first()
            Result.success(clients)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override fun getClientsFlow(): Flow<List<Client>> {
        return clientDao.getAllActive().map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override fun searchClientsFlow(query: String): Flow<List<Client>> {
        return clientDao.search(query).map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override suspend fun getClientById(id: Long): Result<Client> {
        return try {
            val entity = clientDao.getById(id)
                ?: return Result.failure(Exception("Cliente no encontrado"))
            Result.success(entity.toDomain())
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun searchClients(query: String): Result<List<Client>> {
        return try {
            val clients = clientDao.search(query).map { entities ->
                entities.map { it.toDomain() }
            }.first()
            Result.success(clients)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun saveClient(client: Client): Result<Client> {
        return try {
            val entity = client.copy(isDirty = true).toEntity()
            val id = clientDao.insert(entity)
            Result.success(client.copy(id = id, isDirty = true))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun deleteClient(id: Long): Result<Unit> {
        return try {
            clientDao.softDelete(id)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getDirtyClients(): List<Client> {
        return clientDao.getDirty().map { it.toDomain() }
    }

    override suspend fun markClientsClean(ids: List<Long>) {
        clientDao.markClean(ids)
    }
}
