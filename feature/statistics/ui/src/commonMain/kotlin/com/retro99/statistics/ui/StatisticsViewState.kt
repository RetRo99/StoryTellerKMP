package com.retro99.statistics.ui

import com.retro99.base.result.AppError
import com.retro99.statistics.ui.model.ReadingStatisticsUiModel

data class StatisticsViewState(
    val statistics: ReadingStatisticsUiModel? = null,
    val isLoading: Boolean = true,
    val error: AppError? = null,
)

