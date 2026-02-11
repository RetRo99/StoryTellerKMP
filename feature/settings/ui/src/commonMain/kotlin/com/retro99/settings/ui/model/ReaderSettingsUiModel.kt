package com.retro99.settings.ui.model

import com.retro99.reader.domain.model.HighlightColor

data class ReaderSettingsUiModel(
    val fontSize: Double = 1.0,
    val fontFamily: String = "default",
    val theme: ReaderThemeUiModel = ReaderThemeUiModel.SYSTEM,
    val lineHeight: Float = 1.5f,
    val marginHorizontal: Int = 16,
    val marginVertical: Int = 16,
    val scrollMode: Boolean? = null,
    val textAlign: ReaderTextAlignUiModel = ReaderTextAlignUiModel.START,
    // When true, uses publisher's CSS styles. When false, allows custom lineHeight, textAlign, etc.
    val publisherStyles: Boolean = true,
    // Highlight color for ReadAloud text highlighting
    val highlightColor: HighlightColor = HighlightColor.YELLOW,
)

enum class ReaderThemeUiModel {
    LIGHT,
    DARK,
    SEPIA,
    SYSTEM,
}

enum class ReaderTextAlignUiModel {
    START,
    END,
    CENTER,
    JUSTIFY,
}

