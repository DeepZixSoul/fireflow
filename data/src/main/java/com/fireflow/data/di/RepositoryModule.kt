package com.fireflow.data.di

import com.fireflow.data.repository.AuthRepositoryImpl
import com.fireflow.data.repository.ChecklistRepositoryImpl
import com.fireflow.data.repository.ClientRepositoryImpl
import com.fireflow.data.repository.CurveRepositoryImpl
import com.fireflow.data.repository.MotorRepositoryImpl
import com.fireflow.data.repository.PhotoRepositoryImpl
import com.fireflow.data.repository.PressureGroupRepositoryImpl
import com.fireflow.data.repository.PressureMeasurementRepositoryImpl
import com.fireflow.data.repository.RevisionRepositoryImpl
import com.fireflow.data.repository.SecuritySettingsRepositoryImpl
import com.fireflow.data.datasource.RemoteDataSourceImpl
import com.fireflow.data.sync.SyncManager
import com.fireflow.data.sync.SyncSchedulerImpl
import com.fireflow.domain.repository.AuthRepository
import com.fireflow.domain.repository.ChecklistRepository
import com.fireflow.domain.repository.ClientRepository
import com.fireflow.domain.repository.CurveRepository
import com.fireflow.domain.repository.MotorRepository
import com.fireflow.domain.repository.PhotoRepository
import com.fireflow.domain.repository.PressureGroupRepository
import com.fireflow.domain.repository.PressureMeasurementRepository
import com.fireflow.domain.repository.RevisionRepository
import com.fireflow.domain.repository.SecuritySettingsRepository
import com.fireflow.domain.repository.SyncManagerRepository
import com.fireflow.domain.repository.SyncSchedulerRepository
import com.fireflow.domain.datasource.RemoteDataSource
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

    @Binds
    @Singleton
    abstract fun bindSecuritySettingsRepository(impl: SecuritySettingsRepositoryImpl): SecuritySettingsRepository
}
