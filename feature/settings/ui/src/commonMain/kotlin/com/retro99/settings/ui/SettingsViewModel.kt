package com.retro99.settings.ui

import androidx.lifecycle.viewModelScope
import com.retro99.analytics.api.Analytics
import com.retro99.analytics.api.ReaderAnalyticsEvent
import com.retro99.base.ui.BaseViewModel
import com.retro99.reader.domain.usecase.GetReaderSettingsUseCase
import com.retro99.reader.domain.usecase.SaveReaderSettingsUseCase
import com.retro99.settings.ui.model.ReaderSettingsUiModel
import com.retro99.settings.ui.model.toDomainModel
import com.retro99.settings.ui.model.toUiModel
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import org.koin.core.annotation.KoinViewModel
import org.koin.core.annotation.Provided

@KoinViewModel
class SettingsViewModel(
    @Provided private val getReaderSettingsUseCase: GetReaderSettingsUseCase,
    @Provided private val saveReaderSettingsUseCase: SaveReaderSettingsUseCase,
    @Provided private val analytics: Analytics,
) : BaseViewModel<SettingsViewState, SettingsIntent>(SettingsViewState()) {

    init {
        observeReaderSettings()
    }

    override fun onIntent(intent: SettingsIntent) {
        when (intent) {
            is SettingsIntent.OnSectionToggled -> toggleSection(intent.section)
            is SettingsIntent.OnThemeChanged -> updateReaderSetting("theme", intent.theme.name) {
                it.copy(theme = intent.theme)
            }

            is SettingsIntent.OnFontSizeChanged -> updateReaderSetting(
                "font_size",
                intent.fontSize.toString(),
            ) {
                it.copy(fontSize = intent.fontSize)
            }

            is SettingsIntent.OnFontFamilyChanged -> updateReaderSetting(
                "font_family",
                intent.fontFamily.name,
            ) {
                it.copy(fontFamily = intent.fontFamily)
            }

            is SettingsIntent.OnLineHeightChanged -> updateReaderSetting(
                "line_height",
                intent.lineHeight.toString(),
            ) {
                it.copy(lineHeight = intent.lineHeight)
            }

            is SettingsIntent.OnMarginHorizontalChanged -> updateReaderSetting(
                "margin_horizontal",
                intent.margin.toString(),
            ) {
                it.copy(marginHorizontal = intent.margin)
            }

            is SettingsIntent.OnMarginVerticalChanged -> updateReaderSetting(
                "margin_vertical",
                intent.margin.toString(),
            ) {
                it.copy(marginVertical = intent.margin)
            }

            is SettingsIntent.OnTextAlignChanged -> updateReaderSetting(
                "text_align",
                intent.textAlign.name,
            ) {
                it.copy(textAlign = intent.textAlign)
            }

            is SettingsIntent.OnScrollModeChanged -> updateReaderSetting(
                "scroll_mode",
                intent.scrollMode.toString(),
            ) {
                it.copy(scrollMode = intent.scrollMode)
            }

            is SettingsIntent.OnPublisherStylesChanged -> updateReaderSetting(
                "publisher_styles",
                intent.publisherStyles.toString(),
            ) {
                it.copy(publisherStyles = intent.publisherStyles)
            }

            is SettingsIntent.OnShowProgressBarChanged -> updateReaderSetting(
                "show_progress_bar",
                intent.showProgressBar.toString(),
            ) {
                it.copy(showProgressBar = intent.showProgressBar)
            }

            is SettingsIntent.OnChapterProgressDisplayModeChanged -> updateReaderSetting(
                "chapter_progress_display_mode",
                intent.mode.name,
            ) {
                it.copy(chapterProgressDisplayMode = intent.mode)
            }

            is SettingsIntent.OnShowTotalProgressChanged -> updateReaderSetting(
                "show_total_progress",
                intent.showTotalProgress.toString(),
            ) {
                it.copy(showTotalProgress = intent.showTotalProgress)
            }

            is SettingsIntent.OnProgressIndicatorModeChanged -> updateReaderSetting(
                "progress_indicator_mode",
                intent.mode.name,
            ) {
                it.copy(progressIndicatorMode = intent.mode)
            }

            is SettingsIntent.OnProgressBarPositionChanged -> updateReaderSetting(
                "progress_bar_position",
                intent.position.name,
            ) {
                it.copy(progressBarPosition = intent.position)
            }

            is SettingsIntent.OnHighlightColorChanged -> updateReaderSetting(
                "highlight_color",
                intent.color.name,
            ) {
                it.copy(highlightColor = intent.color)
            }

            is SettingsIntent.OnHighlightStyleChanged -> updateReaderSetting(
                "highlight_style",
                intent.style.name,
            ) {
                it.copy(highlightStyle = intent.style)
            }

            is SettingsIntent.OnFullscreenModeChanged -> updateReaderSetting(
                "fullscreen_mode",
                intent.fullscreenMode.toString(),
            ) {
                it.copy(fullscreenMode = intent.fullscreenMode)
            }

            is SettingsIntent.OnShowCurrentTimeChanged -> updateReaderSetting(
                "show_current_time",
                intent.showCurrentTime.toString(),
            ) {
                it.copy(showCurrentTime = intent.showCurrentTime)
            }
        }
    }

    private fun toggleSection(section: SettingsSection) {
        updateState { state ->
            val newExpandedSections = if (section in state.expandedSections) {
                state.expandedSections - section
            } else {
                state.expandedSections + section
            }
            state.copy(expandedSections = newExpandedSections)
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
        settingName: String,
        newValue: String,
        update: (ReaderSettingsUiModel) -> ReaderSettingsUiModel,
    ) {
        analytics.logEvent(ReaderAnalyticsEvent.SettingChanged(settingName, newValue))

        val currentSettings = viewState.value.readerSettings
        val newSettings = update(currentSettings)
        updateState { it.copy(readerSettings = newSettings) }

        viewModelScope.launch {
            saveReaderSettingsUseCase(newSettings.toDomainModel())
        }
    }
}

