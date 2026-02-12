package com.retro99.reader.ui.reader

import com.retro99.reader.ui.di.ReaderScope
import com.retro99.reader.ui.navigator.AudioController
import com.retro99.reader.ui.navigator.BookController
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import org.koin.core.annotation.Scope
import org.koin.core.annotation.Scoped

/**
 * Coordinates synchronization between book navigation and audio playback.
 *
 * Keeps the ViewModel focused on UI state by centralizing cross-controller wiring:
 * - Book locator changes -> audio chapter preparation
 * - Audio locator changes -> book highlight updates
 * - Sentence double-tap events -> audio playback from fragment
 */
@Scope(ReaderScope::class)
@Scoped
class ReaderSyncCoordinator(
    private val bookController: BookController,
    private val audioController: AudioController,
) : AutoCloseable {

    private var bookToAudioJob: Job? = null
    private var audioToBookJob: Job? = null
    private var doubleTapToAudioJob: Job? = null

    fun start(scope: CoroutineScope) {
        if (bookToAudioJob != null || audioToBookJob != null) return

        bookToAudioJob = bookController.currentLocator
            .onEach { locator ->
                audioController.onBookLocationChanged(locator)
                // Only handle ReadAloud-specific logic for books with media overlays
                if (bookController.hasMediaOverlays) {
                    // Reset playback state when user navigates while not playing
                    // so next play starts from the current visible text position.
                    // AudioController checks if playing internally and ignores if so.
                    audioController.resetPlaybackState()
                    // Update visible sentence and seek bar position
                    updateVisibleSentence()
                }
            }
            .launchIn(scope)

        audioToBookJob = audioController.currentAudioLocator
            .filterNotNull()
            .onEach { audioLocator ->
                bookController.applyHighlightWithPageTurn(
                    locator = audioLocator.locator,
                    sentenceDurationMs = audioLocator.sentenceDurationMs,
                )
            }
            .launchIn(scope)

        // Handle double-tap events on sentences to start audio playback
        doubleTapToAudioJob = bookController.sentenceDoubleTapEvents
            .onEach { event ->
                audioController.playFromFragment(event.fragmentId, event.chapterHref)
            }
            .launchIn(scope)
    }

    /**
     * Updates the audio controller with the current visible sentence.
     *
     * Called when the book location changes. This:
     * 1. Notifies the audio controller of the visible sentence for future playback
     * 2. Updates the seek bar position (only if not playing)
     */
    private suspend fun updateVisibleSentence() {
        val visibleSentenceId = bookController.getVisibleSentenceId()
        audioController.setVisibleSentenceId(visibleSentenceId)
        if (visibleSentenceId != null) {
            audioController.updatePositionForFragment(visibleSentenceId)
        }
    }

    override fun close() {
        bookToAudioJob?.cancel()
        audioToBookJob?.cancel()
        doubleTapToAudioJob?.cancel()
        bookToAudioJob = null
        audioToBookJob = null
        doubleTapToAudioJob = null
    }
}