package com.retro99.settings.ui.servers

import com.retro99.settings.ui.servers.model.ServerWithStatusUiModel

data class ServerManagementViewState(
    val isLoading: Boolean = true,
    val servers: List<ServerWithStatusUiModel> = emptyList(),
)
