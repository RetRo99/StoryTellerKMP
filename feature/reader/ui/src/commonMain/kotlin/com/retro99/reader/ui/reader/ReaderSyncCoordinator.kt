package com.retro99.reader.ui.reader

import com.retro99.reader.ui.navigator.AudioController
import com.retro99.reader.ui.navigator.BookController
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach

/**
 * Coordinates synchronization between book navigation and audio playback.
 *
 * Keeps the ViewModel focused on UI state by centralizing cross-controller wiring:
 * - Book locator changes -> audio chapter preparation
 * - Audio locator changes -> book highlight updates
 */
class ReaderSyncCoordinator(
    private val bookController: BookController,
    private val audioController: AudioController,
) : AutoCloseable {

    private var bookToAudioJob: Job? = null
    private var audioToBookJob: Job? = null

    fun start(scope: CoroutineScope) {
        if (bookToAudioJob != null || audioToBookJob != null) return

        bookToAudioJob = bookController.currentLocator
            .onEach { locator ->
                audioController.onBookLocationChanged(locator)
            }
            .launchIn(scope)

        audioToBookJob = audioController.currentAudioLocator
            .filterNotNull()
            .onEach { locator ->
                bookController.applyHighlight(locator)
            }
            .launchIn(scope)
    }

    override fun close() {
        bookToAudioJob?.cancel()
        audioToBookJob?.cancel()
        bookToAudioJob = null
        audioToBookJob = null
    }
}