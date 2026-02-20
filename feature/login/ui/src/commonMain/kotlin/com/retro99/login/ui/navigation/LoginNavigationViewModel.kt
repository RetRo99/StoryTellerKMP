package com.retro99.login.ui.navigation

import com.retro99.base.ui.BaseViewModel
import com.retro99.login.domain.usecase.SkipLoginUseCase
import org.koin.core.annotation.KoinViewModel
import org.koin.core.annotation.Named
import org.koin.core.annotation.Provided

@KoinViewModel
class LoginNavigationViewModel(
    startAtLogin: Boolean,
    @Provided @Named("isDebug") isDebug: Boolean,
    @Provided private val skipLoginUseCase: SkipLoginUseCase,
) : BaseViewModel<LoginNavigationState, LoginNavigationIntent>(
    LoginNavigationState(
        backStack = if (startAtLogin) {
            listOf(LoginDestination.Login)
        } else {
            listOf(LoginDestination.Welcome)
        },
        isDebug = isDebug,
    ),
) {

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

            LoginNavigationIntent.OnSkipLoginClicked -> {
                skipLoginUseCase()
                updateState { state ->
                    state.copy(skipLoginComplete = true)
                }
            }
        }
    }
}

