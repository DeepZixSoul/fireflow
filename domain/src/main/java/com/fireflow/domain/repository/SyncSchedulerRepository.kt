package com.fireflow.domain.repository

interface SyncSchedulerRepository {
    fun schedule()
    fun cancel()
}
