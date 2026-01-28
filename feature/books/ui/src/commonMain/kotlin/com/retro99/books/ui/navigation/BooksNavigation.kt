package com.retro99.books.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.navigation3.rememberViewModelStoreNavEntryDecorator
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberSaveableStateHolderNavEntryDecorator
import androidx.navigation3.ui.NavDisplay
import com.retro99.base.ui.BaseScreen
import com.retro99.books.ui.list.BooksListScreen
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun BooksNavigation(
    modifier: Modifier = Modifier,
    viewModel: BooksNavigationViewModel = koinViewModel(),
) {
    BaseScreen(viewModel = viewModel) { state, intentDispatcher ->
        NavDisplay(
            backStack = state.backStack,
            onBack = {
                intentDispatcher(BooksNavigationIntent.OnBackClicked)
            },
            modifier = modifier,
            entryDecorators = listOf(
                rememberSaveableStateHolderNavEntryDecorator(),
                rememberViewModelStoreNavEntryDecorator(),
            ),
            entryProvider = entryProvider {
                entry<BooksDestination.List> {
                    BooksListScreen()
                }

                entry<BooksDestination.Detail> { destination ->
                    // TODO: Add BookDetailScreen
                    // BookDetailScreen(bookUuid = destination.bookUuid)
                }
            },
        )
    }
}

