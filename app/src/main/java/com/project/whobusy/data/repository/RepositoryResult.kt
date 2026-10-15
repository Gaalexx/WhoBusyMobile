package com.project.whobusy.data.repository

import kotlinx.coroutines.CancellationException

internal suspend fun <Value> repositoryResult(block: suspend () -> Value): Result<Value> =
    try {
        Result.success(block())
    } catch (exception: CancellationException) {
        throw exception
    } catch (exception: Exception) {
        Result.failure(exception)
    }
