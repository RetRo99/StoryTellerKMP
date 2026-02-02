package com.retro99.home.ui.navigation

import kotlinx.serialization.Serializable

sealed interface HomeDestination {

    @Serializable
    data object BooksList : HomeDestination

    @Serializable
    data class BookDetail(val bookUuid: String) : HomeDestination

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

