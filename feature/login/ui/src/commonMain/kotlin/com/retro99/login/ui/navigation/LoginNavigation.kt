package com.retro99.login.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.ui.NavDisplay
import com.retro99.login.ui.signin.SignInScreen
import com.retro99.login.ui.welcome.WelcomeScreen


@Composable
fun LoginNavigation(
    onLoginSuccess: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val backStack = remember { mutableStateListOf<LoginDestination>(LoginDestination.Welcome) }
    
    NavDisplay(
        backStack = backStack,
        onBack = { backStack.removeLastOrNull() },
        modifier = modifier,
        entryProvider = entryProvider {
            entry<LoginDestination.Welcome> {
                WelcomeScreen(
                    onSignInClick = { backStack.add(LoginDestination.SignIn) },
                )
            }
            
            entry<LoginDestination.SignIn> {
                SignInScreen(
                    onSignInSuccess = onLoginSuccess,
                    onBackClick = { backStack.removeLastOrNull() },
                )
            }
        }
    )
}

