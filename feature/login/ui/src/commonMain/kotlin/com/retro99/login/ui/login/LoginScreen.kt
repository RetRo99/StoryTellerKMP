package com.retro99.login.ui.login

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.input.TextFieldLineLimits
import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.foundation.text.input.TextObfuscationMode
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.HelpOutline
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedSecureTextField
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RichTooltip
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TooltipAnchorPosition
import androidx.compose.material3.TooltipBox
import androidx.compose.material3.TooltipDefaults
import androidx.compose.material3.rememberTooltipState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.retro99.base.server.ServerType
import com.retro99.base.ui.BaseScreen
import com.retro99.base.ui.IntentDispatcher
import com.retro99.translations.StringRes
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf
import resources.translations.general_back
import resources.translations.login_hide_password
import resources.translations.login_oauth_sign_in_button
import resources.translations.login_oauth_waiting_button
import resources.translations.login_oauth_waiting_message
import resources.translations.login_password_label
import resources.translations.login_server_type_label
import resources.translations.login_show_password
import resources.translations.login_sign_in_button
import resources.translations.login_title
import resources.translations.login_url_info
import resources.translations.login_url_label
import resources.translations.login_url_tooltip_description
import resources.translations.login_url_tooltip_dismiss
import resources.translations.login_url_tooltip_title
import resources.translations.login_username_label

@Composable
fun LoginScreen(
    onSignInSuccess: () -> Unit,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: LoginViewModel = koinViewModel { parametersOf(onSignInSuccess, onBackClick) },
) {
    BaseScreen(
        modifier = modifier.imePadding(),
        viewModel = viewModel,
    ) { viewState, intentDispatcher ->
        LoginScreenContent(
            urlState = viewModel.urlState,
            usernameState = viewModel.usernameState,
            passwordState = viewModel.passwordState,
            isSignInEnabled = viewState.isSignInEnabled,
            isOAuthSignInEnabled = viewState.isOAuthSignInEnabled,
            isOAuthInProgress = viewState.isOAuthInProgress,
            isOAuthVisible = viewState.isOAuthVisible,
            selectedServerType = viewState.selectedServerType,
            loginError = viewState.loginError,
            intentDispatcher = intentDispatcher,
            onBackClick = onBackClick,
            modifier = modifier,
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun LoginScreenContent(
    urlState: TextFieldState,
    usernameState: TextFieldState,
    passwordState: TextFieldState,
    isSignInEnabled: Boolean,
    isOAuthSignInEnabled: Boolean,
    isOAuthInProgress: Boolean,
    isOAuthVisible: Boolean,
    selectedServerType: ServerType,
    loginError: String?,
    intentDispatcher: IntentDispatcher<LoginIntent>,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var passwordVisible by remember { mutableStateOf(false) }
    var serverTypeExpanded by remember { mutableStateOf(false) }

    Box(
        modifier = modifier.fillMaxSize(),
    ) {
        IconButton(
            onClick = onBackClick,
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(8.dp),
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = null,
            )
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            Text(
                text = stringResource(StringRes.login_title),
                style = MaterialTheme.typography.headlineMedium,
            )

            Spacer(modifier = Modifier.height(32.dp))

            ExposedDropdownMenuBox(
                expanded = serverTypeExpanded,
                onExpandedChange = { serverTypeExpanded = it },
            ) {
                OutlinedTextField(
                    value = selectedServerType.displayName,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text(stringResource(StringRes.login_server_type_label)) },
                    trailingIcon = {
                        ExposedDropdownMenuDefaults.TrailingIcon(expanded = serverTypeExpanded)
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .menuAnchor(MenuAnchorType.PrimaryNotEditable),
                )
                ExposedDropdownMenu(
                    expanded = serverTypeExpanded,
                    onDismissRequest = { serverTypeExpanded = false },
                ) {
                    ServerType.entries.filter { it != ServerType.Local }.forEach { type ->
                        DropdownMenuItem(
                            text = { Text(type.displayName) },
                            onClick = {
                                intentDispatcher(LoginIntent.OnServerTypeSelected(type))
                                serverTypeExpanded = false
                            },
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                state = urlState,
                label = { Text(stringResource(StringRes.login_url_label)) },
                modifier = Modifier.fillMaxWidth(),
                lineLimits = TextFieldLineLimits.SingleLine,
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Uri,
                    imeAction = ImeAction.Next,
                ),
                trailingIcon = { UrlInfoTooltip() },
            )

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                state = usernameState,
                label = { Text(stringResource(StringRes.login_username_label)) },
                modifier = Modifier.fillMaxWidth(),
                lineLimits = TextFieldLineLimits.SingleLine,
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Text,
                    imeAction = ImeAction.Next,
                ),
            )

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedSecureTextField(
                state = passwordState,
                label = { Text(stringResource(StringRes.login_password_label)) },
                modifier = Modifier.fillMaxWidth(),
                textObfuscationMode = if (passwordVisible) {
                    TextObfuscationMode.Visible
                } else {
                    TextObfuscationMode.Hidden
                },
                keyboardOptions = KeyboardOptions(
                    autoCorrectEnabled = false,
                    imeAction = ImeAction.Done,
                    keyboardType = KeyboardType.Password,
                ),
                onKeyboardAction = {
                    if (isSignInEnabled) {
                        intentDispatcher(LoginIntent.OnSignInClicked)
                    }
                },
                trailingIcon = {
                    PasswordVisibilityToggle(
                        isVisible = passwordVisible,
                        onToggle = { passwordVisible = !passwordVisible },
                    )
                },
            )

            Spacer(modifier = Modifier.height(16.dp))

            if (loginError != null) {
                Text(
                    text = loginError,
                    maxLines = 2,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.fillMaxWidth(),
                )
                Spacer(modifier = Modifier.height(16.dp))
            } else {
                Spacer(modifier = Modifier.height(16.dp))
            }

            Button(
                onClick = { intentDispatcher(LoginIntent.OnSignInClicked) },
                modifier = Modifier.fillMaxWidth(),
                enabled = isSignInEnabled,
            ) {
                Text(stringResource(StringRes.login_sign_in_button))
            }

            Spacer(modifier = Modifier.height(8.dp))

            if (isOAuthVisible) {
                OutlinedButton(
                    onClick = { intentDispatcher(LoginIntent.OnOAuthSignInClicked) },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = isOAuthSignInEnabled,
                ) {
                    Text(
                        stringResource(
                            if (isOAuthInProgress) {
                                StringRes.login_oauth_waiting_button
                            } else {
                                StringRes.login_oauth_sign_in_button
                            }
                        )
                    )
                }

                if (isOAuthInProgress) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = stringResource(StringRes.login_oauth_waiting_message),
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.fillMaxWidth(),
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            TextButton(onClick = { intentDispatcher(LoginIntent.OnBackClicked) }) {
                Text(stringResource(StringRes.general_back))
            }
        }
    }
}

@Composable
private fun PasswordVisibilityToggle(
    isVisible: Boolean,
    onToggle: () -> Unit,
) {
    IconButton(onClick = onToggle) {
        Icon(
            imageVector = if (isVisible) {
                Icons.Filled.VisibilityOff
            } else {
                Icons.Filled.Visibility
            },
            contentDescription = if (isVisible) {
                stringResource(StringRes.login_hide_password)
            } else {
                stringResource(StringRes.login_show_password)
            },
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun UrlInfoTooltip() {
    val tooltipState = rememberTooltipState(isPersistent = true)
    val scope = rememberCoroutineScope()
    TooltipBox(
        positionProvider = TooltipDefaults.rememberTooltipPositionProvider(
            TooltipAnchorPosition.Above
        ),
        tooltip = {
            RichTooltip(
                title = { Text(stringResource(StringRes.login_url_tooltip_title)) },
                action = {
                    TextButton(onClick = { scope.launch { tooltipState.dismiss() } }) {
                        Text(stringResource(StringRes.login_url_tooltip_dismiss))
                    }
                },
            ) {
                Text(stringResource(StringRes.login_url_tooltip_description))
            }
        },
        state = tooltipState,
    ) {
        IconButton(
            onClick = {
                scope.launch {
                    if (tooltipState.isVisible) {
                        tooltipState.dismiss()
                    } else {
                        tooltipState.show()
                    }
                }
            }
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.HelpOutline,
                contentDescription = stringResource(StringRes.login_url_info),
            )
        }
    }
}
