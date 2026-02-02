package com.retro99.database.api

import com.retro99.base.result.AppResult

interface DatabaseExecutor {
    suspend fun <T> executeDatabaseOperation(operation: suspend () -> T): AppResult<T>
}