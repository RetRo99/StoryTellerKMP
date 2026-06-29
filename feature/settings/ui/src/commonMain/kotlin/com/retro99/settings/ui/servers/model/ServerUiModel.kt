package com.retro99.settings.ui.servers.model

import com.retro99.server.api.ServerAuthState
import com.retro99.server.api.ServerConfig
import com.retro99.server.api.ServerType

/**
 * UI model representing a server configuration.
 */
data class ServerUiModel(
    val id: String,
    val name: String,
    val type: ServerType,
    val baseUrl: String,
)

/**
 * UI model representing a server with its current authentication state.
 */
data class ServerWithStatusUiModel(
    val server: ServerUiModel,
    val authState: ServerAuthState,
)

/**
 * Represents the fetch status for a server's books.
 */
sealed class ServerFetchStatus {
    abstract val server: ServerUiModel

    data class Loading(override val server: ServerUiModel) : ServerFetchStatus()
    data class Success(override val server: ServerUiModel, val bookCount: Int) : ServerFetchStatus()
    data class NotAuthenticated(override val server: ServerUiModel) : ServerFetchStatus()
    data class Error(override val server: ServerUiModel, val message: String) : ServerFetchStatus()
}

/**
 * Maps a ServerConfig to ServerUiModel.
 */
fun ServerConfig.toUiModel(): ServerUiModel = ServerUiModel(
    id = id,
    name = name,
    type = type,
    baseUrl = baseUrl,
)

