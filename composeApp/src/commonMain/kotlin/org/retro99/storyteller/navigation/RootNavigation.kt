package org.retro99.storyteller.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.ui.NavDisplay
import com.retro99.login.ui.navigation.LoginNavigation
import org.retro99.storyteller.home.HomeScreen
import org.retro99.storyteller.splash.SplashScreen

@Composable
fun RootNavigation(
    modifier: Modifier = Modifier,
) {
    // Root back stack - starts with Splash
    val backStack = remember { mutableStateListOf<RootDestination>(RootDestination.Splash) }
    
    NavDisplay(
        backStack = backStack,
        onBack = { 
            // Don't allow back navigation from root destinations
            // This prevents going back to Splash or Login after logging in
        },
        modifier = modifier,
        entryProvider = entryProvider {
            entry<RootDestination.Splash> {
                SplashScreen(
                    onSplashComplete = { isLoggedIn ->
                        backStack.clear()
                        if (isLoggedIn) {
                            backStack.add(RootDestination.Home)
                        } else {
                            backStack.add(RootDestination.Login)
                        }
                    }
                )
            }
            
            entry<RootDestination.Login> {
                LoginNavigation(
                    onLoginSuccess = {
                        backStack.clear()
                        backStack.add(RootDestination.Home)
                    }
                )
            }
            
            entry<RootDestination.Home> {
                // TODO: Replace with HomeNavigation when home feature is implemented
                HomeScreen(
                    onLogout = {
                        backStack.clear()
                        backStack.add(RootDestination.Login)
                    }
                )
            }
        }
    )
}

