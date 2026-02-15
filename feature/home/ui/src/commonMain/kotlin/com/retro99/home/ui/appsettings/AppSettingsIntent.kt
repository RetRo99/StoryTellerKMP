package com.retro99.home.ui.appsettings

import com.retro99.base.ui.BaseIntent

sealed interface AppSettingsIntent : BaseIntent {
    data class OnLoggingToggled(val enabled: Boolean) : AppSettingsIntent
    data class OnOpenLastBookToggled(val enabled: Boolean) : AppSettingsIntent
    data object OnShareLogsClicked : AppSettingsIntent
    data object OnClearLogsClicked : AppSettingsIntent
    data object OnLogsClearedMessageShown : AppSettingsIntent
    data object OnNoLogsMessageShown : AppSettingsIntent
}

