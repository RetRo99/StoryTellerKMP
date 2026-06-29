package com.retro99.settings.ui.servers

import androidx.lifecycle.viewModelScope
import com.github.michaelbull.result.onFailure
import com.github.michaelbull.result.onSuccess
import com.retro99.base.ui.BaseViewModel
import com.retro99.server.api.ServerAuthState
import com.retro99.server.api.ServerAuthenticatorFactory
import com.retro99.server.api.ServerRegistry
import com.retro99.server.api.ServerType
import com.retro99.settings.ui.servers.model.ServerWithStatusUiModel
import com.retro99.settings.ui.servers.model.toUiModel
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import org.koin.core.annotation.KoinViewModel
import org.koin.core.annotation.Provided

@KoinViewModel
class ServerManagementViewModel(
    @Provided private val serverRegistry: ServerRegistry,
    @Provided private val authenticatorFactory: ServerAuthenticatorFactory,
) : BaseViewModel<ServerManagementViewState, ServerManagementIntent>(ServerManagementViewState()) {

    init {
        observeServers()
    }

    override fun onIntent(intent: ServerManagementIntent) {
        when (intent) {
            is ServerManagementIntent.OnLoginClick -> onLoginClick(intent.serverId)
            is ServerManagementIntent.OnLogoutClick -> onLogoutClick(intent.serverId)
            is ServerManagementIntent.OnRemoveClick -> onRemoveClick(intent.serverId)
            is ServerManagementIntent.OnAddServerClick -> showAddServerDialog()
            is ServerManagementIntent.OnDismissAddServerDialog -> dismissAddServerDialog()
            is ServerManagementIntent.OnValidateServer -> validateServer(intent.url, intent.serverType)
            is ServerManagementIntent.OnAddServer -> addServer(
                intent.name,
                intent.type,
                intent.url,
                intent.username,
                intent.password,
            )
        }
    }

    private fun observeServers() {
        combine(
            serverRegistry.observeAllServers(),
            serverRegistry.observeAllAuthStates(),
        ) { servers, authStates ->
            servers.map { server ->
                ServerWithStatusUiModel(
                    server = server.toUiModel(),
                    authState = authStates[server.id] ?: ServerAuthState.NotAuthenticated(server.id),
                )
            }
        }
            .onEach { serversWithStatus ->
                updateState {
                    it.copy(
                        isLoading = false,
                        servers = serversWithStatus,
                    )
                }
            }
            .launchIn(viewModelScope)
    }

    private fun onLoginClick(serverId: String) {
        // TODO: Navigate to login screen or show login dialog
        // For now, this would be handled by navigation
    }

    private fun onLogoutClick(serverId: String) {
        viewModelScope.launch {
            serverRegistry.clearCredentials(serverId)
        }
    }

    private fun onRemoveClick(serverId: String) {
        viewModelScope.launch {
            serverRegistry.removeServer(serverId)
        }
    }

    private fun showAddServerDialog() {
        updateState {
            it.copy(
                showAddServerDialog = true,
                addServerError = null,
                validationResult = null,
            )
        }
    }

    private fun dismissAddServerDialog() {
        updateState {
            it.copy(
                showAddServerDialog = false,
                isAddingServer = false,
                addServerError = null,
                validationResult = null,
            )
        }
    }

    private fun validateServer(url: String, serverType: ServerType) {
        updateState { it.copy(validationResult = ServerValidationUiResult.Validating) }

        viewModelScope.launch {
            val authenticator = authenticatorFactory.create(serverType)
            authenticator.validateServer(url)
                .onSuccess { result ->
                    updateState {
                        it.copy(
                            validationResult = if (result.isValid) {
                                ServerValidationUiResult.Valid
                            } else {
                                ServerValidationUiResult.Invalid(
                                    result.errorMessage ?: "Invalid server",
                                )
                            },
                        )
                    }
                }
                .onFailure { error ->
                    updateState {
                        it.copy(
                            validationResult = ServerValidationUiResult.Invalid(
                                error.message ?: "Validation failed",
                            ),
                        )
                    }
                }
        }
    }

    private fun addServer(
        name: String,
        type: ServerType,
        url: String,
        username: String,
        password: String,
    ) {
        updateState { it.copy(isAddingServer = true, addServerError = null) }

        viewModelScope.launch {
            // First add the server
            val server = serverRegistry.addServer(name, type, url)

            // Then try to login
            val authenticator = authenticatorFactory.create(type)
            authenticator.login(url, username, password)
                .onSuccess { credentials ->
                    serverRegistry.saveCredentials(credentials.copy(serverId = server.id))
                    dismissAddServerDialog()
                }
                .onFailure { error ->
                    // Server was added but login failed
                    updateState {
                        it.copy(
                            isAddingServer = false,
                            addServerError = error.message ?: "Login failed",
                        )
                    }
                }
        }
    }
}

