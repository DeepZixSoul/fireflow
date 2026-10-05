package com.igrupos.server.routes

import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Route.healthRoutes() {
    get("/health") {
        call.respond(
            mapOf(
                "status" to "ok",
                "service" to "igrupos-server",
                "timestamp" to System.currentTimeMillis().toString()
            )
        )
    }
}
