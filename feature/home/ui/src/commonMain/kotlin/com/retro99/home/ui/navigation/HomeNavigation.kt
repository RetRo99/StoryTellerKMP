package com.retro99.home.ui.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.navigation3.runtime.entryProvider
import com.retro99.books.ui.detail.BookDetailScreen
import com.retro99.books.ui.list.BooksListScreen
import com.retro99.books.ui.series.detail.SeriesDetailScreen
import com.retro99.home.ui.appsettings.AppSettingsScreen
import com.retro99.home.ui.series.SeriesListScreen
import com.retro99.reader.ui.reader.ReaderScreen
import com.retro99.settings.ui.SettingsScreen
import com.retro99.statistics.ui.StatisticsScreen
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun HomeNavigation(
    onLogout: () -> Unit,
    onNavigateToLogin: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: HomeNavigationViewModel = koinViewModel(),
) {
    // Navigation state managed by Nav3's rememberNavBackStack for automatic persistence
    val navigationState = rememberHomeNavigationState()

    // UI state from ViewModel (currently reading, bubble position)
    val uiState by viewModel.viewState.collectAsState()

    // Intent dispatcher for navigation actions
    val intentDispatcher: (HomeNavigationIntent) -> Unit = { viewModel.onIntent(it) }

    // Handle navigation events from ViewModel
    LaunchedEffect(viewModel) {
        viewModel.navigationEvents.collect { event ->
            when (event) {
                is HomeNavigationEvent.NavigateTo -> {
                    navigationState.navigateTo(event.destination)
                }
                is HomeNavigationEvent.SwitchTab -> {
                    navigationState.switchTab(event.tab)
                }
                HomeNavigationEvent.GoBack -> {
                    navigationState.goBack()
                }
                is HomeNavigationEvent.NavigateToReaderReplacing -> {
                    navigationState.switchTab(event.tab)
                    navigationState.navigateToReplacing(
                        HomeDestination.Reader(
                            serverId = event.serverId,
                            bookUuid = event.bookUuid,
                            bookType = event.bookType,
                        ),
                        event.tab,
                    )
                }
            }
        }
    }

    // Reset navigation when user profile changes
    LaunchedEffect(viewModel) {
        viewModel.userProfileChanged.collect {
            navigationState.resetAllStacks()
        }
    }

    val currentDestination = navigationState.currentDestination
    val showBottomBar = (currentDestination as? BottomBarDestination)?.showBottomBar != false
    val isInReader = currentDestination is HomeDestination.Reader
    val currentlyReading = uiState.currentlyReading

    // Track when leaving reader to refresh currently reading state
    val wasInReader = remember { mutableStateOf(false) }
    LaunchedEffect(isInReader) {
        if (wasInReader.value && !isInReader) {
            intentDispatcher(HomeNavigationIntent.RefreshCurrentlyReading)
        }
        wasInReader.value = isInReader
    }

    Box(modifier = modifier.fillMaxSize()) {
        Scaffold(
            bottomBar = {
                if (showBottomBar) {
                    HomeBottomNavigationBar(
                        currentTab = navigationState.currentTab,
                        onTabSelected = { tab ->
                            intentDispatcher(HomeNavigationIntent.SwitchTab(tab))
                        },
                    )
                }
            },
        ) { paddingValues ->
            BottomSheetNavDisplay(
                backStack = navigationState.currentBackStack,
                onBack = { intentDispatcher(HomeNavigationIntent.GoBack) },
                modifier = Modifier.padding(paddingValues),
                entryProvider = entryProvider {
                    entry<HomeDestination.BooksList> {
                        BooksListScreen(
                            onNavigateToBookDetail = { book ->
                                intentDispatcher(
                                    HomeNavigationIntent.NavigateTo(
                                        HomeDestination.BookDetail(
                                            serverId = book.serverId,
                                            bookUuid = book.uuid,
                                        )
                                    )
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
                                        )
                                    )
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
                                        HomeDestination.BookDetail(
                                            serverId = book.serverId,
                                            bookUuid = book.uuid,
                                        )
                                    )
                                )
                            },
                            onBack = { intentDispatcher(HomeNavigationIntent.GoBack) },
                        )
                    }

                    entry<HomeDestination.BookDetail> { destination ->
                        BookDetailScreen(
                            serverId = destination.serverId,
                            bookUuid = destination.bookUuid,
                            onNavigateToReader = { serverId, bookUuid, bookType ->
                                intentDispatcher(
                                    HomeNavigationIntent.NavigateTo(
                                        HomeDestination.Reader(
                                            serverId = serverId,
                                            bookUuid = bookUuid,
                                            bookType = bookType,
                                        )
                                    )
                                )
                            },
                            onNavigateToSeriesDetail = { seriesUuid, seriesName ->
                                intentDispatcher(
                                    HomeNavigationIntent.NavigateTo(
                                        HomeDestination.SeriesDetail(
                                            seriesUuid = seriesUuid,
                                            seriesName = seriesName,
                                        )
                                    )
                                )
                            },
                            onBack = { intentDispatcher(HomeNavigationIntent.GoBack) },
                        )
                    }

                    entry<HomeDestination.Reader> { destination ->
                        ReaderScreen(
                            serverId = destination.serverId,
                            bookUuid = destination.bookUuid,
                            bookType = destination.bookType,
                            onClose = { intentDispatcher(HomeNavigationIntent.GoBack) },
                            onSettingsClick = {
                                intentDispatcher(
                                    HomeNavigationIntent.NavigateTo(HomeDestination.Settings)
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
                            onLogin = onNavigateToLogin,
                            onNavigateToStatistics = {
                                intentDispatcher(
                                    HomeNavigationIntent.NavigateTo(HomeDestination.Statistics)
                                )
                            },
                        )
                    }

                    entry<HomeDestination.Statistics> {
                        StatisticsScreen(
                            onBack = { intentDispatcher(HomeNavigationIntent.GoBack) },
                        )
                    }
                },
            )
        }

        // Draggable floating bubble for Continue Reading
        // Only show when position is loaded (not null) to avoid flicker
        val bubblePosition = uiState.bubblePosition
        if (!isInReader && currentlyReading != null && bubblePosition != null) {
            DraggableFloatingBubble(
                modifier = Modifier.fillMaxSize(),
                initialSide = bubblePosition.toBubbleSide(),
                initialYFraction = bubblePosition.yFraction,
                edgePadding = 16f,
                onPositionChanged = { side, yFraction ->
                    intentDispatcher(HomeNavigationIntent.UpdateBubblePosition(side, yFraction))
                },
            ) {
                ContinueReadingBubble(
                    currentlyReading = currentlyReading,
                    onClick = {
                        intentDispatcher(
                            HomeNavigationIntent.OpenReader(
                                serverId = currentlyReading.serverId,
                                bookUuid = currentlyReading.bookUuid,
                                bookType = currentlyReading.bookType,
                            )
                        )
                    },
                )
            }
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

