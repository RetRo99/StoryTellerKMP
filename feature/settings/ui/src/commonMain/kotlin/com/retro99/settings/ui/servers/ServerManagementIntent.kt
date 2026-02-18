package com.retro99.settings.ui.servers

import com.retro99.base.ui.BaseIntent
import com.retro99.server.api.ServerType

/**
 * Intents for the server management screen.
 */
sealed interface ServerManagementIntent : BaseIntent {
    // Server list actions
    data class OnServerClick(val serverId: String) : ServerManagementIntent
    data class OnLoginClick(val serverId: String) : ServerManagementIntent
    data class OnLogoutClick(val serverId: String) : ServerManagementIntent
    data class OnRemoveClick(val serverId: String) : ServerManagementIntent

    // Add server dialog
    data object OnAddServerClick : ServerManagementIntent
    data object OnDismissAddServerDialog : ServerManagementIntent
    data class OnValidateServer(
        val url: String,
        val serverType: ServerType,
    ) : ServerManagementIntent

    data class OnAddServer(
        val name: String,
        val type: ServerType,
        val url: String,
        val username: String,
        val password: String,
    ) : ServerManagementIntent
}

