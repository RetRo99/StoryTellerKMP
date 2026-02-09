package com.retro99.settings.ui

import com.retro99.settings.ui.model.ReaderSettingsUiModel
import com.retro99.settings.ui.model.ReaderTextAlignUiModel
import com.retro99.settings.ui.model.ReaderThemeUiModel

data class SettingsViewState(
    val isLoading: Boolean = false,
    val readerSettings: ReaderSettingsUiModel = ReaderSettingsUiModel(),
) {
    // Convenience accessors for UI
    val theme: ReaderThemeUiModel get() = readerSettings.theme
    val fontSize: Double get() = readerSettings.fontSize
    val fontFamily: String get() = readerSettings.fontFamily
    val lineHeight: Float get() = readerSettings.lineHeight
    val marginHorizontal: Int get() = readerSettings.marginHorizontal
    val marginVertical: Int get() = readerSettings.marginVertical
    val textAlign: ReaderTextAlignUiModel get() = readerSettings.textAlign
    val scrollMode: Boolean get() = readerSettings.scrollMode
}

