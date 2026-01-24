package com.retro99.base.repository

import com.github.michaelbull.result.fold
import com.retro99.base.result.AppResult
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

/**
 * Base repository class that provides common functionality for all repositories.
 */
interface BaseRepository {
    /**
     * A utility function that fetches data from cache first and then from remote.
     * It emits the cached data if available and then always fetches from remote and updates the cache.
     * If there's an error from the remote source and we have cached data, it doesn't emit the error.
     *
     * @param getCached A suspend function that returns the cached data or null if there's no cached data.
     * @param fetchRemote A suspend function that fetches data from the remote source and updates the cache.
     * @return A flow that emits the cached data if available and then the remote data.
     */
    fun <T> fetchWithCacheFirst(
        getCached: suspend () -> AppResult<T>?,
        fetchRemote: suspend (hasCachedData: Boolean, emit: suspend (AppResult<T>) -> Unit) -> Unit
    ): Flow<AppResult<T>> {
        return flow {
            val cachedResult = getCached()

            // Determine if we have cached data
            val hasCachedData = if (cachedResult != null) {
                cachedResult.fold(
                    success = { true },
                    failure = { false }
                )
            } else {
                false
            }

            // Emit cached data if available
            if (cachedResult != null) {
                emit(cachedResult)
            }

            // Always fetch from remote and update cache
            fetchRemote(hasCachedData) { result ->
                emit(result)
            }
        }
    }
}
