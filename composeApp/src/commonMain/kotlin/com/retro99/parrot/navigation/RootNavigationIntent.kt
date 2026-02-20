package com.retro99.parrot.navigation

import com.retro99.base.ui.BaseIntent

sealed interface RootNavigationIntent : BaseIntent {
    data object OnLoginSuccess : RootNavigationIntent
    data object OnLogout : RootNavigationIntent
    data object OnLoginClicked : RootNavigationIntent
    data object OnBackFromLogin : RootNavigationIntent
}

