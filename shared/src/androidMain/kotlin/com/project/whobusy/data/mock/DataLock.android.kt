package com.project.whobusy.data.mock

internal actual class DataLock actual constructor() {
    private val monitor = Any()

    actual fun <Value> withLock(block: () -> Value): Value = synchronized(monitor, block)
}
