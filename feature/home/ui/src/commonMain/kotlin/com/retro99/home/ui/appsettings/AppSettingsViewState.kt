package com.retro99.home.ui.appsettings

import com.retro99.user.api.UserProfile

data class AppSettingsViewState(
    val isLoggingEnabled: Boolean = false,
    val openLastBookOnLaunch: Boolean = false,
    val showLogsClearedMessage: Boolean = false,
    val showNoLogsMessage: Boolean = false,
    val hasAuthenticatedRemoteServers: Boolean = false,
    val userProfiles: List<UserProfile> = emptyList(),
    val activeProfile: UserProfile? = null,
    val showAddProfileDialog: Boolean = false,
)

