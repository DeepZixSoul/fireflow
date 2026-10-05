package com.fireflow.data.datasource

import com.fireflow.data.sync.ServerConfig
import com.fireflow.domain.model.Client
import com.fireflow.domain.model.CurvePoint
import com.fireflow.domain.model.PressureGroup
import com.fireflow.domain.model.Revision
import com.fireflow.domain.model.SyncResult
import com.fireflow.domain.datasource.RemoteDataSource
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.header
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType
import kotlinx.serialization.Serializable
import javax.inject.Inject
import javax.inject.Singleton

@Serializable
private data class SyncPayload<T>(
    val data: List<T>,
    val lastSyncTimestamp: Long
)

@Singleton
class RemoteDataSourceImpl @Inject constructor(
    private val client: HttpClient,
    private val serverConfig: ServerConfig
) : RemoteDataSource {

    override suspend fun syncClients(
        clients: List<Client>,
        lastSync: Long
    ): Result<SyncResult<Client>> = executeSync(
        endpoint = "clients",
        data = clients,
        lastSync = lastSync
    )

    override suspend fun syncGroups(
        groups: List<PressureGroup>,
        lastSync: Long
    ): Result<SyncResult<PressureGroup>> = executeSync(
        endpoint = "groups",
        data = groups,
        lastSync = lastSync
    )

    override suspend fun syncRevisions(
        revisions: List<Revision>,
        lastSync: Long
    ): Result<SyncResult<Revision>> = executeSync(
        endpoint = "revisions",
        data = revisions,
        lastSync = lastSync
    )

    override suspend fun syncCurves(
        curves: List<CurvePoint>,
        lastSync: Long
    ): Result<SyncResult<CurvePoint>> = executeSync(
        endpoint = "curves",
        data = curves,
        lastSync = lastSync
    )

    private suspend inline fun <reified T> executeSync(
        endpoint: String,
        data: List<T>,
        lastSync: Long
    ): Result<SyncResult<T>> {
        return try {
            val baseUrl = serverConfig.getServerUrl()
            val token = serverConfig.getAuthToken()

            if (baseUrl.isBlank()) {
                return Result.failure(Exception("Servidor no configurado"))
            }

            val response: SyncResult<T> = client.post("$baseUrl/api/v1/$endpoint/sync") {
                header("Authorization", "Bearer $token")
                contentType(ContentType.Application.Json)
                setBody(SyncPayload(data, lastSync))
            }.body()

            Result.success(response)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
