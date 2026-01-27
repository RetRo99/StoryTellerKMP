package com.retro99.login.ui.navigation

import com.retro99.base.ui.BaseViewModel
import org.koin.core.annotation.KoinViewModel
import org.koin.core.annotation.Named
import org.koin.core.annotation.Provided

@KoinViewModel
class LoginNavigationViewModel(
    @Provided @Named("isDebug") private val isDebug: Boolean,
) : BaseViewModel<LoginNavigationState, LoginNavigationIntent>() {

    override val initialState = LoginNavigationState(isDebug = isDebug)

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

