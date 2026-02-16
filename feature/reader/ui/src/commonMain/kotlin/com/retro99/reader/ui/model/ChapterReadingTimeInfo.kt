package com.retro99.reader.ui.model

/**
 * Information about the estimated reading time for the current chapter.
 *
 * @param remainingMinutes The estimated minutes remaining to finish the chapter
 * @param remainingWords The number of words remaining in the chapter
 * @param totalWords The total number of words in the chapter
 */
data class ChapterReadingTimeInfo(
    val remainingMinutes: Int,
    val remainingWords: Int,
    val totalWords: Int,
)

