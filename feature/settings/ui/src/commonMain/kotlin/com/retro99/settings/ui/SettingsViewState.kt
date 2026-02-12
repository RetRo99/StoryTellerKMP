package com.retro99.settings.ui

import com.retro99.reader.domain.model.ChapterProgressDisplayMode
import com.retro99.reader.domain.model.HighlightColor
import com.retro99.reader.domain.model.HighlightStyle
import com.retro99.reader.domain.model.ProgressBarPosition
import com.retro99.reader.domain.model.ProgressIndicatorMode
import com.retro99.settings.ui.model.FontFamilyUiModel
import com.retro99.settings.ui.model.ReaderSettingsUiModel
import com.retro99.settings.ui.model.ReaderTextAlignUiModel
import com.retro99.settings.ui.model.ReaderThemeUiModel

data class SettingsViewState(
    val isLoading: Boolean = false,
    val readerSettings: ReaderSettingsUiModel = ReaderSettingsUiModel(),
    val expandedSections: Set<SettingsSection> = setOf(SettingsSection.APPEARANCE),
) {
    // Convenience accessors for UI
    val theme: ReaderThemeUiModel get() = readerSettings.theme
    val fontSize: Double get() = readerSettings.fontSize
    val fontFamily: FontFamilyUiModel get() = readerSettings.fontFamily
    val lineHeight: Float get() = readerSettings.lineHeight
    val marginHorizontal: Int get() = readerSettings.marginHorizontal
    val marginVertical: Int get() = readerSettings.marginVertical
    val textAlign: ReaderTextAlignUiModel get() = readerSettings.textAlign
    val scrollMode: Boolean? get() = readerSettings.scrollMode
    val publisherStyles: Boolean get() = readerSettings.publisherStyles
    val showProgressBar: Boolean? get() = readerSettings.showProgressBar
    val chapterProgressDisplayMode: ChapterProgressDisplayMode
        get() = readerSettings.chapterProgressDisplayMode
    val highlightColor: HighlightColor get() = readerSettings.highlightColor
    val highlightStyle: HighlightStyle get() = readerSettings.highlightStyle
    val showTotalProgress: Boolean get() = readerSettings.showTotalProgress
    val progressIndicatorMode: ProgressIndicatorMode get() = readerSettings.progressIndicatorMode
    val progressBarPosition: ProgressBarPosition get() = readerSettings.progressBarPosition

    fun isSectionExpanded(section: SettingsSection): Boolean = section in expandedSections
}

enum class SettingsSection {
    APPEARANCE,
    TYPOGRAPHY,
    LAYOUT,
    READALOUD,
}

