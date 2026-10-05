package com.fireflow.server

import com.fireflow.server.config.ServerConfig
import com.fireflow.server.config.DatabaseManager
import com.fireflow.server.features.configureCORS
import com.fireflow.server.features.configureSecurity
import com.fireflow.server.features.configureSerialization
import com.fireflow.server.features.configureStatusPages
import com.fireflow.server.repositories.UserRepository
import com.fireflow.server.repositories.ClientRepository
import com.fireflow.server.repositories.PressureGroupRepository
import com.fireflow.server.repositories.RevisionRepository
import com.fireflow.server.repositories.CurveRepository
import com.fireflow.server.routes.authRoutes
import com.fireflow.server.routes.healthRoutes
import com.fireflow.server.routes.syncRoutes
import com.fireflow.server.services.AuthService
import com.fireflow.server.services.SyncService
import io.ktor.server.application.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import io.ktor.server.routing.*
import org.slf4j.LoggerFactory

fun main() {
    val logger = LoggerFactory.getLogger("FireFlowServer")
    val config = ServerConfig.fromEnvironment()

    logger.info("Starting FireFlow Server on port ${config.port}...")

    embeddedServer(Netty, port = config.port, host = "0.0.0.0", module = Application::module)
        .start(wait = true)
}

fun Application.module() {
    val logger = LoggerFactory.getLogger("FireFlowServer")
    val config = ServerConfig.fromEnvironment()

    logger.info("Configuring FireFlow Server...")

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

    logger.info("FireFlow Server configured successfully")
    logger.info("Endpoints:")
    logger.info("  GET  /health")
    logger.info("  POST /api/v1/auth/login")
    logger.info("  POST /api/v1/clients/sync (auth)")
    logger.info("  POST /api/v1/groups/sync (auth)")
    logger.info("  POST /api/v1/revisions/sync (auth)")
    logger.info("  POST /api/v1/curves/sync (auth)")
}
