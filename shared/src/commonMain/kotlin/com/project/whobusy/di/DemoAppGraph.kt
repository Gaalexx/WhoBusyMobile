package com.project.whobusy.di

import com.project.whobusy.data.mock.DemoAccount
import com.project.whobusy.data.mock.MockAuthRepository
import com.project.whobusy.data.mock.MockCalendarRepository
import com.project.whobusy.data.mock.MockWorkspaceRepository
import com.project.whobusy.domain.repository.AuthRepository
import com.project.whobusy.domain.repository.CalendarRepository
import com.project.whobusy.domain.repository.WorkspaceRepository
import com.project.whobusy.presentation.login.viewmodel.LoginDefaults
import com.project.whobusy.time.Clock
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.Binds
import dev.zacsweers.metro.DependencyGraph
import dev.zacsweers.metro.Provides
import dev.zacsweers.metro.SingleIn
import dev.zacsweers.metrox.viewmodel.ViewModelGraph

@DependencyGraph(AppScope::class)
interface DemoAppGraph : ViewModelGraph {
    val auth: AuthRepository
    val calendar: CalendarRepository
    val workspaces: WorkspaceRepository

    @Binds
    val MockAuthRepository.bindAuthRepository: AuthRepository

    @Binds
    val MockCalendarRepository.bindCalendarRepository: CalendarRepository

    @Binds
    val MockWorkspaceRepository.bindWorkspaceRepository: WorkspaceRepository

    @Provides
    @SingleIn(AppScope::class)
    fun provideClock(): Clock = Clock.systemDefaultZone()

    @Provides
    fun provideLoginDefaults(): LoginDefaults = LoginDefaults(DemoAccount.EMAIL, DemoAccount.PASSWORD)
}
