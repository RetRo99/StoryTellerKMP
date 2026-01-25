package org.retro99.storyteller.navigation

import kotlinx.serialization.Serializable

sealed interface RootDestination {

    @Serializable
    data object Splash : RootDestination

    @Serializable
    data object Login : RootDestination

    @Serializable
    data object Home : RootDestination
}

