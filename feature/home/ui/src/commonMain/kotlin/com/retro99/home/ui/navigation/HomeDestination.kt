package com.retro99.home.ui.navigation

import kotlinx.serialization.Serializable

sealed interface HomeDestination {

    @Serializable
    data object BooksList : HomeDestination

    @Serializable
    data class BookDetail(val bookUuid: String) : HomeDestination

    @Serializable
    data class Reader(val bookUuid: String) : HomeDestination

}

