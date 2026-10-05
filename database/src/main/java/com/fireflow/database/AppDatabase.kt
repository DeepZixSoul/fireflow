package com.fireflow.database

import androidx.room.Database
import androidx.room.RoomDatabase
import com.fireflow.database.dao.ChecklistItemDao
import com.fireflow.database.dao.ClientDao
import com.fireflow.database.dao.CurvePointDao
import com.fireflow.database.dao.MotorDao
import com.fireflow.database.dao.PhotoDao
import com.fireflow.database.dao.PressureGroupDao
import com.fireflow.database.dao.PressureMeasurementDao
import com.fireflow.database.dao.RevisionDao
import com.fireflow.database.dao.UserDao
import com.fireflow.database.entity.ChecklistItemEntity
import com.fireflow.database.entity.ClientEntity
import com.fireflow.database.entity.CurvePointEntity
import com.fireflow.database.entity.MotorEntity
import com.fireflow.database.entity.PhotoEntity
import com.fireflow.database.entity.PressureGroupEntity
import com.fireflow.database.entity.PressureMeasurementEntity
import com.fireflow.database.entity.RevisionEntity
import com.fireflow.database.entity.UserEntity

@Database(
    entities = [
        UserEntity::class,
        ClientEntity::class,
        PressureGroupEntity::class,
        RevisionEntity::class,
        MotorEntity::class,
        PressureMeasurementEntity::class,
        CurvePointEntity::class,
        PhotoEntity::class,
        ChecklistItemEntity::class
    ],
    version = 7,
    exportSchema = true
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun userDao(): UserDao
    abstract fun clientDao(): ClientDao
    abstract fun pressureGroupDao(): PressureGroupDao
    abstract fun revisionDao(): RevisionDao
    abstract fun motorDao(): MotorDao
    abstract fun pressureMeasurementDao(): PressureMeasurementDao
    abstract fun curvePointDao(): CurvePointDao
    abstract fun photoDao(): PhotoDao
    abstract fun checklistItemDao(): ChecklistItemDao
}
