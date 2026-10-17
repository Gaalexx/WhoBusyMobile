package com.project.whobusy.data.repository

import com.project.whobusy.data.network.ApiHttpException
import com.project.whobusy.data.session.RemoteSessionManager
import com.project.whobusy.data.session.SessionStore
import com.project.whobusy.domain.model.CalendarUser
import com.project.whobusy.domain.model.InvalidCredentialsException
import com.project.whobusy.domain.model.RegistrationDraft
import com.project.whobusy.domain.repository.AuthRepository
import io.ktor.http.HttpStatusCode
import javax.inject.Inject

class RemoteAuthRepository @Inject constructor(
    private val sessions: RemoteSessionManager,
    sessionStore: SessionStore,
) : AuthRepository {
    override val currentUser = sessionStore.currentUser

    override suspend fun login(email: String, password: String): Result<CalendarUser> = repositoryResult {
        try {
            sessions.login(email, password)
        } catch (exception: ApiHttpException) {
            if (exception.status == HttpStatusCode.Unauthorized) throw InvalidCredentialsException()
            throw exception
        }
    }

    override suspend fun register(draft: RegistrationDraft): Result<CalendarUser> = repositoryResult {
        sessions.register(draft)
    }

    override suspend fun restoreSession(): Result<Unit> = repositoryResult { sessions.restore() }

    override suspend fun logout(): Result<Unit> = repositoryResult { sessions.logout() }

    override suspend fun logoutAll(): Result<Unit> = repositoryResult { sessions.logoutAll() }
}
