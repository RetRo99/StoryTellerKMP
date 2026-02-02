package com.retro99.base.repository

import com.github.michaelbull.result.getOrElse
import com.github.michaelbull.result.onSuccess
import com.retro99.base.result.AppError
import com.retro99.base.result.AppResult
import kotlinx.coroutines.async
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.supervisorScope

/**
 * Base repository class that provides common functionality for all repositories.
 */
interface BaseRepository {

    /**
     * Fetches data from cache and remote in parallel.
     * - Emits cached data first if available
     * - Emits remote data when it arrives and saves it to cache
     * - If remote fails and cache was empty, throws the error
     * - If remote fails but cache was available, silently ignores the error
     *
     * @param cacheSource Suspend function that returns cached data result (null value means no cache)
     * @param remoteSource Suspend function that fetches data from remote
     * @param saveToCache Suspend function that saves remote data to cache
     * @return Flow that emits cached data (if available) then remote data
     */
    fun <T : Any> cachedRemoteFlow(
        cacheSource: suspend () -> AppResult<T?>,
        remoteSource: suspend () -> AppResult<T>,
        saveToCache: suspend (T) -> Unit,
    ): Flow<T> = flow {
        supervisorScope {
            val deferredCache = async { cacheSource() }
            val deferredRemote = async { remoteSource() }

            val cachedData = deferredCache.await().getOrElse { null }
            if (cachedData != null) {
                emit(cachedData)
            }

            deferredRemote.await()
                .onSuccess { remoteData ->
                    try {
                        saveToCache(remoteData)
                    } catch (e: Exception) {
                        ensureActive()
                        // Log but don't fail if cache save fails
                    }
                    emit(remoteData)
                }
                .getOrElse { error ->
                    ensureActive()
                    if (cachedData == null) {
                        throw error.toException()
                    }
                    // If we have cached data, silently ignore remote error
                }
        }
    }
}

private fun AppError.toException(): Exception {
    return when (this) {
        is AppError.NetworkError -> throwable as? Exception ?: Exception(message)
        is AppError.ApiError -> Exception("API Error $code: $message")
        is AppError.DatabaseError -> throwable as? Exception ?: Exception(message)
        is AppError.UnknownError -> throwable as? Exception ?: Exception(message)
    }
}
