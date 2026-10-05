package com.igrupos.server.features

import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit

class RateLimiter(
    private val maxRequests: Int = 5,
    private val windowMs: Long = 60_000
) {
    private data class RequestRecord(
        val timestamps: MutableList<Long> = mutableListOf()
    )

    private val records = ConcurrentHashMap<String, RequestRecord>()

    private val cleanupScheduler = Executors.newSingleThreadScheduledExecutor { r ->
        Thread(r, "rate-limiter-cleanup").apply { isDaemon = true }
    }

    init {
        cleanupScheduler.scheduleAtFixedRate(
            { cleanup() },
            windowMs,
            windowMs,
            TimeUnit.MILLISECONDS
        )
    }

    fun isAllowed(key: String): Boolean {
        val now = System.currentTimeMillis()
        val record = records.getOrPut(key) { RequestRecord() }

        synchronized(record.timestamps) {
            record.timestamps.removeAll { now - it > windowMs }

            if (record.timestamps.size >= maxRequests) {
                return false
            }

            record.timestamps.add(now)
            return true
        }
    }

    fun cleanup() {
        val now = System.currentTimeMillis()
        records.entries.removeIf { (_, record) ->
            synchronized(record.timestamps) {
                record.timestamps.removeAll { now - it > windowMs }
                record.timestamps.isEmpty()
            }
        }
    }

    fun shutdown() {
        cleanupScheduler.shutdown()
    }
}
