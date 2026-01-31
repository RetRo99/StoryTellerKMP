package com.retro99.reader.ui.controller

import com.retro99.reader.ui.bridge.EpubReaderBridgeRegistry
import platform.UIKit.UIViewController
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

/**
 * iOS implementation of [EpubReaderController].
 *
 * Uses the [EpubReaderBridgeRegistry] to delegate to the Swift Readium implementation.
 */
class IosEpubReaderController : BaseEpubReaderController() {

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

    override fun goToNextPage() {
        bridge?.goToNextPage()
    }

    override fun goToPreviousPage() {
        bridge?.goToPreviousPage()
    }

    /**
     * Creates the reader view controller from the bridge.
     * This is iOS-specific and used by [EpubReaderView].
     *
     * @return The UIViewController for the reader, or null if not available
     */
    fun createReaderViewController(): UIViewController? = bridge?.createReaderViewController()
}

