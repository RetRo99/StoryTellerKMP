package com.retro99.home.ui.navigation

import com.retro99.books.ui.model.BookUiModel
import kotlinx.serialization.Serializable

sealed interface HomeDestination {

    @Serializable
    data object BooksList : HomeDestination

    @Serializable
    data class BookDetail(val book: BookUiModel) : HomeDestination

    @Serializable
    data class Reader(
        val bookUuid: String,
        val ebookFilePath: String,
        val initialLocatorHref: String? = null,
        val initialLocatorType: String? = null,
        val initialLocatorProgression: Double? = null,
        val initialLocatorPosition: Int? = null,
        val initialLocatorTotalProgression: Double? = null,
    ) : HomeDestination

}

