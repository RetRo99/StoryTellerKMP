package com.retro99.statistics.ui.model

import com.retro99.base.ui.compose.TextWrapper
import com.retro99.books.domain.model.BookType
import com.retro99.statistics.domain.model.BookReadingStatsDomainModel
import com.retro99.statistics.domain.model.ReadingStatisticsDomainModel
import com.retro99.translations.StringRes
import resources.translations.statistics_day_number
import resources.translations.statistics_hours_minutes
import resources.translations.statistics_minutes

data class ReadingStatisticsUiModel(
    val totalReadingTimeFormatted: TextWrapper,
    val todayReadingTimeFormatted: TextWrapper,
    val weekReadingTimeFormatted: TextWrapper,
    val monthReadingTimeFormatted: TextWrapper,
    val totalSessions: Long,
    val totalBooksRead: Long,
    val currentStreak: Int,
    val longestStreak: Int,
    val dailyReadingTime: List<DailyReadingTimeUiModel>,
    val mostReadBooks: List<BookReadingStatsUiModel>,
    val readingTimeByType: Map<BookType, Long>,
)

data class DailyReadingTimeUiModel(
    val date: TextWrapper,
    val readingTimeMs: Long,
    val readingTimeFormatted: TextWrapper,
)

data class BookReadingStatsUiModel(
    val bookUuid: String,
    val bookTitle: String,
    val totalTimeMs: Long,
    val totalTimeFormatted: TextWrapper,
    val sessionCount: Long,
)

fun ReadingStatisticsDomainModel.toUiModel(): ReadingStatisticsUiModel {
    return ReadingStatisticsUiModel(
        totalReadingTimeFormatted = formatDuration(totalReadingTimeMs),
        todayReadingTimeFormatted = formatDuration(todayReadingTimeMs),
        weekReadingTimeFormatted = formatDuration(weekReadingTimeMs),
        monthReadingTimeFormatted = formatDuration(monthReadingTimeMs),
        totalSessions = totalSessions,
        totalBooksRead = totalBooksRead,
        currentStreak = currentStreak,
        longestStreak = longestStreak,
        dailyReadingTime = dailyReadingTime.map { daily ->
            DailyReadingTimeUiModel(
                date = formatDate(daily.dayStart),
                readingTimeMs = daily.totalDurationMs,
                readingTimeFormatted = formatDuration(daily.totalDurationMs),
            )
        },
        mostReadBooks = mostReadBooks.map { book ->
            BookReadingStatsUiModel(
                bookUuid = book.bookUuid,
                bookTitle = book.bookTitle,
                totalTimeMs = book.totalDurationMs,
                totalTimeFormatted = formatDuration(book.totalDurationMs),
                sessionCount = book.sessionCount,
            )
        },
        readingTimeByType = readingTimeByType,
    )
}

private fun formatDuration(ms: Long): TextWrapper {
    val totalSeconds = ms / 1000
    val hours = totalSeconds / 3600
    val minutes = (totalSeconds % 3600) / 60

    return when {
        hours > 0 -> TextWrapper.Resource(StringRes.statistics_hours_minutes, hours, minutes)
        minutes > 0 -> TextWrapper.Resource(StringRes.statistics_minutes, minutes)
        else -> TextWrapper.Resource(StringRes.statistics_minutes, 0)
    }
}

private fun formatDate(timestamp: Long): TextWrapper {
    val dayNumber = timestamp / (24 * 60 * 60 * 1000)
    return TextWrapper.Resource(StringRes.statistics_day_number, dayNumber)
}

fun BookReadingStatsDomainModel.toBookUiModel(): BookReadingStatsUiModel {
    return BookReadingStatsUiModel(
        bookUuid = bookUuid,
        bookTitle = bookTitle,
        totalTimeMs = totalDurationMs,
        totalTimeFormatted = formatDuration(totalDurationMs),
        sessionCount = sessionCount,
    )
}

