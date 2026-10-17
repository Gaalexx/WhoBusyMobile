package com.project.whobusy.domain.repository

import com.project.whobusy.domain.model.CalendarUser
import com.project.whobusy.domain.model.RegistrationDraft
import kotlinx.coroutines.flow.Flow

interface AuthRepository {
    val currentUser: Flow<CalendarUser?>

    suspend fun login(
        email: String,
        password: String,
    ): Result<CalendarUser>

    suspend fun register(draft: RegistrationDraft): Result<CalendarUser> =
        Result.failure(UnsupportedOperationException("Registration is not supported"))

    suspend fun restoreSession(): Result<Unit> = Result.success(Unit)

    suspend fun logout(): Result<Unit>

    suspend fun logoutAll(): Result<Unit> =
        Result.failure(UnsupportedOperationException("Global logout is not supported"))
}
