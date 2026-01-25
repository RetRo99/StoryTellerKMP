package com.retro99.home.ui.navigation

import com.retro99.base.ui.BaseIntent

sealed interface HomeNavigationIntent : BaseIntent {
    data object OnLogout : HomeNavigationIntent
    data object OnBackClicked : HomeNavigationIntent
}

