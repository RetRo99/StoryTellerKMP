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
                // Update seek bar position when user navigates (only for ReadAloud books)
                // AudioController will check if playing and ignore if so
                if (bookController.hasMediaOverlays) {
                    updateSeekBarForVisibleSentence()
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
     * Updates the seek bar position to reflect the first visible sentence.
     *
     * Called when the book location changes. The audio controller will check
     * if audio is playing and only update the position if not playing.
     */
    private suspend fun updateSeekBarForVisibleSentence() {
        val visibleSentenceId = bookController.getVisibleSentenceId() ?: return
        audioController.updatePositionForFragment(visibleSentenceId)
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