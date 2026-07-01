package com.retro99.settings.ui.servers

import com.retro99.base.ui.BaseIntent

sealed interface ServerManagementIntent : BaseIntent {
    data class OnLoginClick(val serverId: String) : ServerManagementIntent
    data class OnLogoutClick(val serverId: String) : ServerManagementIntent
    data class OnRemoveClick(val serverId: String) : ServerManagementIntent
    data object OnAddServerClick : ServerManagementIntent
}
