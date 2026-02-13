package com.retro99.base.result

import com.github.michaelbull.result.Err
import com.github.michaelbull.result.Ok
import com.github.michaelbull.result.Result
import com.github.michaelbull.result.onFailure
import com.retro99.analytics.api.Analytics
import com.retro99.translations.StringRes
import org.jetbrains.compose.resources.StringResource
import resources.translations.error_api_generic
import resources.translations.error_api_unknown
import resources.translations.error_database_generic
import resources.translations.error_database_specific
import resources.translations.error_network_connectivity
import resources.translations.error_network_generic
import resources.translations.error_unknown
import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.InvocationKind
import kotlin.contracts.contract
import kotlin.coroutines.cancellation.CancellationException

typealias AppResult<T> = Result<T, AppError>

typealias CompletableResult = Result<Unit, AppError>

inline fun <V, E> Result<V, E>.andThenAlways(action: (Result<V, E>) -> Result<V, E>): Result<V, E> {
    return action(this)
}

sealed class AppError(open val message: String?) {
    data class NetworkError(val throwable: Throwable, val isConnectivity: Boolean = false) :
        AppError(throwable.message)

    data class ApiError(val code: Int, override val message: String? = null) : AppError(message)
    data class DatabaseError(val throwable: Throwable, val table: String? = null) :
        AppError(throwable.message)

    data class UnknownError(val throwable: Throwable) : AppError(throwable.message)

    /**
     * Converts this AppError to a Throwable for logging purposes.
     */
    fun toThrowable(): Throwable = when (this) {
        is NetworkError -> throwable
        is DatabaseError -> throwable
        is UnknownError -> throwable
        is ApiError -> Exception("API Error $code: $message")
    }

    fun toStringRes(): StringResource {
        return when (this) {
            is NetworkError -> if (isConnectivity) {
                StringRes.error_network_connectivity
            } else {
                StringRes.error_network_generic
            }

            is ApiError -> if (message != null) {
                StringRes.error_api_generic
            } else {
                StringRes.error_api_unknown
            }

            is DatabaseError -> if (table != null) {
                StringRes.error_database_specific
            } else {
                StringRes.error_database_generic
            }

            is UnknownError -> StringRes.error_unknown
        }
    }
}

/**
 * Calls the specified function [block] with [this] value as its receiver and returns its
 * encapsulated result if invocation was successful, catching any [Throwable] exception that was
 * thrown from the [block] function execution and encapsulating it as an AppError.
 * CancellationException is rethrown to allow proper coroutine cancellation.
 */
@OptIn(ExperimentalContracts::class)
inline infix fun <T, V> T.runCatchingAsAppError(block: T.() -> V): Result<V, AppError> {
    contract {
        callsInPlace(block, InvocationKind.AT_MOST_ONCE)
    }
    return try {
        Ok(block())
    } catch (e: CancellationException) {
        throw e
    } catch (e: Throwable) {
        Err(AppError.UnknownError(e))
    }
}

/**
 * Logs the AppError using the provided Analytics instance and returns the error unchanged.
 * Useful for chaining in error handling flows.
 *
 * @param analytics The Analytics instance to use for logging
 * @param context A descriptive message about where the error occurred
 * @return The same AppError for chaining
 */
fun AppError.log(analytics: Analytics, context: String): AppError {
    analytics.logException(toThrowable(), context)
    return this
}

/**
 * Extension function on AppResult that logs any error and returns the result unchanged.
 * Useful for adding logging to existing error handling chains.
 *
 * @param analytics The Analytics instance to use for logging
 * @param context A descriptive message about where the error occurred
 * @return The same Result for chaining
 */
fun <T> AppResult<T>.logOnFailure(analytics: Analytics, context: String): AppResult<T> {
    return onFailure { error ->
        error.log(analytics, context)
    }
}
