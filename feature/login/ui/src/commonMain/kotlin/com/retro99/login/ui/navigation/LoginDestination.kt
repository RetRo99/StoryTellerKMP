package com.retro99.login.ui.navigation

import kotlinx.serialization.Serializable

sealed interface LoginDestination {

    @Serializable
    data object Welcome : LoginDestination

    @Serializable
    data object SignIn : LoginDestination

}