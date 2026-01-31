package com.retro99.reader.ui.controller

import com.retro99.reader.ui.bridge.EpubReaderBridgeRegistry
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

/**
 * iOS implementation of [EpubReaderController].
 *
 * Uses the [EpubReaderBridgeRegistry] to delegate to the Swift Readium implementation.
 */
class IosEpubReaderController : EpubReaderController {

    private var currentFilePath: String? = null

    private val _isReady = MutableStateFlow(false)
    override val isReady: StateFlow<Boolean> = _isReady.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    override val error: StateFlow<String?> = _error.asStateFlow()

    override suspend fun openPublication(filePath: String): Boolean {
        val bridge = EpubReaderBridgeRegistry.getBridge()
        if (bridge == null) {
            _error.value = "EPUB reader bridge not registered"
            return false
        }

        _error.value = null
        currentFilePath = filePath

        return suspendCoroutine { continuation ->
            bridge.openPublication(
                filePath = filePath,
                onSuccess = {
                    _isReady.value = true
                    continuation.resume(true)
                },
                onError = { errorMessage ->
                    _error.value = errorMessage
                    _isReady.value = false
                    continuation.resume(false)
                },
            )
        }
    }

    override fun closePublication() {
        EpubReaderBridgeRegistry.getBridge()?.closePublication()
        currentFilePath = null
        _isReady.value = false
        _error.value = null
    }

    override fun goToNextPage() {
        EpubReaderBridgeRegistry.getBridge()?.goToNextPage()
    }

    override fun goToPreviousPage() {
        EpubReaderBridgeRegistry.getBridge()?.goToPreviousPage()
    }

    /**
     * Gets the file path of the currently open publication.
     * This can be used by iOS-specific reader implementations.
     *
     * @return The file path, or null if no publication is open
     */
    fun getCurrentFilePath(): String? = currentFilePath
}

