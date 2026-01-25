package com.retro99.home.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.navigation3.rememberViewModelStoreNavEntryDecorator
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberSaveableStateHolderNavEntryDecorator
import androidx.navigation3.ui.NavDisplay
import com.retro99.base.ui.BaseScreen
import com.retro99.home.ui.HomeScreen
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun HomeNavigation(
    onLogout: () -> Unit,
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
                entry<HomeDestination.Dashboard> {
                    HomeScreen(
                        onLogout = onLogout,
                    )
                }

                // Add more entries here as needed, e.g.:
                // entry<HomeDestination.Profile> {
                //     ProfileScreen(
                //         onBackClick = { backStack.removeLastOrNull() },
                //     )
                // }
            }
        )
    }
}

