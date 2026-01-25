package org.retro99.storyteller.navigation

import androidx.lifecycle.viewModelScope
import com.retro99.base.ui.BaseIntent
import com.retro99.base.ui.BaseViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.koin.core.annotation.Single

data class RootNavigationState(
    val backStack: List<RootDestination> = listOf(RootDestination.Splash)
)

sealed interface RootNavigationIntent : BaseIntent {
    data object OnLoginSuccess : RootNavigationIntent
    data object OnLogout : RootNavigationIntent
}

@Single
class RootNavigationViewModel  : BaseViewModel<RootNavigationState, RootNavigationIntent>() {

    override val initialState = RootNavigationState()

    init {
        setState(initialState)
        checkAuthState()
    }

    override fun onIntent(intent: RootNavigationIntent) {
        when (intent) {
            RootNavigationIntent.OnLoginSuccess -> handleLoginSuccess()
            RootNavigationIntent.OnLogout -> handleLogout()
        }
    }

    private fun checkAuthState() {
        viewModelScope.launch {
            // TODO check if logged in
            delay(4000)
            updateState { state ->
                state.copy(backStack = listOf(RootDestination.Home))
            }
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

