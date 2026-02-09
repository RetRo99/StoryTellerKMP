package com.retro99.settings.ui

import androidx.lifecycle.viewModelScope
import com.retro99.base.ui.BaseViewModel
import com.retro99.settings.domain.usecase.LogoutUseCase
import kotlinx.coroutines.launch
import org.koin.core.annotation.InjectedParam
import org.koin.core.annotation.KoinViewModel
import org.koin.core.annotation.Provided

@KoinViewModel
class SettingsViewModel(
    @Provided private val logoutUseCase: LogoutUseCase,
    @InjectedParam private val onLogoutSuccess: () -> Unit,
) : BaseViewModel<SettingsViewState, SettingsIntent>(SettingsViewState()) {

    override fun onIntent(intent: SettingsIntent) {
        when (intent) {
            SettingsIntent.OnLogoutClicked -> handleLogout()
        }
    }

    private fun handleLogout() {
        updateState { it.copy(isLoading = true) }

        viewModelScope.launch {
            logoutUseCase()
            onLogoutSuccess()
        }
    }
}

