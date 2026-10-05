package com.fireflow.domain.repository

import com.fireflow.domain.model.Client
import kotlinx.coroutines.flow.Flow

interface ClientRepository {
    suspend fun getClients(): Result<List<Client>>
    suspend fun getAllClients(): List<Client>
    suspend fun getClientById(id: Long): Result<Client>
    suspend fun searchClients(query: String): Result<List<Client>>
    suspend fun saveClient(client: Client): Result<Client>
    suspend fun deleteClient(id: Long): Result<Unit>
    suspend fun getDirtyClients(): List<Client>
    suspend fun markClientsClean(ids: List<Long>)
    fun getClientsFlow(): Flow<List<Client>>
    fun searchClientsFlow(query: String): Flow<List<Client>>
}
