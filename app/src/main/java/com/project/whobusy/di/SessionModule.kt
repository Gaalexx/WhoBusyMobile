package com.project.whobusy.di

import com.project.whobusy.data.session.storage.EncryptedRefreshTokenStore
import com.project.whobusy.data.session.storage.RefreshTokenStore
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import javax.inject.Qualifier
import javax.inject.Singleton

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class ApplicationScope

@Module
@InstallIn(SingletonComponent::class)
object SessionModule {
    @Provides
    @Singleton
    fun provideRefreshTokenStore(store: EncryptedRefreshTokenStore): RefreshTokenStore = store

    @Provides
    @Singleton
    @ApplicationScope
    fun provideApplicationScope(): CoroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
}
