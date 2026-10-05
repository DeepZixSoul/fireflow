package com.igrupos.data.di

import com.igrupos.data.repository.AuthRepositoryImpl
import com.igrupos.data.repository.ChecklistRepositoryImpl
import com.igrupos.data.repository.ClientRepositoryImpl
import com.igrupos.data.repository.CurveRepositoryImpl
import com.igrupos.data.repository.MotorRepositoryImpl
import com.igrupos.data.repository.PhotoRepositoryImpl
import com.igrupos.data.repository.PressureGroupRepositoryImpl
import com.igrupos.data.repository.PressureMeasurementRepositoryImpl
import com.igrupos.data.repository.RevisionRepositoryImpl
import com.igrupos.data.datasource.RemoteDataSourceImpl
import com.igrupos.data.sync.SyncManager
import com.igrupos.data.sync.SyncSchedulerImpl
import com.igrupos.domain.repository.AuthRepository
import com.igrupos.domain.repository.ChecklistRepository
import com.igrupos.domain.repository.ClientRepository
import com.igrupos.domain.repository.CurveRepository
import com.igrupos.domain.repository.MotorRepository
import com.igrupos.domain.repository.PhotoRepository
import com.igrupos.domain.repository.PressureGroupRepository
import com.igrupos.domain.repository.PressureMeasurementRepository
import com.igrupos.domain.repository.RevisionRepository
import com.igrupos.domain.repository.SyncManagerRepository
import com.igrupos.domain.repository.SyncSchedulerRepository
import com.igrupos.domain.datasource.RemoteDataSource
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    @Singleton
    abstract fun bindAuthRepository(impl: AuthRepositoryImpl): AuthRepository

    @Binds
    @Singleton
    abstract fun bindClientRepository(impl: ClientRepositoryImpl): ClientRepository

    @Binds
    @Singleton
    abstract fun bindPressureGroupRepository(impl: PressureGroupRepositoryImpl): PressureGroupRepository

    @Binds
    @Singleton
    abstract fun bindRevisionRepository(impl: RevisionRepositoryImpl): RevisionRepository

    @Binds
    @Singleton
    abstract fun bindMotorRepository(impl: MotorRepositoryImpl): MotorRepository

    @Binds
    @Singleton
    abstract fun bindCurveRepository(impl: CurveRepositoryImpl): CurveRepository

    @Binds
    @Singleton
    abstract fun bindPhotoRepository(impl: PhotoRepositoryImpl): PhotoRepository

    @Binds
    @Singleton
    abstract fun bindChecklistRepository(impl: ChecklistRepositoryImpl): ChecklistRepository

    @Binds
    @Singleton
    abstract fun bindPressureMeasurementRepository(impl: PressureMeasurementRepositoryImpl): PressureMeasurementRepository

    @Binds
    @Singleton
    abstract fun bindSyncScheduler(impl: SyncSchedulerImpl): SyncSchedulerRepository

    @Binds
    @Singleton
    abstract fun bindSyncManager(impl: SyncManager): SyncManagerRepository

    @Binds
    @Singleton
    abstract fun bindRemoteDataSource(impl: RemoteDataSourceImpl): RemoteDataSource
}
