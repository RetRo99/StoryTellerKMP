package com.retro99.parrot.navigation

import kotlinx.serialization.Serializable

sealed interface RootDestination {

    @Serializable
    data object Splash : RootDestination

    @Serializable
    data class Login(
        val initial: Boolean,
    ) : RootDestination

    @Serializable
    data object Home : RootDestination
}

