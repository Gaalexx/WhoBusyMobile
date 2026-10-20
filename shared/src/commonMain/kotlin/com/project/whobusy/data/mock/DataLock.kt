package com.project.whobusy.data.mock

internal expect class DataLock() {
    fun <Value> withLock(block: () -> Value): Value
}
