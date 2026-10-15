package com.project.whobusy.di

import com.project.whobusy.BuildConfig
import com.project.whobusy.data.mock.MockAuthRepository
import com.project.whobusy.data.mock.DemoAccount
import com.project.whobusy.presentation.login.viewmodel.LoginDefaults
import com.project.whobusy.data.mock.MockCalendarRepository
import com.project.whobusy.data.remote.ApiConfiguration
import com.project.whobusy.data.repository.RemoteAuthRepository
import com.project.whobusy.data.repository.RemoteCalendarRepository
import com.project.whobusy.domain.repository.AuthRepository
import com.project.whobusy.domain.repository.CalendarRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import java.time.Clock
import javax.inject.Provider
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object CalendarModule {
    @Provides
    fun provideLoginDefaults(): LoginDefaults = if (BuildConfig.USE_MOCK_DATA) {
        LoginDefaults(DemoAccount.EMAIL, DemoAccount.PASSWORD)
    } else LoginDefaults()

    @Provides
    @Singleton
    fun provideClock(): Clock = Clock.systemDefaultZone()

    @Provides
    @Singleton
    fun provideApiConfiguration(): ApiConfiguration = ApiConfiguration(BuildConfig.API_BASE_URL)

    @Provides
    @Singleton
    fun provideAuthRepository(
        mock: Provider<MockAuthRepository>,
        remote: Provider<RemoteAuthRepository>,
    ): AuthRepository = if (BuildConfig.USE_MOCK_DATA) mock.get() else remote.get()

    @Provides
    @Singleton
    fun provideCalendarRepository(
        mock: Provider<MockCalendarRepository>,
        remote: Provider<RemoteCalendarRepository>,
    ): CalendarRepository = if (BuildConfig.USE_MOCK_DATA) mock.get() else remote.get()
}
