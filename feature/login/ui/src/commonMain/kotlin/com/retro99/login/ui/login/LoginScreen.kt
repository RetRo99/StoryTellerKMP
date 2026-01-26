package com.retro99.login.ui.login

import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.material.icons.automirrored.filled.HelpOutline
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.runtime.derivedStateOf
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
import com.retro99.base.ui.BaseScreen
import com.retro99.base.ui.IntentDispatcher
import kotlinx.coroutines.launch
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf

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
    ) { _, intentDispatcher ->
        LoginScreenContent(
            urlState = viewModel.urlState,
            usernameState = viewModel.usernameState,
            passwordState = viewModel.passwordState,
            intentDispatcher = intentDispatcher,
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
    intentDispatcher: IntentDispatcher<LoginIntent>,
    modifier: Modifier = Modifier,
) {
    var passwordVisible by remember { mutableStateOf(false) }

    val isSignInEnabled by remember {
        derivedStateOf {
            urlState.text.isNotBlank() &&
                    usernameState.text.isNotBlank() &&
                    passwordState.text.isNotBlank()
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Text(
            text = "Sign In",
            style = MaterialTheme.typography.headlineMedium,
        )

        Spacer(modifier = Modifier.height(32.dp))

        OutlinedTextField(
            state = urlState,
            label = { Text("StoryTeller URL") },
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
            label = { Text("Username") },
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
            label = { Text("Password") },
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

        Spacer(modifier = Modifier.height(32.dp))

        Button(
            onClick = { intentDispatcher(LoginIntent.OnSignInClicked) },
            modifier = Modifier.fillMaxWidth(),
            enabled = isSignInEnabled,
        ) {
            Text("Sign In")
        }

        Spacer(modifier = Modifier.height(16.dp))

        TextButton(onClick = { intentDispatcher(LoginIntent.OnBackClicked) }) {
            Text("Back")
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
                "Hide password"
            } else {
                "Show password"
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
                title = { Text("StoryTeller URL") },
                action = {
                    TextButton(onClick = { scope.launch { tooltipState.dismiss() } }) {
                        Text("Got it")
                    }
                },
            ) {
                Text(
                    "Enter the full URL for your Storyteller server instance, " +
                            "including the scheme (http:// or https://).\n\n" +
                            "This may look like a local IP address and port, such as:\n" +
                            "http://192.168.1.12:8001\n\n" +
                            "Or a domain name, such as:\n" +
                            "https://yourdomain.com"
                )
            }
        },
        state = tooltipState,
    ) {
        IconButton(onClick = { scope.launch { tooltipState.show() } }) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.HelpOutline,
                contentDescription = "URL info",
            )
        }
    }
}
