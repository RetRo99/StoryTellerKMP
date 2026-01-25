package org.retro99.storyteller.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.ui.NavDisplay
import com.retro99.base.ui.BaseScreen
import com.retro99.home.ui.navigation.HomeNavigation
import com.retro99.login.ui.navigation.LoginNavigation
import org.koin.compose.viewmodel.koinViewModel
import org.retro99.storyteller.splash.SplashScreen

@Composable
fun RootNavigation(
    modifier: Modifier = Modifier,
    viewModel: RootNavigationViewModel = koinViewModel(),
) {
    BaseScreen(viewModel = viewModel) { state, intentDispatcher ->
        NavDisplay(
            backStack = state.backStack,
            onBack = {
                // Don't allow back navigation from root destinations
                // This prevents going back to Splash or Login after logging in
            },
            modifier = modifier,
            entryProvider = entryProvider {
                entry<RootDestination.Splash> {
                    SplashScreen()
                }

                entry<RootDestination.Login> {
                    LoginNavigation(
                        onLoginSuccess = {
                            intentDispatcher(RootNavigationIntent.OnLoginSuccess)
                        }
                    )
                }

                entry<RootDestination.Home> {
                    HomeNavigation(
                        onLogout = {
                            intentDispatcher(RootNavigationIntent.OnLogout)
                        }
                    )
                }
            }
        )
    }
}

