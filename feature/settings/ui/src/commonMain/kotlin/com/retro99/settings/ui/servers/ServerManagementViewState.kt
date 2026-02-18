package com.retro99.settings.ui.servers

import com.retro99.settings.ui.servers.model.ServerWithStatusUiModel

/**
 * View state for the server management screen.
 */
data class ServerManagementViewState(
    val isLoading: Boolean = true,
    val servers: List<ServerWithStatusUiModel> = emptyList(),
    val showAddServerDialog: Boolean = false,
    val isAddingServer: Boolean = false,
    val addServerError: String? = null,
    val validationResult: ServerValidationUiResult? = null,
)

/**
 * Result of server validation during add server flow.
 */
sealed class ServerValidationUiResult {
    data object Validating : ServerValidationUiResult()
    data object Valid : ServerValidationUiResult()
    data class Invalid(val message: String) : ServerValidationUiResult()
}

