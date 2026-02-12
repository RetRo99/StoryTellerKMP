package com.retro99.parrot.navigation

data class RootNavigationState(
    val backStack: List<RootDestination> = listOf(RootDestination.Splash),
)

