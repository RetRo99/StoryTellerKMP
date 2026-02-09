package com.retro99.settings.ui

import com.retro99.reader.domain.model.ReaderSettingsDomainModel
import com.retro99.reader.domain.model.ReaderTextAlign
import com.retro99.reader.domain.model.ReaderTheme

data class SettingsViewState(
    val isLoading: Boolean = false,
    val readerSettings: ReaderSettingsDomainModel = ReaderSettingsDomainModel(),
) {
    // Convenience accessors for UI
    val theme: ReaderTheme get() = readerSettings.theme
    val fontSize: Double get() = readerSettings.fontSize
    val fontFamily: String get() = readerSettings.fontFamily
    val lineHeight: Float get() = readerSettings.lineHeight
    val marginHorizontal: Int get() = readerSettings.marginHorizontal
    val marginVertical: Int get() = readerSettings.marginVertical
    val textAlign: ReaderTextAlign get() = readerSettings.textAlign
    val scrollMode: Boolean get() = readerSettings.scrollMode ?: false
}

