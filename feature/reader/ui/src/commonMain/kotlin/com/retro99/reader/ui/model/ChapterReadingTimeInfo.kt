package com.retro99.reader.ui.model

/**
 * Information about the word count of the current chapter.
 *
 * @param totalWords The total number of words in the chapter
 */
data class ChapterWordCountInfo(
    val totalWords: Int,
)

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
) {
    companion object {
        /**
         * Calculates the reading time info based on word count and reading progress.
         *
         * @param wordCountInfo The word count information for the chapter
         * @param chapterProgression The current progression through the chapter (0.0 to 1.0)
         * @param wordsPerMinute The reading speed in words per minute
         * @return The calculated reading time info, or null if calculation is not possible
         */
        fun calculate(
            wordCountInfo: ChapterWordCountInfo?,
            chapterProgression: Double?,
            wordsPerMinute: Int,
        ): ChapterReadingTimeInfo? {
            if (wordCountInfo == null || chapterProgression == null || wordsPerMinute <= 0) {
                return null
            }

            val totalWords = wordCountInfo.totalWords
            val remainingFraction = (1.0 - chapterProgression).coerceIn(0.0, 1.0)
            val remainingWords = (totalWords * remainingFraction).toInt()
            val remainingMinutes = (remainingWords.toDouble() / wordsPerMinute).toInt()

            return ChapterReadingTimeInfo(
                remainingMinutes = remainingMinutes,
                remainingWords = remainingWords,
                totalWords = totalWords,
            )
        }
    }
}

