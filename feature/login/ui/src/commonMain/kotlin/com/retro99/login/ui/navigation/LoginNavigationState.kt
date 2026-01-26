package com.retro99.login.ui.navigation

data class LoginNavigationState(
    val backStack: List<LoginDestination> = listOf(LoginDestination.Welcome),
    val isDebug: Boolean = false,
)

