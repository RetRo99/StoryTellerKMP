package com.retro99.books.ui.navigation

import kotlinx.serialization.Serializable

sealed interface BooksDestination {

    @Serializable
    data object List : BooksDestination

    @Serializable
    data class Detail(val bookUuid: String) : BooksDestination
}

