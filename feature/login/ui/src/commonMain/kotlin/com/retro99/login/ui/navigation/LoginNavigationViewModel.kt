package com.retro99.login.ui.navigation

import com.retro99.base.ui.BaseViewModel
import org.koin.android.annotation.KoinViewModel

@KoinViewModel
class LoginNavigationViewModel : BaseViewModel<LoginNavigationState, LoginNavigationIntent>() {

    override val initialState = LoginNavigationState()

    override fun onIntent(intent: LoginNavigationIntent) {
        when (intent) {
            LoginNavigationIntent.OnBackClicked -> {
                updateState { state ->
                    state.copy(backStack = state.backStack.dropLast(1))
                }
            }

            is LoginNavigationIntent.NavigateTo -> {
                updateState { state ->
                    state.copy(backStack = state.backStack + intent.destination)
                }
            }
        }
    }
}

