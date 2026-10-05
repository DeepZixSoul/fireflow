package com.igrupos.domain.repository

interface SyncSchedulerRepository {
    fun schedule()
    fun cancel()
}
