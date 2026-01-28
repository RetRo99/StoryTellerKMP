package com.retro99.books.ui.navigation

import com.retro99.base.ui.BaseIntent

sealed interface BooksNavigationIntent : BaseIntent {
    data object OnBackClicked : BooksNavigationIntent
    data class NavigateTo(val destination: BooksDestination) : BooksNavigationIntent
}

