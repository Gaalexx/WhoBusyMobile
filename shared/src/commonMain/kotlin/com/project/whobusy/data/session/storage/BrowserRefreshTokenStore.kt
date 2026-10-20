package com.project.whobusy.data.session.storage

object BrowserRefreshTokenStore : RefreshTokenStore {
    override suspend fun read(): String? = error("Browser refresh credentials are managed by HttpOnly cookies")

    override suspend fun write(token: String): Unit = error("Browser refresh credentials must never enter JavaScript storage")

    override suspend fun clear(): Unit = error("HttpOnly cookies must be cleared by the server")
}
