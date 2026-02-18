package com.retro99.settings.ui.servers

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import com.retro99.base.ui.BaseScreen
import com.retro99.base.ui.IntentDispatcher
import com.retro99.server.api.ServerAuthState
import com.retro99.server.api.ServerType
import com.retro99.settings.ui.servers.model.ServerWithStatusUiModel
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun ServerManagementScreen(
    modifier: Modifier = Modifier,
    viewModel: ServerManagementViewModel = koinViewModel(),
) {
    BaseScreen(
        modifier = modifier,
        viewModel = viewModel,
    ) { viewState, intentDispatcher ->
        ServerManagementScreenContent(
            viewState = viewState,
            intentDispatcher = intentDispatcher,
            modifier = modifier,
        )
    }
}

@Composable
private fun ServerManagementScreenContent(
    viewState: ServerManagementViewState,
    intentDispatcher: IntentDispatcher<ServerManagementIntent>,
    modifier: Modifier = Modifier,
) {
    Scaffold(
        modifier = modifier.fillMaxSize(),
        floatingActionButton = {
            FloatingActionButton(
                onClick = { intentDispatcher(ServerManagementIntent.OnAddServerClick) },
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add Server")
            }
        },
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
        ) {
            Text(
                text = "Server Management",
                style = MaterialTheme.typography.headlineMedium,
            )

            Spacer(modifier = Modifier.height(16.dp))

            if (viewState.isLoading) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center,
                ) {
                    CircularProgressIndicator()
                }
            } else if (viewState.servers.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center,
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "No servers configured",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Tap + to add a server",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                        )
                    }
                }
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    items(viewState.servers, key = { it.server.id }) { serverWithStatus ->
                        ServerListItem(
                            serverWithStatus = serverWithStatus,
                            onServerClick = {
                                intentDispatcher(ServerManagementIntent.OnServerClick(serverWithStatus.server.id))
                            },
                            onLogoutClick = {
                                intentDispatcher(ServerManagementIntent.OnLogoutClick(serverWithStatus.server.id))
                            },
                            onRemoveClick = {
                                intentDispatcher(ServerManagementIntent.OnRemoveClick(serverWithStatus.server.id))
                            },
                        )
                    }
                }
            }
        }

        if (viewState.showAddServerDialog) {
            AddServerDialog(
                isAdding = viewState.isAddingServer,
                error = viewState.addServerError,
                validationResult = viewState.validationResult,
                onDismiss = { intentDispatcher(ServerManagementIntent.OnDismissAddServerDialog) },
                onValidate = { url, type ->
                    intentDispatcher(ServerManagementIntent.OnValidateServer(url, type))
                },
                onAdd = { name, type, url, username, password ->
                    intentDispatcher(ServerManagementIntent.OnAddServer(name, type, url, username, password))
                },
            )
        }
    }
}

@Composable
private fun ServerListItem(
    serverWithStatus: ServerWithStatusUiModel,
    onServerClick: () -> Unit,
    onLogoutClick: () -> Unit,
    onRemoveClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val server = serverWithStatus.server
    val authState = serverWithStatus.authState

    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onServerClick),
        colors = CardDefaults.cardColors(
            containerColor = if (serverWithStatus.isActive) {
                MaterialTheme.colorScheme.primaryContainer
            } else {
                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
            },
        ),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = server.name,
                        style = MaterialTheme.typography.titleMedium,
                    )
                    if (serverWithStatus.isActive) {
                        Spacer(modifier = Modifier.width(8.dp))
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = "Active",
                            modifier = Modifier.size(16.dp),
                            tint = MaterialTheme.colorScheme.primary,
                        )
                    }
                }
                Text(
                    text = server.baseUrl,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = authState.toDisplayString(),
                    style = MaterialTheme.typography.labelSmall,
                    color = authState.toColor(),
                )
            }

            if (authState is ServerAuthState.Authenticated) {
                IconButton(onClick = onLogoutClick) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Logout",
                    )
                }
            }

            IconButton(onClick = onRemoveClick) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = "Remove",
                    tint = MaterialTheme.colorScheme.error,
                )
            }
        }
    }
}

@Composable
private fun ServerAuthState.toDisplayString(): String = when (this) {
    is ServerAuthState.Authenticated -> "Logged in as $username"
    is ServerAuthState.NotAuthenticated -> "Not logged in"
    is ServerAuthState.TokenExpired -> "Session expired"
    is ServerAuthState.AuthenticationFailed -> "Login failed"
}

@Composable
private fun ServerAuthState.toColor() = when (this) {
    is ServerAuthState.Authenticated -> MaterialTheme.colorScheme.primary
    is ServerAuthState.NotAuthenticated -> MaterialTheme.colorScheme.onSurfaceVariant
    is ServerAuthState.TokenExpired -> MaterialTheme.colorScheme.error
    is ServerAuthState.AuthenticationFailed -> MaterialTheme.colorScheme.error
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AddServerDialog(
    isAdding: Boolean,
    error: String?,
    validationResult: ServerValidationUiResult?,
    onDismiss: () -> Unit,
    onValidate: (url: String, type: ServerType) -> Unit,
    onAdd: (name: String, type: ServerType, url: String, username: String, password: String) -> Unit,
) {
    var name by remember { mutableStateOf("") }
    var url by remember { mutableStateOf("") }
    var username by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var selectedType by remember { mutableStateOf(ServerType.Storyteller) }
    var typeDropdownExpanded by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add Server") },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Server Name") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                )

                ExposedDropdownMenuBox(
                    expanded = typeDropdownExpanded,
                    onExpandedChange = { typeDropdownExpanded = it },
                ) {
                    OutlinedTextField(
                        value = selectedType.name,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Server Type") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = typeDropdownExpanded) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor(MenuAnchorType.PrimaryNotEditable),
                    )
                    ExposedDropdownMenu(
                        expanded = typeDropdownExpanded,
                        onDismissRequest = { typeDropdownExpanded = false },
                    ) {
                        ServerType.entries.forEach { type ->
                            DropdownMenuItem(
                                text = { Text(type.name) },
                                onClick = {
                                    selectedType = type
                                    typeDropdownExpanded = false
                                },
                            )
                        }
                    }
                }

                OutlinedTextField(
                    value = url,
                    onValueChange = { url = it },
                    label = { Text("Server URL") },
                    placeholder = { Text("https://example.com") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    trailingIcon = {
                        when (validationResult) {
                            is ServerValidationUiResult.Validating -> {
                                CircularProgressIndicator(modifier = Modifier.size(20.dp))
                            }
                            is ServerValidationUiResult.Valid -> {
                                Icon(Icons.Default.Check, "Valid", tint = MaterialTheme.colorScheme.primary)
                            }
                            is ServerValidationUiResult.Invalid -> {
                                Icon(Icons.Default.Close, "Invalid", tint = MaterialTheme.colorScheme.error)
                            }
                            null -> {}
                        }
                    },
                )

                if (validationResult is ServerValidationUiResult.Invalid) {
                    Text(
                        text = validationResult.message,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.error,
                    )
                }

                TextButton(
                    onClick = { onValidate(url, selectedType) },
                    enabled = url.isNotBlank() && validationResult !is ServerValidationUiResult.Validating,
                ) {
                    Text("Validate Server")
                }

                OutlinedTextField(
                    value = username,
                    onValueChange = { username = it },
                    label = { Text("Username") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                )

                OutlinedTextField(
                    value = password,
                    onValueChange = { password = it },
                    label = { Text("Password") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    visualTransformation = PasswordVisualTransformation(),
                )

                if (error != null) {
                    Text(
                        text = error,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.error,
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { onAdd(name, selectedType, url, username, password) },
                enabled = name.isNotBlank() && url.isNotBlank() && username.isNotBlank() && password.isNotBlank() && !isAdding,
            ) {
                if (isAdding) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(16.dp),
                        color = MaterialTheme.colorScheme.onPrimary,
                    )
                } else {
                    Text("Add & Login")
                }
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        },
    )
}

