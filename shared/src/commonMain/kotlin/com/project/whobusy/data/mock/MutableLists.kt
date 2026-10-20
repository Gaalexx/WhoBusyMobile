package com.project.whobusy.data.mock

internal inline fun <Value> MutableList<Value>.replaceEach(transform: (Value) -> Value) {
    for (index in indices) {
        this[index] = transform(this[index])
    }
}
