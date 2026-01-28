package com.retro99.books.ui.navigation

data class BooksNavigationState(
    val backStack: List<BooksDestination> = listOf(BooksDestination.List),
)

