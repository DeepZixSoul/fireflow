package com.fireflow.server.config

import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import org.flywaydb.core.Flyway
import org.jetbrains.exposed.sql.Database
import org.slf4j.LoggerFactory

object DatabaseManager {
    private val logger = LoggerFactory.getLogger(DatabaseManager::class.java)
    private lateinit var dataSource: HikariDataSource
    private lateinit var database: Database

    fun init(config: DatabaseConnectionConfig) {
        logger.info("Initializing database connection...")

        val hikariConfig = HikariConfig().apply {
            jdbcUrl = config.url
            driverClassName = config.driver
            username = config.user
            password = config.password
            maximumPoolSize = config.maximumPoolSize
            minimumIdle = config.minimumIdle
            idleTimeout = config.idleTimeout
            connectionTimeout = config.connectionTimeout
            maxLifetime = config.maxLifetime
            isAutoCommit = false
            transactionIsolation = "TRANSACTION_REPEATABLE_READ"
            validate()
        }

        dataSource = HikariDataSource(hikariConfig)

        database = Database.connect(dataSource)

        runMigrations()

        logger.info("Database initialized successfully")
    }

    private fun runMigrations() {
        logger.info("Running Flyway migrations...")

        val isProduction = System.getenv("ENVIRONMENT") == "production"

        val flywayBuilder = Flyway.configure()
            .dataSource(dataSource)
            .locations("classpath:db/migration")

        if (isProduction) {
            flywayBuilder.baselineOnMigrate(false)
        } else {
            flywayBuilder.baselineOnMigrate(true)
        }

        val flyway = flywayBuilder.load()
        val result = flyway.migrate()

        logger.info("Flyway migrations completed: ${result.migrationsExecuted} applied")
    }

    fun getDatabase(): Database {
        if (!::database.isInitialized) {
            throw IllegalStateException("Database not initialized. Call init() first.")
        }
        return database
    }

    fun close() {
        if (::dataSource.isInitialized) {
            dataSource.close()
            logger.info("Database connection pool closed")
        }
    }
}
