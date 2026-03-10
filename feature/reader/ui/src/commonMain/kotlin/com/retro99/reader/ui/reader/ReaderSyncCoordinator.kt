package com.retro99.reader.ui.reader

import com.retro99.reader.ui.di.ReaderScope
import com.retro99.reader.ui.navigator.AudioController
import com.retro99.reader.ui.navigator.BookController
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.drop
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import org.koin.core.annotation.Scope
import org.koin.core.annotation.Scoped

/**
 * Coordinates synchronization between book navigation and audio playback.
 *
 * Keeps the ViewModel focused on UI state by centralizing cross-controller wiring:
 * - Book locator changes -> audio chapter preparation
 * - Audio locator changes -> book highlight updates
 * - Sentence double-tap events -> audio playback from fragment
 * - Chapter audio completion -> auto-play next chapter
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
    private var chapterCompletionJob: Job? = null

    fun start(scope: CoroutineScope) {
        if (bookToAudioJob != null || audioToBookJob != null) return

        bookToAudioJob = bookController.currentLocator
            .onEach { locator ->
                val visibleSentenceId = bookController.getVisibleSentenceId()
                audioController.onBookLocationChanged(locator, visibleSentenceId)
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

        // Handle chapter audio completion - auto-play next chapter
        chapterCompletionJob = audioController.chapterAudioCompleted
            .onEach { completedChapterHref ->
                onChapterAudioCompleted(scope, completedChapterHref)
            }
            .launchIn(scope)
    }

    /**
     * Handles chapter audio completion by navigating to the next chapter
     * and starting playback from the beginning.
     */
    private fun onChapterAudioCompleted(scope: CoroutineScope, completedChapterHref: String) {
        scope.launch {
            // Navigate forward - this will move to the next chapter
            bookController.goToNextPage()

            // Wait for the locator to change to a different chapter
            val newLocator = bookController.currentLocator
                .drop(1) // Skip current value
                .first { it.href != completedChapterHref }

            // Start playback from the beginning of the new chapter
            audioController.playFromFragment(
                fragmentId = newLocator.fragments?.firstOrNull() ?: "",
                chapterHref = newLocator.href,
            )
        }
    }

    override fun close() {
        bookToAudioJob?.cancel()
        audioToBookJob?.cancel()
        doubleTapToAudioJob?.cancel()
        chapterCompletionJob?.cancel()
        bookToAudioJob = null
        audioToBookJob = null
        doubleTapToAudioJob = null
        chapterCompletionJob = null
    }
}