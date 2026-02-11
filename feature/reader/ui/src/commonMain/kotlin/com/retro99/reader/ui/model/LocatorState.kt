package com.retro99.reader.ui.model

/**
 * Represents the current page within a chapter based on the actual viewport display.
 *
 * Unlike the EPUB position (which is based on fixed 1024-character blocks),
 * this represents the actual displayed page that changes based on font size,
 * margins, and viewport dimensions.
 *
 * @param currentPage The current page number within the chapter (1-based)
 * @param totalPages The total number of pages in the current chapter
 */
data class ChapterPageInfo(
    val currentPage: Int,
    val totalPages: Int,
)

/**
 * Common data class representing the current reading position/locator.
 *
 * This is a platform-agnostic representation of the reading position that can be
 * used in the common interface. Platform implementations convert their native
 * locator types to this common model.
 *
 * @param href The href of the current resource
 * @param type The media type of the resource
 * @param title The title of the current section, if available
 * @param progression The progression within the resource (0.0 to 1.0)
 * @param position The position index, if available
 * @param totalProgression The total progression through the publication (0.0 to 1.0)
 * @param fragments The list of fragment identifiers (e.g., sentence IDs)
 * @param chapterPageInfo The current page info within the chapter based on viewport display
 */
data class LocatorState(
    val href: String,
    val type: String,
    val title: String?,
    val progression: Double?,
    val position: Int?,
    val totalProgression: Double?,
    val fragments: List<String>?,
    val chapterPageInfo: ChapterPageInfo? = null,
)

/**
 * Extended locator state for audio playback that includes timing information.
 * Used for coordinating text highlighting with audio playback, including
 * handling sentences that are split across pages.
 *
 * @param locator The base locator state with position information
 * @param sentenceDurationMs The duration of the current sentence in milliseconds.
 *                           Used to calculate when to turn the page for split sentences.
 */
data class AudioLocatorState(
    val locator: LocatorState,
    val sentenceDurationMs: Long,
)

