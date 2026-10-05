package com.igrupos.app

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import com.igrupos.data.sync.SyncWorker
import com.igrupos.database.dao.ChecklistItemDao
import com.igrupos.database.dao.UserDao
import com.igrupos.database.entity.ChecklistItemEntity
import com.igrupos.database.entity.UserEntity
import com.igrupos.security.PasswordHasher
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

private val Context.seedDataStore by preferencesDataStore(name = "seed_state")

@Singleton
class AppInitializer @Inject constructor(
    @ApplicationContext private val context: Context,
    private val userDao: UserDao,
    private val checklistItemDao: ChecklistItemDao,
    private val passwordHasher: PasswordHasher
) {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    private object Keys {
        val SEEDED = booleanPreferencesKey("database_seeded")
    }

    fun initialize() {
        scope.launch {
            val seeded = context.seedDataStore.data.first()[Keys.SEEDED] ?: false
            if (!seeded) {
                seedDefaultData()
                context.seedDataStore.edit { it[Keys.SEEDED] = true }
            }
            SyncWorker.schedule(context)
        }
    }

    private suspend fun seedDefaultData() {
        if (userDao.getByUsername("admin") == null) {
            val adminHash = passwordHasher.hash(SeedCredentials.ADMIN_PASSWORD)
            userDao.insert(
                UserEntity(
                    username = "admin",
                    displayName = "Administrador",
                    email = "admin@igrupos.com",
                    passwordHash = adminHash,
                    role = "ADMIN",
                    mustChangePassword = true
                )
            )
        }

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
