package com.retro99.statistics.domain.model

/**
 * Represents reading streak information.
 *
 * @param currentStreak The number of consecutive days in the current streak
 * @param longestStreak The number of consecutive days in the longest streak
 * @param lastReadingDay The timestamp of the last reading day
 * @param currentStreakDays List of timestamps (start of day) for each day in the current streak
 * @param longestStreakDays List of timestamps (start of day) for each day in the longest streak
 */
data class ReadingStreakDomainModel(
    val currentStreak: Int,
    val longestStreak: Int,
    val lastReadingDay: Long?,
    val currentStreakDays: List<Long> = emptyList(),
    val longestStreakDays: List<Long> = emptyList(),
)

