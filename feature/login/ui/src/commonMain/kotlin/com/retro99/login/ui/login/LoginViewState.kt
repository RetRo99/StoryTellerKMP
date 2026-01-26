package com.retro99.login.ui.login

data class LoginViewState(
    val isLoading: Boolean = false,
    val isSignInEnabled: Boolean = false,
    val urlError: String? = null,
    val usernameError: String? = null,
    val passwordError: String? = null,
    val loginError: String? = null,
)
