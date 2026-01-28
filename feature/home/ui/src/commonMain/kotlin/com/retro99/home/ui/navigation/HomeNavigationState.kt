package com.retro99.home.ui.navigation

data class HomeNavigationState(
    val backStack: List<HomeDestination> = listOf(HomeDestination.BooksList),
)

