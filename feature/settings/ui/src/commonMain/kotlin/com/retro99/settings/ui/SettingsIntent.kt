package com.retro99.settings.ui

import com.retro99.base.ui.BaseIntent

sealed interface SettingsIntent : BaseIntent {
    data object OnLogoutClicked : SettingsIntent
}

