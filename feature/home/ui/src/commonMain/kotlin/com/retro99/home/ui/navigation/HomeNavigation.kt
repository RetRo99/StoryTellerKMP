package com.retro99.home.ui.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation3.runtime.entryProvider
import com.retro99.base.ui.BaseScreen
// import com.retro99.books.ui.authors.detail.AuthorDetailScreen
import com.retro99.books.ui.detail.BookDetailScreen
import com.retro99.books.ui.list.BooksListScreen
import com.retro99.books.ui.series.detail.SeriesDetailScreen
import com.retro99.home.ui.appsettings.AppSettingsScreen
// import com.retro99.home.ui.authors.AuthorsListScreen
import com.retro99.home.ui.series.SeriesListScreen
import com.retro99.reader.ui.reader.ReaderScreen
import com.retro99.settings.ui.SettingsScreen
import com.retro99.statistics.ui.StatisticsScreen
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun HomeNavigation(
    onLogout: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: HomeNavigationViewModel = koinViewModel(),
) {
    BaseScreen(viewModel = viewModel) { state, intentDispatcher ->
        val currentDestination = state.currentBackStack.lastOrNull()
        val showBottomBar = (currentDestination as? BottomBarDestination)?.showBottomBar != false

        Scaffold(
            modifier = modifier,
            bottomBar = {
                if (showBottomBar) {
                    HomeBottomNavigationBar(
                        currentTab = state.currentTab,
                        onTabSelected = { tab ->
                            intentDispatcher(HomeNavigationIntent.SwitchTab(tab))
                        },
                    )
                }
            },
        ) { paddingValues ->
            BottomSheetNavDisplay(
                backStack = state.currentBackStack,
                onBack = { intentDispatcher(HomeNavigationIntent.OnBackClicked) },
                modifier = Modifier.padding(paddingValues),
                entryProvider = entryProvider {
                    entry<HomeDestination.BooksList> {
                        BooksListScreen(
                            onNavigateToBookDetail = { book ->
                                intentDispatcher(
                                    HomeNavigationIntent.NavigateTo(
                                        HomeDestination.BookDetail(book.uuid),
                                    ),
                                )
                            },
                        )
                    }

                    entry<HomeDestination.SeriesList> {
                        SeriesListScreen(
                            onNavigateToSeriesDetail = { series ->
                                intentDispatcher(
                                    HomeNavigationIntent.NavigateTo(
                                        HomeDestination.SeriesDetail(
                                            seriesUuid = series.uuid,
                                            seriesName = series.name,
                                        ),
                                    ),
                                )
                            },
                        )
                    }

                    entry<HomeDestination.SeriesDetail> { destination ->
                        SeriesDetailScreen(
                            seriesUuid = destination.seriesUuid,
                            seriesName = destination.seriesName,
                            onNavigateToBookDetail = { book ->
                                intentDispatcher(
                                    HomeNavigationIntent.NavigateTo(
                                        HomeDestination.BookDetail(book.uuid),
                                    ),
                                )
                            },
                            onBack = { intentDispatcher(HomeNavigationIntent.OnBackClicked) },
                        )
                    }

//                    entry<HomeDestination.AuthorsList> {
//                        AuthorsListScreen(
//                            onNavigateToAuthorDetail = { author ->
//                                intentDispatcher(
//                                    HomeNavigationIntent.NavigateTo(
//                                        HomeDestination.AuthorDetail(
//                                            authorUuid = author.uuid,
//                                            authorName = author.name,
//                                        ),
//                                    ),
//                                )
//                            },
//                        )
//                    }
//
//                    entry<HomeDestination.AuthorDetail> { destination ->
//                        AuthorDetailScreen(
//                            authorUuid = destination.authorUuid,
//                            authorName = destination.authorName,
//                            onNavigateToBookDetail = { book ->
//                                intentDispatcher(
//                                    HomeNavigationIntent.NavigateTo(
//                                        HomeDestination.BookDetail(book.uuid),
//                                    ),
//                                )
//                            },
//                            onBack = { intentDispatcher(HomeNavigationIntent.OnBackClicked) },
//                        )
//                    }

                    entry<HomeDestination.BookDetail> { destination ->
                        BookDetailScreen(
                            bookUuid = destination.bookUuid,
                            onNavigateToReader = { bookUuid, bookType ->
                                intentDispatcher(
                                    HomeNavigationIntent.NavigateTo(
                                        HomeDestination.Reader(bookUuid, bookType),
                                    ),
                                )
                            },
                            onBack = { intentDispatcher(HomeNavigationIntent.OnBackClicked) },
                        )
                    }

                    entry<HomeDestination.Reader> { destination ->
                        ReaderScreen(
                            bookUuid = destination.bookUuid,
                            bookType = destination.bookType,
                            onClose = { intentDispatcher(HomeNavigationIntent.OnBackClicked) },
                            onSettingsClick = {
                                intentDispatcher(
                                    HomeNavigationIntent.NavigateTo(HomeDestination.Settings),
                                )
                            },
                        )
                    }

                    entry<HomeDestination.Settings> {
                        SettingsScreen()
                    }

                    entry<HomeDestination.AppSettings> {
                        AppSettingsScreen(
                            onLogout = onLogout,
                            onNavigateToStatistics = {
                                intentDispatcher(
                                    HomeNavigationIntent.NavigateTo(HomeDestination.Statistics),
                                )
                            },
                        )
                    }

                    entry<HomeDestination.Statistics> {
                        StatisticsScreen(
                            onBack = { intentDispatcher(HomeNavigationIntent.OnBackClicked) },
                        )
                    }
                },
            )
        }
    }
}

@Composable
private fun HomeBottomNavigationBar(
    currentTab: HomeTab,
    onTabSelected: (HomeTab) -> Unit,
    modifier: Modifier = Modifier,
) {
    NavigationBar(modifier = modifier) {
        HomeTab.entries.forEach { tab ->
            NavigationBarItem(
                selected = currentTab == tab,
                onClick = { onTabSelected(tab) },
                icon = {
                    Icon(
                        imageVector = tab.icon,
                        contentDescription = stringResource(tab.labelRes),
                    )
                },
                label = {
                    Text(text = stringResource(tab.labelRes))
                },
            )
        }
    }
}

