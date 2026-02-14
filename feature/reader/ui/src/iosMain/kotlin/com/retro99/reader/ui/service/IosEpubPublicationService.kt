package com.retro99.reader.ui.service

import com.github.michaelbull.result.Ok
import com.retro99.analytics.api.Analytics
import com.retro99.base.result.AppResult
import com.retro99.reader.domain.model.BookType
import com.retro99.reader.ui.bridge.EpubReaderBridgeRegistry
import com.retro99.reader.ui.model.PositionUiModel
import com.retro99.reader.ui.model.ReaderSettingsUiModel
import com.retro99.reader.ui.publication.EpubPublication
import org.koin.core.annotation.Single
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

/**
 * iOS implementation of [EpubPublicationService].
 *
 * Uses the [EpubReaderBridgeRegistry] to delegate to the Swift Readium implementation.
 */
@Single(binds = [EpubPublicationService::class])
class IosEpubPublicationService(
    analytics: Analytics,
) : BaseEpubPublicationService(analytics) {

    /**
     * Gets the bridge instance, if registered.
     */
    val bridge get() = EpubReaderBridgeRegistry.getBridge()

    override suspend fun openPublication(
        filePath: String,
        bookUuid: String,
        initialSettings: ReaderSettingsUiModel,
        bookType: BookType,
        initialPosition: PositionUiModel?,
    ): AppResult<EpubPublication> {
        val currentBridge = bridge
        if (currentBridge == null) {
            return createError("EPUB reader bridge not registered")
        }

        // Close any existing publication before opening a new one
        // This ensures proper cleanup of the previous view controller
        currentBridge.closePublication()

        return suspendCoroutine { continuation ->
            currentBridge.openPublication(
                filePath = filePath,
                onSuccess = {
                    val publication = EpubPublication(
                        currentBridge,
                        bookUuid,
                        initialSettings,
                        bookType,
                        initialPosition,
                    )
                    continuation.resume(Ok(publication))
                },
                onError = { errorMessage ->
                    continuation.resume(createError(errorMessage))
                },
            )
        }
    }
}

