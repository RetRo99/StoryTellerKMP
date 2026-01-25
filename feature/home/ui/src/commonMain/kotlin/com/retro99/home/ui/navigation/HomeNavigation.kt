package com.retro99.home.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.ui.NavDisplay
import com.retro99.home.ui.HomeScreen

@Composable
fun HomeNavigation(
    onLogout: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val backStack = remember { mutableStateListOf<HomeDestination>(HomeDestination.Dashboard) }

    NavDisplay(
        backStack = backStack,
        onBack = { backStack.removeLastOrNull() },
        modifier = modifier,
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

