package com.retro99.login.ui.login

import androidx.compose.foundation.text.input.TextFieldState
import com.retro99.base.ui.BaseViewModel
import org.koin.android.annotation.KoinViewModel
import org.koin.core.annotation.InjectedParam

@KoinViewModel
class LoginViewModel(
    @InjectedParam private val onSignInSuccess: () -> Unit,
    @InjectedParam private val onBackClick: () -> Unit,
) : BaseViewModel<LoginViewState, LoginIntent>() {

    val emailState = TextFieldState()
    val passwordState = TextFieldState()

    override val initialState = LoginViewState()

    override fun onIntent(intent: LoginIntent) {
        when (intent) {
            LoginIntent.OnSignInClicked -> handleSignInClicked()
            LoginIntent.OnBackClicked -> onBackClick()
        }
    }

    private fun handleSignInClicked() {
        // TODO: Implement actual sign in logic
        updateState { it.copy(isLoading = true) }
        onSignInSuccess()
    }
}

