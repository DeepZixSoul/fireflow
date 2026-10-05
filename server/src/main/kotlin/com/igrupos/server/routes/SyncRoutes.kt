package com.igrupos.server.routes

import com.igrupos.server.services.SyncService
import com.igrupos.server.models.*
import com.igrupos.server.models.request.SyncRequest
import com.igrupos.server.models.response.SyncResponse
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import org.slf4j.LoggerFactory

private val logger = LoggerFactory.getLogger("SyncRoutes")

private const val MAX_SYNC_RECORDS = 10_000

private suspend inline fun <reified T> ApplicationCall.handleSync(
    entityName: String,
    syncHandler: (SyncRequest<T>) -> SyncResponse<T>
) {
    try {
        val principal = principal<JWTPrincipal>()
        if (principal == null) {
            logger.warn("$entityName sync: missing JWT principal")
            respond(
                HttpStatusCode.Unauthorized,
                mapOf("error" to "Token inválido")
            )
            return
        }

        val role = principal.payload.getClaim("role").asString()
        if (role != "admin" && role != "technician") {
            logger.warn("$entityName sync: unauthorized role=$role")
            respond(
                HttpStatusCode.Forbidden,
                mapOf("error" to "Sin permisos para sincronizar $entityName")
            )
            return
        }

        val username = principal.payload.getClaim("username").asString()
        logger.debug("{} sync request from user: {}", entityName, username)

        val request = receive<SyncRequest<T>>()

        if (request.data.size > MAX_SYNC_RECORDS) {
            logger.warn("$entityName sync: payload too large (${request.data.size} records)")
            respond(
                HttpStatusCode.BadRequest,
                mapOf("error" to "Demasiados registros. Máximo $MAX_SYNC_RECORDS por petición.")
            )
            return
        }

        val response = syncHandler(request)

        respond(HttpStatusCode.OK, response)
    } catch (e: Exception) {
        logger.error("$entityName sync error", e)
        respond(
            HttpStatusCode.InternalServerError,
            mapOf("error" to "Error en sincronización de $entityName")
        )
    }
}

fun Route.syncRoutes(syncService: SyncService) {
    authenticate("auth-jwt") {
        route("/api/v1") {
            post("/clients/sync") {
                call.handleSync("clientes") { syncService.syncClients(it) }
            }

            post("/groups/sync") {
                call.handleSync("grupos") { syncService.syncGroups(it) }
            }

            post("/revisions/sync") {
                call.handleSync("revisiones") { syncService.syncRevisions(it) }
            }

            post("/curves/sync") {
                call.handleSync("curvas") { syncService.syncCurves(it) }
            }
        }
    }
}
