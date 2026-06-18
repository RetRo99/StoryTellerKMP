package com.retro99.login.ui.login

import com.retro99.base.ui.BaseIntent

sealed interface LoginIntent : BaseIntent {
    data object OnSignInClicked : LoginIntent
    data object OnOAuthSignInClicked : LoginIntent
    data object OnBackClicked : LoginIntent
}

