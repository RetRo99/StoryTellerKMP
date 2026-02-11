package com.retro99.settings.ui

import com.retro99.base.ui.BaseIntent
import com.retro99.reader.domain.model.HighlightColor
import com.retro99.reader.domain.model.HighlightStyle
import com.retro99.settings.ui.model.FontFamilyUiModel
import com.retro99.settings.ui.model.ReaderTextAlignUiModel
import com.retro99.settings.ui.model.ReaderThemeUiModel

sealed interface SettingsIntent : BaseIntent {
    // Section expansion
    data class OnSectionToggled(val section: SettingsSection) : SettingsIntent

    // Reader settings intents
    data class OnThemeChanged(val theme: ReaderThemeUiModel) : SettingsIntent
    data class OnFontSizeChanged(val fontSize: Double) : SettingsIntent
    data class OnFontFamilyChanged(val fontFamily: FontFamilyUiModel) : SettingsIntent
    data class OnLineHeightChanged(val lineHeight: Float) : SettingsIntent
    data class OnMarginHorizontalChanged(val margin: Int) : SettingsIntent
    data class OnMarginVerticalChanged(val margin: Int) : SettingsIntent
    data class OnTextAlignChanged(val textAlign: ReaderTextAlignUiModel) : SettingsIntent
    data class OnScrollModeChanged(val scrollMode: Boolean?) : SettingsIntent
    data class OnPublisherStylesChanged(val publisherStyles: Boolean) : SettingsIntent
    data class OnShowProgressBarChanged(val showProgressBar: Boolean?) : SettingsIntent

    // ReadAloud settings intents
    data class OnHighlightColorChanged(val color: HighlightColor) : SettingsIntent
    data class OnHighlightStyleChanged(val style: HighlightStyle) : SettingsIntent
}

