package com.retro99.login.ui.navigation

import com.retro99.base.ui.BaseIntent

sealed interface LoginNavigationIntent : BaseIntent {
    data object OnBackClicked : LoginNavigationIntent
    data class NavigateTo(val destination: LoginDestination) : LoginNavigationIntent
    data object OnSkipLoginClicked : LoginNavigationIntent
}

