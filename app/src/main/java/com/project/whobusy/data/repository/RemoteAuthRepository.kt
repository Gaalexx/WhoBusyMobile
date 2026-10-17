package com.project.whobusy.data.repository

import com.project.whobusy.data.remote.api.AuthAPI
import com.project.whobusy.data.remote.dto.LoginRequestDTO
import com.project.whobusy.data.session.SessionStore
import com.project.whobusy.domain.model.CalendarUser
import com.project.whobusy.domain.model.InvalidCredentialsException
import com.project.whobusy.domain.repository.AuthRepository
import io.ktor.client.plugins.ClientRequestException
import io.ktor.http.HttpStatusCode
import java.util.Locale
import javax.inject.Inject

class RemoteAuthRepository
@Inject
constructor(
    private val api: AuthAPI,
    private val sessionStore: SessionStore,
) : AuthRepository {
    override val currentUser = sessionStore.currentUser

    override suspend fun login(
        email: String,
        password: String,
    ): Result<CalendarUser> =
        repositoryResult {
            val request = LoginRequestDTO(email.trim().lowercase(Locale.ROOT), password)
            val token =
                try {
                    api.login(request)
                } catch (exception: ClientRequestException) {
                    if (exception.response.status == HttpStatusCode.Unauthorized) {
                        throw InvalidCredentialsException()
                    }
                    throw exception
                }
            require(token.tokenType.equals("bearer", ignoreCase = true))
            require(token.accessToken.isNotBlank())
            val user = api.getCurrentUser(token.accessToken).toDomain()
            sessionStore.save(token.accessToken, user)
            user
        }

    override fun logout() = sessionStore.clear()
}
