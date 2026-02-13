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
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.retro99.analytics.api.Analytics
import com.retro99.base.buildconfig.BuildConfig
import com.retro99.base.ui.sharing.FileSharer
import com.retro99.translations.StringRes
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.koinInject
import resources.translations.app_settings_clear_logs
import resources.translations.app_settings_clear_logs_description
import resources.translations.app_settings_logout
import resources.translations.app_settings_logout_description
import resources.translations.app_settings_logs_cleared
import resources.translations.app_settings_section_account
import resources.translations.app_settings_section_support
import resources.translations.app_settings_share_logs
import resources.translations.app_settings_share_logs_description
import resources.translations.app_settings_title
import resources.translations.app_settings_version

@Composable
fun AppSettingsScreen(
    onLogout: () -> Unit,
    modifier: Modifier = Modifier,
    buildConfig: BuildConfig = koinInject(),
    analytics: Analytics = koinInject(),
    fileSharer: FileSharer = koinInject(),
) {
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val logsClearedMessage = stringResource(StringRes.app_settings_logs_cleared)

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

            // Account Section
            SettingsSectionHeader(
                title = stringResource(StringRes.app_settings_section_account),
            )

            SettingsItem(
                icon = Icons.AutoMirrored.Filled.Logout,
                title = stringResource(StringRes.app_settings_logout),
                description = stringResource(StringRes.app_settings_logout_description),
                onClick = onLogout,
                isDestructive = true,
            )

            HorizontalDivider()

            Spacer(modifier = Modifier.height(24.dp))

            // Support Section
            SettingsSectionHeader(
                title = stringResource(StringRes.app_settings_section_support),
            )

            SettingsItem(
                icon = Icons.Default.Share,
                title = stringResource(StringRes.app_settings_share_logs),
                description = stringResource(StringRes.app_settings_share_logs_description),
                onClick = {
                    val fileLogger = analytics.getFileLogger()
                    val logFilePath = fileLogger.getLogFilePath()
                    fileSharer.shareFile(
                        filePath = logFilePath,
                        mimeType = "text/plain",
                        title = "Share App Logs",
                    )
                },
            )

            SettingsItem(
                icon = Icons.Default.DeleteSweep,
                title = stringResource(StringRes.app_settings_clear_logs),
                description = stringResource(StringRes.app_settings_clear_logs_description),
                onClick = {
                    analytics.getFileLogger().clearLogs()
                    scope.launch {
                        snackbarHostState.showSnackbar(logsClearedMessage)
                    }
                },
            )

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

