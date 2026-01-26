package com.retro99.login.ui.login

import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.runtime.snapshotFlow
import androidx.lifecycle.viewModelScope
import com.retro99.base.ui.BaseViewModel
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import org.koin.android.annotation.KoinViewModel
import org.koin.core.annotation.InjectedParam

@KoinViewModel
class LoginViewModel(
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
        // TODO: Implement actual sign in logic
        updateState { it.copy(isLoading = true) }
        onSignInSuccess()
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
