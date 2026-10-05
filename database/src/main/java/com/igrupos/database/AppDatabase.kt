package com.igrupos.database

import androidx.room.Database
import androidx.room.RoomDatabase
import com.igrupos.database.dao.ChecklistItemDao
import com.igrupos.database.dao.ClientDao
import com.igrupos.database.dao.CurvePointDao
import com.igrupos.database.dao.MotorDao
import com.igrupos.database.dao.PhotoDao
import com.igrupos.database.dao.PressureGroupDao
import com.igrupos.database.dao.PressureMeasurementDao
import com.igrupos.database.dao.RevisionDao
import com.igrupos.database.dao.UserDao
import com.igrupos.database.entity.ChecklistItemEntity
import com.igrupos.database.entity.ClientEntity
import com.igrupos.database.entity.CurvePointEntity
import com.igrupos.database.entity.MotorEntity
import com.igrupos.database.entity.PhotoEntity
import com.igrupos.database.entity.PressureGroupEntity
import com.igrupos.database.entity.PressureMeasurementEntity
import com.igrupos.database.entity.RevisionEntity
import com.igrupos.database.entity.UserEntity

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
