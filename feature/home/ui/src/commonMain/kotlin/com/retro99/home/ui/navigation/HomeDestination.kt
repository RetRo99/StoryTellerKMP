package com.retro99.home.ui.navigation

import com.retro99.books.ui.model.BookUiModel
import kotlinx.serialization.Serializable

sealed interface HomeDestination {

    @Serializable
    data object BooksList : HomeDestination

    @Serializable
    data class BookDetail(val book: BookUiModel) : HomeDestination

    @Serializable
    data class Reader(val book: BookUiModel) : HomeDestination

}

