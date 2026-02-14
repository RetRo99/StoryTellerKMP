package com.retro99.reader.ui.service

import com.github.michaelbull.result.Err
import com.retro99.analytics.api.Analytics
import com.retro99.base.result.AppError
import com.retro99.base.result.AppResult

/**
 * Base implementation of [EpubPublicationService] that provides shared utilities.
 *
 * This abstract class handles common logic for both Android and iOS implementations,
 * including error logging and result creation.
 *
 * Platform-specific implementations should extend this class and implement the
 * publication lifecycle methods.
 */
abstract class BaseEpubPublicationService(
    protected val analytics: Analytics,
) : EpubPublicationService {

    /**
     * Creates an error result with the given message and logs it to analytics.
     *
     * @param message The error message
     * @return An [AppResult] containing the error
     */
    protected fun <T> createError(message: String): AppResult<T> {
        analytics.logException(
            throwable = Throwable(message),
            message = "EpubPublicationService: Failed to open publication",
        )
        return Err(AppError.UnknownError(Throwable(message)))
    }
}

