package com.project.whobusy.data.network

import io.ktor.client.engine.HttpClientEngine
import io.ktor.client.engine.js.Js

internal actual fun createBrowserEngine(): HttpClientEngine = Js.create {
    configureRequest {
        credentials = "include"
        redirect = "error"
        cache = "no-store"
    }
}
