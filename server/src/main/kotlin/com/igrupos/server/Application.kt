package com.igrupos.server

import com.igrupos.server.config.ServerConfig
import com.igrupos.server.config.DatabaseManager
import com.igrupos.server.features.configureCORS
import com.igrupos.server.features.configureSecurity
import com.igrupos.server.features.configureSerialization
import com.igrupos.server.features.configureStatusPages
import com.igrupos.server.repositories.UserRepository
import com.igrupos.server.repositories.ClientRepository
import com.igrupos.server.repositories.PressureGroupRepository
import com.igrupos.server.repositories.RevisionRepository
import com.igrupos.server.repositories.CurveRepository
import com.igrupos.server.routes.authRoutes
import com.igrupos.server.routes.healthRoutes
import com.igrupos.server.routes.syncRoutes
import com.igrupos.server.services.AuthService
import com.igrupos.server.services.SyncService
import io.ktor.server.application.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import io.ktor.server.routing.*
import org.slf4j.LoggerFactory

fun main() {
    val logger = LoggerFactory.getLogger("IGruposServer")
    val config = ServerConfig.fromEnvironment()

    logger.info("Starting IGrupos Server on port ${config.port}...")

    embeddedServer(Netty, port = config.port, host = "0.0.0.0", module = Application::module)
        .start(wait = true)
}

fun Application.module() {
    val logger = LoggerFactory.getLogger("IGruposServer")
    val config = ServerConfig.fromEnvironment()

    logger.info("Configuring IGrupos Server...")

    // Database
    DatabaseManager.init(config.database)
    val database = DatabaseManager.getDatabase()

    // Repositories
    val userRepository = UserRepository(database)
    val clientRepository = ClientRepository(database)
    val groupRepository = PressureGroupRepository(database)
    val revisionRepository = RevisionRepository(database)
    val curveRepository = CurveRepository(database)

    // Services
    val authService = AuthService(userRepository)
    val syncService = SyncService(clientRepository, groupRepository, revisionRepository, curveRepository)

    // Features
    configureSerialization()
    configureCORS()
    configureStatusPages()
    configureSecurity(config.jwt, userRepository)

    // Routing
    routing {
        healthRoutes()
        authRoutes(authService)
        syncRoutes(syncService)
    }

    logger.info("IGrupos Server configured successfully")
    logger.info("Endpoints:")
    logger.info("  GET  /health")
    logger.info("  POST /api/v1/auth/login")
    logger.info("  POST /api/v1/clients/sync (auth)")
    logger.info("  POST /api/v1/groups/sync (auth)")
    logger.info("  POST /api/v1/revisions/sync (auth)")
    logger.info("  POST /api/v1/curves/sync (auth)")
}
