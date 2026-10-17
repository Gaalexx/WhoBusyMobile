package com.project.whobusy.data.mock

import com.project.whobusy.data.repository.repositoryResult
import com.project.whobusy.data.session.SessionStore
import com.project.whobusy.domain.model.CalendarUser
import com.project.whobusy.domain.model.RegistrationDraft
import com.project.whobusy.domain.repository.AuthRepository
import javax.inject.Inject

class MockAuthRepository
@Inject
constructor(
    private val sessionStore: SessionStore,
    private val accountStore: MockAccountStore = MockAccountStore(),
) : AuthRepository {
    override val currentUser = sessionStore.currentUser

    override suspend fun login(
        email: String,
        password: String,
    ): Result<CalendarUser> =
        repositoryResult {
            accountStore.authenticate(email, password).also { sessionStore.save("mock-session", it) }
        }

    override suspend fun register(draft: RegistrationDraft): Result<CalendarUser> =
        repositoryResult {
            accountStore.register(draft).also { sessionStore.save("mock-session", it) }
        }

    override fun logout() = sessionStore.clear()
}
