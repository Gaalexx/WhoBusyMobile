package com.project.whobusy.data.network

import io.ktor.client.engine.HttpClientEngine
import io.ktor.client.engine.js.Js
import kotlin.js.toJsString

internal actual fun createBrowserEngine(): HttpClientEngine = Js.create {
    configureRequest {
        credentials = "include".toJsString()
        redirect = "error".toJsString()
        cache = "no-store".toJsString()
    }
}
