package com.retro99.home.ui.navigation

import com.retro99.reader.domain.model.BookType
import kotlinx.serialization.Serializable

sealed interface HomeDestination {

    @Serializable
    data object BooksList : HomeDestination

    @Serializable
    data class BookDetail(val bookUuid: String) : HomeDestination

    @Serializable
    data class Reader(
        val bookUuid: String,
        val bookType: BookType,
    ) : HomeDestination

    @Serializable
    data object Settings : HomeDestination

}

