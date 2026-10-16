package com.project.whobusy.data.mock

import com.project.whobusy.data.repository.repositoryResult
import com.project.whobusy.data.session.SessionStore
import com.project.whobusy.domain.model.CalendarUser
import com.project.whobusy.domain.model.InvalidCredentialsException
import com.project.whobusy.domain.repository.AuthRepository
import java.util.Locale
import javax.inject.Inject

class MockAuthRepository
    @Inject
    constructor(
        private val sessionStore: SessionStore,
    ) : AuthRepository {
        override val currentUser = sessionStore.currentUser

        override suspend fun login(
            email: String,
            password: String,
        ): Result<CalendarUser> =
            repositoryResult {
                if (email.trim().lowercase(Locale.ROOT) != DemoAccount.EMAIL ||
                    password != DemoAccount.PASSWORD
                ) {
                    throw InvalidCredentialsException()
                }
                DemoAccount.user.also { sessionStore.save("mock-session", it) }
            }

        override fun logout() = sessionStore.clear()
    }
