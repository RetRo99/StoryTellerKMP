package com.retro99.settings.ui

import com.retro99.base.ui.BaseIntent
import com.retro99.reader.domain.model.ReaderTextAlign
import com.retro99.reader.domain.model.ReaderTheme

sealed interface SettingsIntent : BaseIntent {
    data object OnLogoutClicked : SettingsIntent

    // Reader settings intents
    data class OnThemeChanged(val theme: ReaderTheme) : SettingsIntent
    data class OnFontSizeChanged(val fontSize: Double) : SettingsIntent
    data class OnFontFamilyChanged(val fontFamily: String) : SettingsIntent
    data class OnLineHeightChanged(val lineHeight: Float) : SettingsIntent
    data class OnMarginHorizontalChanged(val margin: Int) : SettingsIntent
    data class OnMarginVerticalChanged(val margin: Int) : SettingsIntent
    data class OnTextAlignChanged(val textAlign: ReaderTextAlign) : SettingsIntent
    data class OnScrollModeChanged(val enabled: Boolean) : SettingsIntent
}

