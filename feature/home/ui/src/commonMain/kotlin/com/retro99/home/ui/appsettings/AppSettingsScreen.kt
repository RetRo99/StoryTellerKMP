package com.retro99.home.ui.appsettings

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Login
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.retro99.base.buildconfig.BuildConfig
import com.retro99.base.ui.BaseScreen
import com.retro99.base.ui.IntentDispatcher
import com.retro99.translations.StringRes
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.koinInject
import org.koin.compose.viewmodel.koinViewModel
import resources.translations.app_settings_clear_logs
import resources.translations.app_settings_clear_logs_description
import resources.translations.app_settings_enable_logging
import resources.translations.app_settings_enable_logging_description
import resources.translations.app_settings_logout
import resources.translations.app_settings_logout_description
import resources.translations.app_settings_logs_cleared
import resources.translations.app_settings_no_logs
import resources.translations.app_settings_open_last_book
import resources.translations.app_settings_open_last_book_description
import resources.translations.app_settings_section_account
import resources.translations.app_settings_section_reading
import resources.translations.app_settings_section_support
import resources.translations.app_settings_share_logs
import resources.translations.app_settings_share_logs_description
import resources.translations.app_settings_title
import resources.translations.app_settings_version
import resources.translations.statistics_description
import resources.translations.statistics_title

@Composable
fun AppSettingsScreen(
    onLogout: () -> Unit,
    onNavigateToStatistics: () -> Unit,
    modifier: Modifier = Modifier,
    onLogin: () -> Unit = onLogout, // Default to logout callback for backward compatibility
    viewModel: AppSettingsViewModel = koinViewModel(),
) {
    BaseScreen(
        modifier = modifier,
        viewModel = viewModel,
    ) { viewState, intentDispatcher ->
        AppSettingsScreenContent(
            viewState = viewState,
            onLogout = onLogout,
            onLogin = onLogin,
            onNavigateToStatistics = onNavigateToStatistics,
            intentDispatcher = intentDispatcher,
        )
    }
}

@Composable
private fun AppSettingsScreenContent(
    viewState: AppSettingsViewState,
    onLogout: () -> Unit,
    onLogin: () -> Unit,
    onNavigateToStatistics: () -> Unit,
    intentDispatcher: IntentDispatcher<AppSettingsIntent>,
    modifier: Modifier = Modifier,
    buildConfig: BuildConfig = koinInject(),
) {
    val snackbarHostState = remember { SnackbarHostState() }
    val logsClearedMessage = stringResource(StringRes.app_settings_logs_cleared)
    val noLogsMessage = stringResource(StringRes.app_settings_no_logs)

    LaunchedEffect(viewState.showLogsClearedMessage) {
        if (viewState.showLogsClearedMessage) {
            snackbarHostState.showSnackbar(logsClearedMessage)
            intentDispatcher(AppSettingsIntent.OnLogsClearedMessageShown)
        }
    }

    LaunchedEffect(viewState.showNoLogsMessage) {
        if (viewState.showNoLogsMessage) {
            snackbarHostState.showSnackbar(noLogsMessage)
            intentDispatcher(AppSettingsIntent.OnNoLogsMessageShown)
        }
    }

    Box(
        modifier = modifier.fillMaxSize(),
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
        ) {
            Text(
                text = stringResource(StringRes.app_settings_title),
                style = MaterialTheme.typography.headlineMedium,
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Reading Section
            SettingsSectionHeader(
                title = stringResource(StringRes.app_settings_section_reading),
            )

            SettingsToggleItem(
                icon = Icons.Default.MenuBook,
                title = stringResource(StringRes.app_settings_open_last_book),
                description = stringResource(StringRes.app_settings_open_last_book_description),
                isChecked = viewState.openLastBookOnLaunch,
                onCheckedChange = { enabled ->
                    intentDispatcher(AppSettingsIntent.OnOpenLastBookToggled(enabled))
                },
            )

            SettingsItem(
                icon = Icons.Default.BarChart,
                title = stringResource(StringRes.statistics_title),
                description = stringResource(StringRes.statistics_description),
                onClick = onNavigateToStatistics,
            )

            HorizontalDivider()

            Spacer(modifier = Modifier.height(24.dp))

            // Support Section
            SettingsSectionHeader(
                title = stringResource(StringRes.app_settings_section_support),
            )

            SettingsToggleItem(
                icon = Icons.Default.Description,
                title = stringResource(StringRes.app_settings_enable_logging),
                description = stringResource(StringRes.app_settings_enable_logging_description),
                isChecked = viewState.isLoggingEnabled,
                onCheckedChange = { enabled ->
                    intentDispatcher(AppSettingsIntent.OnLoggingToggled(enabled))
                },
            )

            SettingsItem(
                icon = Icons.Default.Share,
                title = stringResource(StringRes.app_settings_share_logs),
                description = stringResource(StringRes.app_settings_share_logs_description),
                onClick = { intentDispatcher(AppSettingsIntent.OnShareLogsClicked) },
            )

            SettingsItem(
                icon = Icons.Default.DeleteSweep,
                title = stringResource(StringRes.app_settings_clear_logs),
                description = stringResource(StringRes.app_settings_clear_logs_description),
                onClick = { intentDispatcher(AppSettingsIntent.OnClearLogsClicked) },
            )

            HorizontalDivider()

            Spacer(modifier = Modifier.height(24.dp))

            // Account Section
            SettingsSectionHeader(
                title = stringResource(StringRes.app_settings_section_account),
            )

            if (viewState.hasAuthenticatedRemoteServers) {
                SettingsItem(
                    icon = Icons.AutoMirrored.Filled.Logout,
                    title = stringResource(StringRes.app_settings_logout),
                    description = stringResource(StringRes.app_settings_logout_description),
                    onClick = onLogout,
                    isDestructive = true,
                )
            } else {
                SettingsItem(
                    icon = Icons.AutoMirrored.Filled.Login,
                    title = "Login to Server",
                    description = "Connect to a remote server to sync your books",
                    onClick = onLogin,
                )
            }

            HorizontalDivider()

            // Add bottom padding to account for version text
            Spacer(modifier = Modifier.height(48.dp))
        }

        // Version info at the bottom
        Text(
            text = stringResource(
                StringRes.app_settings_version,
                buildConfig.versionName,
                buildConfig.versionCode,
            ),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 16.dp),
        )

        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier.align(Alignment.BottomCenter),
        )
    }
}

@Composable
private fun SettingsSectionHeader(
    title: String,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleSmall,
            color = MaterialTheme.colorScheme.primary,
        )
        Spacer(modifier = Modifier.height(8.dp))
        HorizontalDivider()
    }
}

@Composable
private fun SettingsItem(
    icon: ImageVector,
    title: String,
    description: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    isDestructive: Boolean = false,
) {
    val contentColor = if (isDestructive) {
        MaterialTheme.colorScheme.error
    } else {
        MaterialTheme.colorScheme.onSurface
    }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = contentColor,
        )

        Spacer(modifier = Modifier.width(16.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge,
                color = contentColor,
            )
            Text(
                text = description,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun SettingsToggleItem(
    icon: ImageVector,
    title: String,
    description: String,
    isChecked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clickable { onCheckedChange(!isChecked) }
            .padding(vertical = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurface,
        )

        Spacer(modifier = Modifier.width(16.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurface,
            )
            Text(
                text = description,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }

        Switch(
            checked = isChecked,
            onCheckedChange = onCheckedChange,
        )
    }
}

