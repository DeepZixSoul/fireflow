package com.igrupos.data.sync

import android.content.Context
import com.igrupos.domain.repository.SyncSchedulerRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SyncSchedulerImpl @Inject constructor(
    @ApplicationContext private val context: Context
) : SyncSchedulerRepository {
    override fun schedule() {
        SyncWorker.schedule(context)
    }

    override fun cancel() {
        SyncWorker.cancel(context)
    }
}
