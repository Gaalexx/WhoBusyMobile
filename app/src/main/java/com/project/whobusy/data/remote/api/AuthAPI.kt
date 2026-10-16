package com.project.whobusy.data.remote.api

import com.project.whobusy.data.remote.ApiConfiguration
import com.project.whobusy.data.remote.dto.LoginRequestDTO
import com.project.whobusy.data.remote.dto.TokenDTO
import com.project.whobusy.data.remote.dto.UserDTO
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.bearerAuth
import io.ktor.client.request.get
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType
import javax.inject.Inject

class AuthAPI
    @Inject
    constructor(
        private val client: HttpClient,
        private val configuration: ApiConfiguration,
    ) {
        suspend fun login(request: LoginRequestDTO): TokenDTO =
            client
                .post(configuration.endpoint("auth/login")) {
                    contentType(ContentType.Application.Json)
                    setBody(request)
                }.body()

        suspend fun getCurrentUser(token: String): UserDTO =
            client
                .get(configuration.endpoint("auth/me")) {
                    bearerAuth(token)
                }.body()
    }
