package com.retro99.home.ui.navigation

import com.retro99.reader.domain.model.BookType
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient

sealed interface HomeDestination : BottomSheetDestination {

    @Serializable
    data object BooksList : HomeDestination

    @Serializable
    data object SeriesList : HomeDestination

    @Serializable
    data object AuthorsList : HomeDestination

    @Serializable
    data class BookDetail(val bookUuid: String) : HomeDestination

    @Serializable
    data class SeriesDetail(
        val seriesUuid: String,
        val seriesName: String,
    ) : HomeDestination

    @Serializable
    data class Reader(
        val bookUuid: String,
        val bookType: BookType,
    ) : HomeDestination

    @Serializable
    data object Settings : HomeDestination {
        @Transient
        override val isBottomSheet: Boolean = true

        @Transient
        override val bottomSheetConfig: BottomSheetConfig = BottomSheetConfig(
            skipPartiallyExpanded = false,
            fillMaxHeight = false,
        )
    }
}

