package com.igrupos.database.di

import android.content.Context
import androidx.room.Room
import com.igrupos.database.AppDatabase
import com.igrupos.database.BuildConfig
import com.igrupos.database.MIGRATION_1_2
import com.igrupos.database.MIGRATION_2_4
import com.igrupos.database.MIGRATION_3_4
import com.igrupos.database.MIGRATION_4_5
import com.igrupos.database.MIGRATION_5_6
import com.igrupos.database.MIGRATION_6_7
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import net.zetetic.database.sqlcipher.SupportOpenHelperFactory
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    private const val DB_NAME = "igrupos.db"

    @Provides
    @Singleton
    fun provideAppDatabase(
        @ApplicationContext context: Context
    ): AppDatabase {
        System.loadLibrary("sqlcipher")
        val passphrase = BuildConfig.DB_PASSPHRASE.toByteArray()
        val factory = SupportOpenHelperFactory(passphrase)

        return Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            DB_NAME
        )
            .openHelperFactory(factory)
            .addMigrations(MIGRATION_1_2, MIGRATION_2_4, MIGRATION_3_4, MIGRATION_4_5, MIGRATION_5_6, MIGRATION_6_7)
            .build()
    }

    @Provides
    @Singleton
    fun provideUserDao(db: AppDatabase) = db.userDao()

    @Provides
    @Singleton
    fun provideClientDao(db: AppDatabase) = db.clientDao()

    @Provides
    @Singleton
    fun providePressureGroupDao(db: AppDatabase) = db.pressureGroupDao()

    @Provides
    @Singleton
    fun provideRevisionDao(db: AppDatabase) = db.revisionDao()

    @Provides
    @Singleton
    fun provideMotorDao(db: AppDatabase) = db.motorDao()

    @Provides
    @Singleton
    fun providePressureMeasurementDao(db: AppDatabase) = db.pressureMeasurementDao()

    @Provides
    @Singleton
    fun provideCurvePointDao(db: AppDatabase) = db.curvePointDao()

    @Provides
    @Singleton
    fun providePhotoDao(db: AppDatabase) = db.photoDao()

    @Provides
    @Singleton
    fun provideChecklistItemDao(db: AppDatabase) = db.checklistItemDao()
}
