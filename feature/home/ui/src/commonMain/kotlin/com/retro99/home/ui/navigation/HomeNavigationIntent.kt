package com.retro99.home.ui.navigation

import com.retro99.base.ui.BaseIntent

sealed interface HomeNavigationIntent : BaseIntent {
    data object OnBackClicked : HomeNavigationIntent
    data class NavigateTo(val destination: HomeDestination) : HomeNavigationIntent
}

