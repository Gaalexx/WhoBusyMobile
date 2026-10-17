package com.project.whobusy.data.session.storage

interface RefreshTokenStore {
    suspend fun read(): String?

    suspend fun write(token: String)

    suspend fun clear()
}
