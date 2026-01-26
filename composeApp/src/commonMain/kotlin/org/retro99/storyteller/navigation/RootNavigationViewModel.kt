package org.retro99.storyteller.navigation

import androidx.lifecycle.viewModelScope
import com.retro99.base.ui.BaseViewModel
import com.retro99.login.domain.usecase.CheckAuthStateUseCase
import kotlinx.coroutines.launch
import org.koin.android.annotation.KoinViewModel

@KoinViewModel
class RootNavigationViewModel(
    private val checkAuthStateUseCase: CheckAuthStateUseCase,
) : BaseViewModel<RootNavigationState, RootNavigationIntent>() {

    override val initialState = RootNavigationState()

    init {
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
            val isLoggedIn = checkAuthStateUseCase()
            val destination = if (isLoggedIn) {
                RootDestination.Home
            } else {
                RootDestination.Login
            }
            updateState { state ->
                state.copy(backStack = listOf(destination))
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

