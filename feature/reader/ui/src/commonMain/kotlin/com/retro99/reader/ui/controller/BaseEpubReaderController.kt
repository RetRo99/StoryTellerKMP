package com.retro99.reader.ui.controller

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Base implementation of [EpubReaderController] that provides shared state management.
 *
 * This abstract class handles the common state management logic for both Android and iOS
 * implementations, including:
 * - Ready state tracking
 * - Error state tracking
 * - Helper methods for state mutations
 *
 * Platform-specific implementations should extend this class and implement the
 * publication lifecycle methods.
 */
abstract class BaseEpubReaderController : EpubReaderController {

    protected val _isReady = MutableStateFlow(false)
    override val isReady: StateFlow<Boolean> = _isReady.asStateFlow()

    protected val _error = MutableStateFlow<String?>(null)
    override val error: StateFlow<String?> = _error.asStateFlow()

    /**
     * Resets all state to initial values.
     * Should be called when closing a publication.
     */
    protected fun resetState() {
        _isReady.value = false
        _error.value = null
    }

    /**
     * Sets an error state with the given message.
     * Automatically sets isReady to false.
     */
    protected fun setError(message: String) {
        _error.value = message
        _isReady.value = false
    }

    /**
     * Sets the ready state to true and clears any error.
     */
    protected fun setReady() {
        _isReady.value = true
        _error.value = null
    }
}

