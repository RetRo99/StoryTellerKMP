package com.retro99.login.ui.login

import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.runtime.snapshotFlow
import androidx.lifecycle.viewModelScope
import com.github.michaelbull.result.fold
import com.retro99.base.ui.BaseViewModel
import com.retro99.login.domain.usecase.LoginUseCase
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import org.koin.core.annotation.InjectedParam
import org.koin.core.annotation.KoinViewModel
import org.koin.core.annotation.Provided

@KoinViewModel
class LoginViewModel(
    @Provided private val loginUseCase: LoginUseCase,
    @InjectedParam private val onSignInSuccess: () -> Unit,
    @InjectedParam private val onBackClick: () -> Unit,
) : BaseViewModel<LoginViewState, LoginIntent>() {

    val urlState = TextFieldState(initialText = "https://")
    val usernameState = TextFieldState()
    val passwordState = TextFieldState()

    override val initialState = LoginViewState()

    init {
        observeTextFieldChanges()
    }

    private fun observeTextFieldChanges() {
        snapshotFlow {
            Triple(
                urlState.text.toString(),
                usernameState.text.toString(),
                passwordState.text.toString(),
            )
        }.onEach { (url, username, password) ->
            updateState { currentState ->
                val urlError = validateUrl(url)
                val usernameError = validateUsername(username)
                val passwordError = validatePassword(password)

                val allFieldsNotEmpty =
                    url.isNotBlank() && username.isNotBlank() && password.isNotBlank()
                val noErrors =
                    urlError == null && usernameError == null && passwordError == null

                currentState.copy(
                    urlError = urlError,
                    usernameError = usernameError,
                    passwordError = passwordError,
                    isSignInEnabled = allFieldsNotEmpty && noErrors && !currentState.isLoading,
                )
            }
        }.launchIn(viewModelScope)
    }

    override fun onIntent(intent: LoginIntent) {
        when (intent) {
            LoginIntent.OnSignInClicked -> handleSignInClicked()
            LoginIntent.OnBackClicked -> onBackClick()
        }
    }

    private fun handleSignInClicked() {
        updateState { it.copy(isLoading = true, isSignInEnabled = false, loginError = null) }

        viewModelScope.launch {
            val url = urlState.text.toString()
            val username = usernameState.text.toString()
            val password = passwordState.text.toString()

            loginUseCase(url, username, password).fold(
                success = {
                    onSignInSuccess()
                },
                failure = { error ->
                    updateState { state ->
                        state.copy(
                            isLoading = false,
                            isSignInEnabled = true,
                            loginError = error.message,
                        )
                    }
                },
            )
        }
    }

    // Validation functions - return null if valid, error message string if invalid
    private fun validateUrl(url: String): String? {
        return null
    }

    private fun validateUsername(username: String): String? {
        return null
    }

    private fun validatePassword(password: String): String? {
        return null
    }
}
