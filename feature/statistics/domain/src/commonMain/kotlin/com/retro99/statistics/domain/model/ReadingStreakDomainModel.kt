package com.retro99.statistics.domain.model

/**
 * Represents reading streak information.
 */
data class ReadingStreakDomainModel(
    val currentStreak: Int,
    val longestStreak: Int,
    val lastReadingDay: Long?,
)

