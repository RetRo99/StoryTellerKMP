package org.retro99.storyteller.navigation

import androidx.lifecycle.viewModelScope
import com.retro99.auth.domain.usecase.CheckAuthStateUseCase
import com.retro99.auth.domain.usecase.LogoutUseCase
import com.retro99.base.ui.BaseViewModel
import kotlinx.coroutines.launch
import org.koin.core.annotation.KoinViewModel

@KoinViewModel
class RootNavigationViewModel(
    private val checkAuthStateUseCase: CheckAuthStateUseCase,
    private val logoutUseCase: LogoutUseCase,
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
        viewModelScope.launch {
            logoutUseCase()
            updateState { state ->
                state.copy(backStack = listOf(RootDestination.Login))
            }
        }
    }
}

