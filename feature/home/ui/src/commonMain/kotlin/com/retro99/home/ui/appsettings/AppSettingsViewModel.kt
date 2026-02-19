package com.retro99.home.ui.appsettings

import androidx.lifecycle.viewModelScope
import com.retro99.analytics.api.FileLogger
import com.retro99.auth.domain.usecase.ObserveHasAuthenticatedRemoteServersUseCase
import com.retro99.base.ui.BaseViewModel
import com.retro99.base.ui.sharing.FileSharer
import com.retro99.preferences.api.Preferences
import com.retro99.preferences.api.PreferencesKey
import com.retro99.user.api.UserRegistry
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import org.koin.core.annotation.KoinViewModel
import org.koin.core.annotation.Provided

@KoinViewModel
class AppSettingsViewModel(
    @Provided private val fileLogger: FileLogger,
    @Provided private val fileSharer: FileSharer,
    @Provided private val preferences: Preferences,
    @Provided private val observeHasAuthenticatedRemoteServersUseCase: ObserveHasAuthenticatedRemoteServersUseCase,
    @Provided private val userRegistry: UserRegistry,
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

    init {
        observeAuthenticatedServers()
        observeUserProfiles()
    }

    private fun observeAuthenticatedServers() {
        observeHasAuthenticatedRemoteServersUseCase()
            .onEach { hasRemoteServers ->
                updateState { it.copy(hasAuthenticatedRemoteServers = hasRemoteServers) }
            }
            .launchIn(viewModelScope)
    }

    private fun observeUserProfiles() {
        userRegistry.observeAllProfiles()
            .onEach { profiles ->
                updateState { it.copy(userProfiles = profiles) }
            }
            .launchIn(viewModelScope)

        userRegistry.observeActiveProfile()
            .onEach { profile ->
                updateState { it.copy(activeProfile = profile) }
            }
            .launchIn(viewModelScope)
    }

    override fun onIntent(intent: AppSettingsIntent) {
        when (intent) {
            is AppSettingsIntent.OnLoggingToggled -> setLoggingEnabled(intent.enabled)
            is AppSettingsIntent.OnOpenLastBookToggled -> setOpenLastBookOnLaunch(intent.enabled)
            AppSettingsIntent.OnShareLogsClicked -> shareLogs()
            AppSettingsIntent.OnClearLogsClicked -> clearLogs()
            AppSettingsIntent.OnLogsClearedMessageShown -> onLogsClearedMessageShown()
            AppSettingsIntent.OnNoLogsMessageShown -> onNoLogsMessageShown()
            is AppSettingsIntent.OnProfileSelected -> selectProfile(intent.profileId)
            AppSettingsIntent.OnAddProfileClicked -> showAddProfileDialog()
            is AppSettingsIntent.OnAddProfileConfirmed -> addProfile(intent.name)
            AppSettingsIntent.OnAddProfileDismissed -> hideAddProfileDialog()
        }
    }

    private fun selectProfile(profileId: String) {
        viewModelScope.launch {
            userRegistry.setActiveProfile(profileId)
        }
    }

    private fun showAddProfileDialog() {
        updateState { it.copy(showAddProfileDialog = true) }
    }

    private fun hideAddProfileDialog() {
        updateState { it.copy(showAddProfileDialog = false) }
    }

    private fun addProfile(name: String) {
        viewModelScope.launch {
            val profile = userRegistry.createProfile(name = name)
            userRegistry.setActiveProfile(profile.id)
            updateState { it.copy(showAddProfileDialog = false) }
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

