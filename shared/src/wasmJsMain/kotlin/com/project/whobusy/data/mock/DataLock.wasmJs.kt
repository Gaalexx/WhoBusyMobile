package com.project.whobusy.data.mock

internal actual class DataLock actual constructor() {
    actual fun <Value> withLock(block: () -> Value): Value = block()
}
