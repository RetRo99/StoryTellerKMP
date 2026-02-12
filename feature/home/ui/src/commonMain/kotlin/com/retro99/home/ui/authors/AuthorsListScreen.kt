package com.retro99.home.ui.authors

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.retro99.books.ui.authors.model.AuthorListUiModel
import com.retro99.books.ui.authors.AuthorsListScreen as BooksAuthorsListScreen

/**
 * Authors list screen wrapper for the home module.
 *
 * This screen displays a list of authors that the user can browse.
 * Delegates to the AuthorsListScreen from the books/ui module.
 */
@Composable
fun AuthorsListScreen(
    onNavigateToAuthorDetail: (author: AuthorListUiModel) -> Unit = {},
    modifier: Modifier = Modifier,
) {
    BooksAuthorsListScreen(
        onNavigateToAuthorDetail = onNavigateToAuthorDetail,
        modifier = modifier,
    )
}

