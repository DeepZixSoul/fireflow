package com.fireflow.app

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import com.fireflow.data.sync.SyncWorker
import com.fireflow.database.dao.ChecklistItemDao
import com.fireflow.database.entity.ChecklistItemEntity
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

private val Context.seedDataStore by preferencesDataStore(name = "seed_state")

/**
 * Runs one-time initialization tasks: default checklist items and sync scheduling.
 *
 * The administrator account is no longer seeded here (R7): it is created on the
 * first-run setup screen so the app ships with no default credentials.
 */
@Singleton
class AppInitializer @Inject constructor(
    @ApplicationContext private val context: Context,
    private val checklistItemDao: ChecklistItemDao
) {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    private object Keys {
        val SEEDED = booleanPreferencesKey("database_seeded")
    }

    fun initialize() {
        scope.launch {
            val seeded = context.seedDataStore.data.first()[Keys.SEEDED] ?: false
            if (!seeded) {
                seedDefaultChecklist()
                context.seedDataStore.edit { it[Keys.SEEDED] = true }
            }
            SyncWorker.schedule(context)
        }
    }

    private suspend fun seedDefaultChecklist() {
        if (checklistItemDao.getAllEnabled().first().isEmpty()) {
            val items = listOf(
                ChecklistItemEntity(label = "Se han abierto las válvulas correspondientes.", orderIndex = 0),
                ChecklistItemEntity(label = "Se han cerrado las válvulas correspondientes.", orderIndex = 1),
                ChecklistItemEntity(label = "Tiene gasóleo.", orderIndex = 2),
                ChecklistItemEntity(label = "Las baterías tienen carga.", orderIndex = 3),
                ChecklistItemEntity(label = "El grupo queda en automático.", orderIndex = 4),
            )
            checklistItemDao.insertAll(items)
        }
    }
}
