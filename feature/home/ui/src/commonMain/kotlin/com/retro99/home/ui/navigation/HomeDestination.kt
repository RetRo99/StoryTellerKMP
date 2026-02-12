package com.retro99.home.ui.navigation

import com.retro99.reader.domain.model.BookType
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient

/**
 * Interface for destinations that control bottom navigation bar visibility.
 * Implement this interface and override [showBottomBar] to return `false`
 * for full-screen destinations like readers.
 */
interface BottomBarDestination {
    /**
     * Whether the bottom navigation bar should be visible when this destination is displayed.
     * Default is `true`.
     */
    val showBottomBar: Boolean
        get() = true
}

sealed interface HomeDestination : BottomSheetDestination, BottomBarDestination {

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
    data class AuthorDetail(
        val authorUuid: String,
        val authorName: String,
    ) : HomeDestination

    @Serializable
    data class Reader(
        val bookUuid: String,
        val bookType: BookType,
    ) : HomeDestination {
        @Transient
        override val showBottomBar: Boolean = false
    }

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

