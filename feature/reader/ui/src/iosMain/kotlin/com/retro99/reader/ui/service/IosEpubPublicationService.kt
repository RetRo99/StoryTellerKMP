package com.retro99.reader.ui.service

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
class IosEpubPublicationService : BaseEpubPublicationService() {

    /**
     * Gets the bridge instance, if registered.
     */
    val bridge get() = EpubReaderBridgeRegistry.getBridge()

    private var initialSettings: ReaderSettingsUiModel? = null

    override suspend fun openPublication(
        filePath: String,
        bookUuid: String,
        initialSettings: ReaderSettingsUiModel,
        bookType: BookType,
        initialPosition: PositionUiModel?,
    ): EpubPublication? {
        val currentBridge = bridge
        if (currentBridge == null) {
            setError("EPUB reader bridge not registered")
            return null
        }

        // Close any existing publication before opening a new one
        // This ensures proper cleanup of the previous view controller
        currentBridge.closePublication()

        resetState()
        this.initialSettings = initialSettings

        return suspendCoroutine { continuation ->
            currentBridge.openPublication(
                filePath = filePath,
                onSuccess = {
                    setReady()
                    val publication = EpubPublication(
                        currentBridge,
                        bookUuid,
                        initialSettings,
                        bookType,
                        initialPosition,
                    )
                    continuation.resume(publication)
                },
                onError = { errorMessage ->
                    setError(errorMessage)
                    continuation.resume(null)
                },
            )
        }
    }

    override fun closePublication() {
        bridge?.closePublication()
        initialSettings = null
        resetState()
    }

    /**
     * Gets the initial settings that were used to open the publication.
     * This is iOS-specific and used by the View to create the reader with initial preferences.
     *
     * @return The initial settings, or null if no publication is open
     */
    fun getInitialSettings(): ReaderSettingsUiModel? = initialSettings
}

