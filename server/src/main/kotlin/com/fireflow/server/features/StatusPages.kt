package com.fireflow.server.features

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.plugins.statuspages.*
import io.ktor.server.response.*
import org.slf4j.LoggerFactory

fun Application.configureStatusPages() {
    val logger = LoggerFactory.getLogger("StatusPages")

    install(StatusPages) {
        exception<Throwable> { call, cause ->
            val method = call.request.local.method.value
            val uri = call.request.local.uri
                .replace(Regex("[?&](token|Authorization)=[^&]*"), "")
                .replace(Regex("Bearer\\s+\\S+"), "Bearer [REDACTED]")
            logger.error("Unhandled exception on $method $uri", cause)
            call.respond(
                HttpStatusCode.InternalServerError,
                mapOf("error" to "Error interno del servidor")
            )
        }

        status(HttpStatusCode.NotFound) { call, _ ->
            call.respond(
                HttpStatusCode.NotFound,
                mapOf("error" to "Recurso no encontrado")
            )
        }

        status(HttpStatusCode.Unauthorized) { call, _ ->
            call.respond(
                HttpStatusCode.Unauthorized,
                mapOf("error" to "No autorizado")
            )
        }

        status(HttpStatusCode.BadRequest) { call, _ ->
            call.respond(
                HttpStatusCode.BadRequest,
                mapOf("error" to "Solicitud inválida")
            )
        }
    }
}
