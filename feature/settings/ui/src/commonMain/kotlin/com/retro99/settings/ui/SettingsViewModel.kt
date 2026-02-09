package com.retro99.settings.ui

import androidx.lifecycle.viewModelScope
import com.retro99.base.ui.BaseViewModel
import com.retro99.reader.domain.usecase.GetReaderSettingsUseCase
import com.retro99.reader.domain.usecase.SaveReaderSettingsUseCase
import com.retro99.settings.domain.usecase.LogoutUseCase
import com.retro99.settings.ui.model.ReaderSettingsUiModel
import com.retro99.settings.ui.model.toDomainModel
import com.retro99.settings.ui.model.toUiModel
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import org.koin.core.annotation.InjectedParam
import org.koin.core.annotation.KoinViewModel
import org.koin.core.annotation.Provided

@KoinViewModel
class SettingsViewModel(
    @Provided private val logoutUseCase: LogoutUseCase,
    @Provided private val getReaderSettingsUseCase: GetReaderSettingsUseCase,
    @Provided private val saveReaderSettingsUseCase: SaveReaderSettingsUseCase,
    @InjectedParam private val onLogoutSuccess: () -> Unit,
) : BaseViewModel<SettingsViewState, SettingsIntent>(SettingsViewState()) {

    init {
        observeReaderSettings()
    }

    override fun onIntent(intent: SettingsIntent) {
        when (intent) {
            SettingsIntent.OnLogoutClicked -> handleLogout()
            is SettingsIntent.OnThemeChanged -> updateReaderSetting { it.copy(theme = intent.theme) }
            is SettingsIntent.OnFontSizeChanged -> updateReaderSetting {
                it.copy(fontSize = intent.fontSize)
            }

            is SettingsIntent.OnFontFamilyChanged -> updateReaderSetting {
                it.copy(fontFamily = intent.fontFamily)
            }

            is SettingsIntent.OnLineHeightChanged -> updateReaderSetting {
                it.copy(lineHeight = intent.lineHeight)
            }

            is SettingsIntent.OnMarginHorizontalChanged -> updateReaderSetting {
                it.copy(marginHorizontal = intent.margin)
            }

            is SettingsIntent.OnMarginVerticalChanged -> updateReaderSetting {
                it.copy(marginVertical = intent.margin)
            }

            is SettingsIntent.OnTextAlignChanged -> updateReaderSetting {
                it.copy(textAlign = intent.textAlign)
            }

            is SettingsIntent.OnScrollModeChanged -> updateReaderSetting {
                it.copy(scrollMode = intent.enabled)
            }
        }
    }

    private fun observeReaderSettings() {
        getReaderSettingsUseCase()
            .onEach { settings ->
                updateState { it.copy(readerSettings = settings.toUiModel()) }
            }
            .launchIn(viewModelScope)
    }

    private fun updateReaderSetting(
        update: (ReaderSettingsUiModel) -> ReaderSettingsUiModel,
    ) {
        val currentSettings = viewState.value.readerSettings
        val newSettings = update(currentSettings)
        updateState { it.copy(readerSettings = newSettings) }

        viewModelScope.launch {
            saveReaderSettingsUseCase(newSettings.toDomainModel())
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

