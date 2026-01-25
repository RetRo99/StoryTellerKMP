package com.retro99.home.ui.navigation

import kotlinx.serialization.Serializable

sealed interface HomeDestination {

    @Serializable
    data object Dashboard : HomeDestination

}

