package com.retro99.login.ui.login

import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.runtime.snapshotFlow
import androidx.lifecycle.viewModelScope
import com.github.michaelbull.result.fold
import com.retro99.analytics.api.Analytics
import com.retro99.analytics.api.AuthAnalyticsEvent
import com.retro99.base.result.log
import com.retro99.base.server.ServerType
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
    @Provided private val analytics: Analytics,
    @InjectedParam private val onSignInSuccess: () -> Unit,
    @InjectedParam private val onBackClick: () -> Unit,
) : BaseViewModel<LoginViewState, LoginIntent>(LoginViewState()) {

    val urlState = TextFieldState(initialText = "https://")
    val usernameState = TextFieldState()
    val passwordState = TextFieldState()

    init {
        observeTextFieldChanges()
        updateFormState(
            url = urlState.text.toString(),
            username = usernameState.text.toString(),
            password = passwordState.text.toString(),
        )
    }

    private fun observeTextFieldChanges() {
        snapshotFlow {
            Triple(
                urlState.text.toString(),
                usernameState.text.toString(),
                passwordState.text.toString(),
            )
        }.onEach { (url, username, password) ->
            updateFormState(url, username, password)
        }.launchIn(viewModelScope)
    }

    private fun updateFormState(
        url: String,
        username: String,
        password: String,
    ) {
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
                isOAuthSignInEnabled = isValidServerUrl(url) && !currentState.isLoading,
            )
        }
    }

    override fun onIntent(intent: LoginIntent) {
        when (intent) {
            LoginIntent.OnSignInClicked -> handleSignInClicked()
            LoginIntent.OnOAuthSignInClicked -> handleOAuthSignInClicked()
            LoginIntent.OnBackClicked -> onBackClick()
        }
    }

    private fun handleSignInClicked() {
        val url = urlState.text.toString().trim()
        // TODO: Allow user to select server type in the future
        val serverType = ServerType.Storyteller

        // Log login attempt (hash URL for privacy)
        analytics.logEvent(
            AuthAnalyticsEvent.LoginAttempted(serverUrlHash = url.hashCode().toString())
        )

        updateState {
            it.copy(
                isLoading = true,
                isOAuthInProgress = false,
                isSignInEnabled = false,
                isOAuthSignInEnabled = false,
                loginError = null,
            )
        }

        viewModelScope.launch {
            val username = usernameState.text.toString()
            val password = passwordState.text.toString()

            loginUseCase(serverType, url, username, password).fold(
                success = {
                    analytics.setUserId(username.hashCode().toString())
                    analytics.logEvent(AuthAnalyticsEvent.LoginSucceeded)
                    onSignInSuccess()
                },
                failure = { error ->
                    analytics.logEvent(
                        AuthAnalyticsEvent.LoginFailed(
                            errorType = error::class.simpleName ?: "unknown",
                        )
                    )
                    error.log(analytics, "LoginViewModel: Failed to login")
                    updateAfterLoginFailure(error.message)
                },
            )
        }
    }

    private fun handleOAuthSignInClicked() {
        val url = urlState.text.toString().trim()
        val serverType = ServerType.Storyteller

        analytics.logEvent(
            AuthAnalyticsEvent.LoginAttempted(serverUrlHash = url.hashCode().toString())
        )

        updateState {
            it.copy(
                isLoading = true,
                isOAuthInProgress = true,
                isSignInEnabled = false,
                isOAuthSignInEnabled = false,
                loginError = null,
            )
        }

        viewModelScope.launch {
            loginUseCase.withOAuth(serverType, url).fold(
                success = {
                    analytics.logEvent(AuthAnalyticsEvent.LoginSucceeded)
                    onSignInSuccess()
                },
                failure = { error ->
                    analytics.logEvent(
                        AuthAnalyticsEvent.LoginFailed(
                            errorType = error::class.simpleName ?: "unknown",
                        )
                    )
                    error.log(analytics, "LoginViewModel: Failed to login with OAuth")
                    updateAfterLoginFailure(error.message)
                },
            )
        }
    }

    private fun updateAfterLoginFailure(errorMessage: String?) {
        updateState {
            it.copy(
                isLoading = false,
                isOAuthInProgress = false,
                loginError = errorMessage,
            )
        }
        updateFormState(
            url = urlState.text.toString(),
            username = usernameState.text.toString(),
            password = passwordState.text.toString(),
        )
    }

    // Validation functions - return null if valid, error message string if invalid
    private fun validateUrl(url: String): String? {
        val trimmedUrl = url.trim()
        if (trimmedUrl.isBlank() || trimmedUrl == "https://" || trimmedUrl == "http://") {
            return null
        }
        return if (isValidServerUrl(trimmedUrl)) null else "Enter a valid http(s) URL"
    }

    private fun validateUsername(username: String): String? {
        return null
    }

    private fun validatePassword(password: String): String? {
        return null
    }

    private fun isValidServerUrl(url: String): Boolean {
        val trimmedUrl = url.trim()
        val schemeSeparator = trimmedUrl.indexOf("://")
        if (schemeSeparator <= 0) return false

        val scheme = trimmedUrl.substring(0, schemeSeparator).lowercase()
        if (scheme != "http" && scheme != "https") return false

        val authority = trimmedUrl
            .substring(schemeSeparator + 3)
            .substringBefore('/')
            .substringBefore('?')
            .substringBefore('#')

        if (authority.isBlank()) return false

        val host = when {
            authority.startsWith('[') -> authority.substringAfter('[').substringBefore(']')
            authority.count { it == ':' } == 1 -> authority.substringBefore(':')
            else -> authority
        }

        return host.isNotBlank()
    }
}
