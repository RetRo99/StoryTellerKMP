package com.retro99.home.ui.appsettings

data class AppSettingsViewState(
    val isLoggingEnabled: Boolean = false,
    val openLastBookOnLaunch: Boolean = false,
    val showLogsClearedMessage: Boolean = false,
    val showNoLogsMessage: Boolean = false,
    val hasAuthenticatedRemoteServers: Boolean = false,
)

