package com.retro99.home.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.navigation3.rememberViewModelStoreNavEntryDecorator
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberSaveableStateHolderNavEntryDecorator
import androidx.navigation3.ui.NavDisplay
import com.retro99.base.ui.BaseScreen
import com.retro99.books.ui.detail.BookDetailScreen
import com.retro99.books.ui.list.BooksListScreen
import com.retro99.reader.ui.reader.ReaderScreen
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun HomeNavigation(
    modifier: Modifier = Modifier,
    viewModel: HomeNavigationViewModel = koinViewModel(),
) {
    BaseScreen(viewModel = viewModel) { state, intentDispatcher ->
        NavDisplay(
            backStack = state.backStack,
            onBack = {
                intentDispatcher(HomeNavigationIntent.OnBackClicked)
            },
            modifier = modifier,
            entryDecorators = listOf(
                rememberSaveableStateHolderNavEntryDecorator(),
                rememberViewModelStoreNavEntryDecorator(),
            ),
            entryProvider = entryProvider {
                entry<HomeDestination.BooksList> {
                    BooksListScreen(
                        onNavigateToBookDetail = { bookUuid ->
                            intentDispatcher(
                                HomeNavigationIntent.NavigateTo(
                                    HomeDestination.BookDetail(bookUuid),
                                ),
                            )
                        },
                    )
                }

                entry<HomeDestination.BookDetail> { destination ->
                    BookDetailScreen(
                        bookUuid = destination.bookUuid,
                        onNavigateToReader = { bookUuid, ebookFilePath ->
                            intentDispatcher(
                                HomeNavigationIntent.NavigateTo(
                                    HomeDestination.Reader(bookUuid, ebookFilePath),
                                ),
                            )
                        },
                    )
                }

                entry<HomeDestination.Reader> { destination ->
                    ReaderScreen(
                        bookUuid = destination.bookUuid,
                        ebookFilePath = destination.ebookFilePath,
                        onClose = { intentDispatcher(HomeNavigationIntent.OnBackClicked) },
                    )
                }
            },
        )
    }
}

