package org.retro99.storyteller.navigation

import com.retro99.base.ui.BaseIntent
import com.retro99.base.ui.BaseViewModel
import org.koin.core.annotation.Single

data class RootNavigationState(
    val backStack: List<RootDestination> = listOf(RootDestination.Splash)
)

sealed interface RootNavigationIntent : BaseIntent {
    data class OnSplashComplete(val isLoggedIn: Boolean) : RootNavigationIntent
    data object OnLoginSuccess : RootNavigationIntent
    data object OnLogout : RootNavigationIntent
}

@Single
class RootNavigationViewModel : BaseViewModel<RootNavigationState, RootNavigationIntent>() {

    override val initialState = RootNavigationState()

    init {
        setState(initialState)
    }

    override fun onIntent(intent: RootNavigationIntent) {
        when (intent) {
            is RootNavigationIntent.OnSplashComplete -> handleSplashComplete(intent.isLoggedIn)
            RootNavigationIntent.OnLoginSuccess -> handleLoginSuccess()
            RootNavigationIntent.OnLogout -> handleLogout()
        }
    }

    private fun handleSplashComplete(isLoggedIn: Boolean) {
        updateState { state ->
            val newDestination = if (isLoggedIn) RootDestination.Home else RootDestination.Login
            state.copy(backStack = listOf(newDestination))
        }
    }

    private fun handleLoginSuccess() {
        updateState { state ->
            state.copy(backStack = listOf(RootDestination.Home))
        }
    }

    private fun handleLogout() {
        updateState { state ->
            state.copy(backStack = listOf(RootDestination.Login))
        }
    }
}

