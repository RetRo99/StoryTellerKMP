package com.retro99.login.ui.login

import com.retro99.base.server.ServerType

data class LoginViewState(
    val isLoading: Boolean = false,
    val isOAuthInProgress: Boolean = false,
    val isSignInEnabled: Boolean = false,
    val isOAuthSignInEnabled: Boolean = false,
    val selectedServerType: ServerType = ServerType.Storyteller,
    val urlError: String? = null,
    val usernameError: String? = null,
    val passwordError: String? = null,
    val loginError: String? = null,
) {
    val isOAuthVisible: Boolean
        get() = selectedServerType == ServerType.Storyteller
}
