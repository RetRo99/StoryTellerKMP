package com.retro99.home.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.CollectionsBookmark
// import androidx.compose.material.icons.filled.Person
import androidx.compose.ui.graphics.vector.ImageVector
import com.retro99.translations.StringRes
import org.jetbrains.compose.resources.StringResource
// import resources.translations.home_tab_authors
import resources.translations.home_tab_books
import resources.translations.home_tab_series

/**
 * Represents the tabs in the Home bottom navigation bar.
 *
 * Each tab has its own back stack and maintains its navigation state
 * independently of other tabs.
 *
 * @property icon The icon to display in the navigation bar item.
 * @property labelRes The string resource for the tab label.
 * @property startDestination The initial destination for this tab's back stack.
 */
enum class HomeTab(
    val icon: ImageVector,
    val labelRes: StringResource,
    val startDestination: HomeDestination,
) {
    Books(
        icon = Icons.AutoMirrored.Filled.MenuBook,
        labelRes = StringRes.home_tab_books,
        startDestination = HomeDestination.BooksList,
    ),
    Series(
        icon = Icons.Filled.CollectionsBookmark,
        labelRes = StringRes.home_tab_series,
        startDestination = HomeDestination.SeriesList,
    ),
//    Authors(
//        icon = Icons.Filled.Person,
//        labelRes = StringRes.home_tab_authors,
//        startDestination = HomeDestination.AuthorsList,
//    ),
    ;

    companion object {
        val DEFAULT = Books
    }
}

