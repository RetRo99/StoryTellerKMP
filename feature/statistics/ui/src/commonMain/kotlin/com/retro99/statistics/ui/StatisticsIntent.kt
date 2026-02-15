package com.retro99.statistics.ui

import com.retro99.base.ui.BaseIntent

sealed interface StatisticsIntent : BaseIntent {
    data object OnRefresh : StatisticsIntent
    data object OnBackClicked : StatisticsIntent
}

