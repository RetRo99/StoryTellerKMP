package com.retro99.reader.ui.controller

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * iOS implementation of [EpubReaderController].
 *
 * This is a placeholder implementation until Readium iOS integration is added.
 * Currently, it simulates a successful publication open for the file path
 * so the UI can render a placeholder message.
 */
class IosEpubReaderController : EpubReaderController {

    private var currentFilePath: String? = null

    private val _isReady = MutableStateFlow(false)
    override val isReady: StateFlow<Boolean> = _isReady.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    override val error: StateFlow<String?> = _error.asStateFlow()

    override suspend fun openPublication(filePath: String): Boolean {
        return try {
            _error.value = null
            currentFilePath = filePath
            _isReady.value = true
            true
        } catch (e: Exception) {
            _error.value = "Error opening ebook: ${e.message}"
            false
        }
    }

    override fun closePublication() {
        currentFilePath = null
        _isReady.value = false
        _error.value = null
    }

    /**
     * Gets the file path of the currently open publication.
     * This can be used by iOS-specific reader implementations.
     *
     * @return The file path, or null if no publication is open
     */
    fun getCurrentFilePath(): String? = currentFilePath
}

