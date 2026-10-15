package com.project.whobusy.data.remote

import java.net.URI

data class ApiConfiguration(val baseUrl: String) {
    fun endpoint(path: String): String {
        val uri = URI(baseUrl)
        require(uri.scheme == "https" && !uri.host.isNullOrBlank()) {
            "Set whoBusy.apiBaseUrl to the HTTPS API base URL"
        }
        require(uri.rawQuery == null && uri.rawFragment == null && uri.userInfo == null) {
            "API base URL must not contain credentials, query or fragment"
        }
        return baseUrl.trimEnd('/') + "/" + path.trimStart('/')
    }
}
