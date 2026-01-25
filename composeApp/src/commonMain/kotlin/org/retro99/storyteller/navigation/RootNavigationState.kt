package org.retro99.storyteller.navigation

data class RootNavigationState(
    val backStack: List<RootDestination> = listOf(RootDestination.Splash)
)

