package org.retro99.storyteller.navigation

import com.retro99.base.ui.BaseIntent

sealed interface RootNavigationIntent : BaseIntent {
    data object OnLoginSuccess : RootNavigationIntent
    data object OnLogout : RootNavigationIntent
}

