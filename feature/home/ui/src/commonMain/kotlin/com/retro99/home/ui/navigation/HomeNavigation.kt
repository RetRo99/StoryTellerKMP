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
                        onNavigateToBookDetail = { book ->
                            intentDispatcher(
                                HomeNavigationIntent.NavigateTo(
                                    HomeDestination.BookDetail(book),
                                ),
                            )
                        },
                    )
                }

                entry<HomeDestination.BookDetail> { destination ->
                    BookDetailScreen(
                        book = destination.book,
                        onNavigateToReader = { bookUuid, ebookFilePath, href, type, progression, position, totalProgression ->
                            intentDispatcher(
                                HomeNavigationIntent.NavigateTo(
                                    HomeDestination.Reader(
                                        bookUuid = bookUuid,
                                        ebookFilePath = ebookFilePath,
                                        initialLocatorHref = href,
                                        initialLocatorType = type,
                                        initialLocatorProgression = progression,
                                        initialLocatorPosition = position,
                                        initialLocatorTotalProgression = totalProgression,
                                    ),
                                ),
                            )
                        },
                    )
                }

                entry<HomeDestination.Reader> { destination ->
                    ReaderScreen(
                        bookUuid = destination.bookUuid,
                        ebookFilePath = destination.ebookFilePath,
                        initialLocatorHref = destination.initialLocatorHref,
                        initialLocatorType = destination.initialLocatorType,
                        initialLocatorProgression = destination.initialLocatorProgression,
                        initialLocatorPosition = destination.initialLocatorPosition,
                        initialLocatorTotalProgression = destination.initialLocatorTotalProgression,
                        onClose = { intentDispatcher(HomeNavigationIntent.OnBackClicked) },
                    )
                }
            },
        )
    }
}

