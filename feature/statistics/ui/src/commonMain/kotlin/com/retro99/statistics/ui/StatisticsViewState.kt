package com.retro99.statistics.ui

import com.retro99.base.result.AppError
import com.retro99.statistics.ui.model.BookReadingStatsUiModel
import com.retro99.statistics.ui.model.ReadingStatisticsUiModel
import com.retro99.statistics.domain.model.StatisticsPeriod

data class StatisticsViewState(
    val statistics: ReadingStatisticsUiModel? = null,
    val isLoading: Boolean = true,
    val error: AppError? = null,
    val detailState: StatisticsDetailState? = null,
)

/**
 * State for the statistics detail bottom sheet.
 */
data class StatisticsDetailState(
    val period: StatisticsPeriod,
    val books: List<BookReadingStatsUiModel>,
    val isLoading: Boolean = false,
)

