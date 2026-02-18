package com.retro99.settings.domain

interface SettingsRepository {

    /**
     * Logout from a specific server or all servers.
     * @param serverId If provided, logout from that specific server. If null, logout from all servers.
     */
    suspend fun logout(serverId: String? = null)
}

