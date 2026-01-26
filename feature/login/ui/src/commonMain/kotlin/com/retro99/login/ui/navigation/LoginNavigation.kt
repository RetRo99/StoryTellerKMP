package com.retro99.login.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.navigation3.rememberViewModelStoreNavEntryDecorator
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberSaveableStateHolderNavEntryDecorator
import androidx.navigation3.ui.NavDisplay
import com.retro99.base.ui.BaseScreen
import com.retro99.login.ui.login.LoginScreen
import com.retro99.login.ui.welcome.WelcomeScreen
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun LoginNavigation(
    onLoginSuccess: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: LoginNavigationViewModel = koinViewModel(),
) {
    BaseScreen(viewModel = viewModel) { state, intentDispatcher ->
        NavDisplay(
            backStack = state.backStack,
            onBack = {
                intentDispatcher(LoginNavigationIntent.OnBackClicked)
            },
            modifier = modifier,
            entryDecorators = listOf(
                rememberSaveableStateHolderNavEntryDecorator(),
                rememberViewModelStoreNavEntryDecorator(),
            ),
            entryProvider = entryProvider {
                entry<LoginDestination.Welcome> {
                    WelcomeScreen(
                        isDebug = state.isDebug,
                        onSignInClick = {
                            intentDispatcher(LoginNavigationIntent.NavigateTo(LoginDestination.Login))
                        },
                    )
                }

                entry<LoginDestination.Login> {
                    LoginScreen(
                        onSignInSuccess = onLoginSuccess,
                        onBackClick = {
                            intentDispatcher(LoginNavigationIntent.OnBackClicked)
                        },
                    )
                }
            }
        )
    }
}

