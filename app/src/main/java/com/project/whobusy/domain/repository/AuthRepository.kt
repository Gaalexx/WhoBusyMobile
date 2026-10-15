package com.project.whobusy.domain.repository

import com.project.whobusy.domain.model.CalendarUser
import kotlinx.coroutines.flow.Flow

interface AuthRepository {
    val currentUser: Flow<CalendarUser?>
    suspend fun login(email: String, password: String): Result<CalendarUser>
    fun logout()
}
