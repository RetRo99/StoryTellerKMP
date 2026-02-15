package com.retro99.home.ui.appsettings

import com.retro99.analytics.api.FileLogger
import com.retro99.base.ui.BaseViewModel
import com.retro99.base.ui.sharing.FileSharer
import com.retro99.preferences.api.Preferences
import com.retro99.preferences.api.PreferencesKey
import org.koin.core.annotation.KoinViewModel
import org.koin.core.annotation.Provided

@KoinViewModel
class AppSettingsViewModel(
    @Provided private val fileLogger: FileLogger,
    @Provided private val fileSharer: FileSharer,
    @Provided private val preferences: Preferences,
) : BaseViewModel<AppSettingsViewState, AppSettingsIntent>(
    AppSettingsViewState(
        isLoggingEnabled = preferences.getBoolean(
            PreferencesKey.FileLoggingEnabled,
            defaultValue = false,
        ),
        openLastBookOnLaunch = preferences.getBoolean(
            PreferencesKey.OpenLastBookOnLaunch,
            defaultValue = false,
        ),
    ),
) {

    override fun onIntent(intent: AppSettingsIntent) {
        when (intent) {
            is AppSettingsIntent.OnLoggingToggled -> setLoggingEnabled(intent.enabled)
            is AppSettingsIntent.OnOpenLastBookToggled -> setOpenLastBookOnLaunch(intent.enabled)
            AppSettingsIntent.OnShareLogsClicked -> shareLogs()
            AppSettingsIntent.OnClearLogsClicked -> clearLogs()
            AppSettingsIntent.OnLogsClearedMessageShown -> onLogsClearedMessageShown()
            AppSettingsIntent.OnNoLogsMessageShown -> onNoLogsMessageShown()
        }
    }

    private fun setLoggingEnabled(enabled: Boolean) {
        preferences.putBoolean(PreferencesKey.FileLoggingEnabled, enabled)
        updateState { it.copy(isLoggingEnabled = enabled) }
    }

    private fun setOpenLastBookOnLaunch(enabled: Boolean) {
        preferences.putBoolean(PreferencesKey.OpenLastBookOnLaunch, enabled)
        updateState { it.copy(openLastBookOnLaunch = enabled) }
    }

    private fun shareLogs() {
        val logContents = fileLogger.getLogContents()
        if (logContents.isEmpty()) {
            updateState { it.copy(showNoLogsMessage = true) }
            return
        }
        val logFilePath = fileLogger.getLogFilePath()
        fileSharer.shareFile(
            filePath = logFilePath,
            mimeType = "text/plain",
            title = "Share App Logs",
        )
    }

    private fun clearLogs() {
        fileLogger.clearLogs()
        updateState { it.copy(showLogsClearedMessage = true) }
    }

    private fun onLogsClearedMessageShown() {
        updateState { it.copy(showLogsClearedMessage = false) }
    }

    private fun onNoLogsMessageShown() {
        updateState { it.copy(showNoLogsMessage = false) }
    }
}

