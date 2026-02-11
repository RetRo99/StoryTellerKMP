package com.retro99.reader.ui.navigator

import com.retro99.reader.ui.model.ChapterPageInfo
import com.retro99.reader.ui.model.LocatorState
import com.retro99.reader.ui.model.PositionUiModel
import com.retro99.reader.ui.model.ReaderSettingsUiModel
import kotlinx.coroutines.flow.Flow

/**
 * Result of checking sentence visibility on the current page.
 * Used for pre-emptive page turn logic during TTS playback.
 *
 * @property visibleFraction The fraction of the sentence visible on the current page (0.0 to 1.0).
 *                           1.0 means fully visible, 0.0 means entirely on next/previous page.
 * @property needsPageTurn True if the sentence is split and a page turn will be needed.
 */
data class SentenceVisibilityResult(
    val visibleFraction: Double,
    val needsPageTurn: Boolean,
) {
    companion object {
        /** Default result when visibility cannot be determined - assume fully visible */
        val FULLY_VISIBLE = SentenceVisibilityResult(
            visibleFraction = 1.0,
            needsPageTurn = false,
        )

        /** Result when element is not found or entirely on next page */
        val HIDDEN = SentenceVisibilityResult(
            visibleFraction = 0.0,
            needsPageTurn = true,
        )
    }
}

interface BookController : AutoCloseable {

    /**
     * Flow of current reading position/locator changes.
     * Emits whenever the user navigates to a new position.
     */
    val currentLocator: Flow<LocatorState>

    /**
     * Navigates to the next page.
     */
    fun goToNextPage()

    /**
     * Navigates to the previous page.
     */
    fun goToPreviousPage()

    /**
     * Navigates to a specific chapter by its href.
     *
     * @param href The href of the chapter to navigate to
     */
    fun goToChapter(href: String)

    /**
     * Applies the given reader settings.
     *
     * @param settings The reader settings to apply
     */
    fun setSettings(settings: ReaderSettingsUiModel)

    /**
     * Navigates to a specific position in the publication.
     *
     * @param position The position to navigate to
     */
    fun goToPosition(position: PositionUiModel)

    /**
     * Applies a highlight decoration to the given locator and handles split sentences.
     *
     * For sentences that are split across pages, this method will:
     * 1. Apply the highlight immediately
     * 2. Schedule a page turn after the visible portion has been read
     *
     * @param locator The locator to highlight
     * @param sentenceDurationMs The duration of the sentence in milliseconds (for timing page turns)
     */
    suspend fun applyHighlightWithPageTurn(
        locator: LocatorState,
        sentenceDurationMs: Long,
    )

    /**
     * Checks the visibility of a sentence element on the current page.
     * Used for pre-emptive page turn logic during TTS playback.
     *
     * @param elementId The ID of the sentence element to check
     * @return The visibility result including visible fraction and whether page turn is needed
     */
    suspend fun checkSentenceVisibility(elementId: String): SentenceVisibilityResult

    /**
     * Gets the current page info within the chapter based on the actual viewport display.
     *
     * Unlike the EPUB position (which is based on fixed 1024-character blocks),
     * this returns the actual displayed page that changes based on font size,
     * margins, and viewport dimensions.
     *
     * @return The current page info, or null if it cannot be determined
     */
    suspend fun getChapterPageInfo(): ChapterPageInfo?
}