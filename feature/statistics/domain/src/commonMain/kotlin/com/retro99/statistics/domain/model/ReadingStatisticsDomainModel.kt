package com.retro99.statistics.domain.model

import com.retro99.books.domain.model.BookType

/**
 * Aggregated reading statistics for display in the dashboard.
 */
data class ReadingStatisticsDomainModel(
    val totalReadingTimeMs: Long,
    val todayReadingTimeMs: Long,
    val weekReadingTimeMs: Long,
    val monthReadingTimeMs: Long,
    val totalSessions: Long,
    val totalBooksRead: Long,
    val currentStreak: Int,
    val longestStreak: Int,
    val dailyReadingTime: List<DailyReadingTimeDomainModel>,
    val mostReadBooks: List<BookReadingStatsDomainModel>,
    val readingTimeByType: Map<BookType, Long>,
)

