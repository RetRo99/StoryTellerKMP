package com.retro99.reader.ui.service

import com.retro99.reader.domain.model.ReaderSettingsDomainModel
import com.retro99.reader.ui.bridge.EpubReaderBridgeRegistry
import com.retro99.reader.ui.bridge.EpubReaderSettings
import platform.UIKit.UIViewController
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

/**
 * iOS implementation of [EpubPublicationService].
 *
 * Uses the [EpubReaderBridgeRegistry] to delegate to the Swift Readium implementation.
 */
class IosEpubPublicationService : BaseEpubPublicationService() {

    /**
     * Gets the bridge instance, if registered.
     */
    val bridge get() = EpubReaderBridgeRegistry.getBridge()

    override suspend fun openPublication(filePath: String): Boolean {
        val currentBridge = bridge
        if (currentBridge == null) {
            setError("EPUB reader bridge not registered")
            return false
        }

        resetState()

        return suspendCoroutine { continuation ->
            currentBridge.openPublication(
                filePath = filePath,
                onSuccess = {
                    setReady()
                    continuation.resume(true)
                },
                onError = { errorMessage ->
                    setError(errorMessage)
                    continuation.resume(false)
                },
            )
        }
    }

    override fun closePublication() {
        bridge?.closePublication()
        resetState()
    }

    /**
     * Creates the reader view controller from the bridge.
     * This is iOS-specific and used by the View to create the navigator.
     *
     * @param initialSettings The initial reader settings to apply
     * @return The UIViewController for the reader, or null if not available
     */
    fun createReaderViewController(initialSettings: ReaderSettingsDomainModel): UIViewController? =
        bridge?.createReaderViewController(settings = EpubReaderSettings.from(initialSettings))
}

