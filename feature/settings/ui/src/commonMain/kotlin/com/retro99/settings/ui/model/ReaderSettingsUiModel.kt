package com.retro99.settings.ui.model

import com.retro99.reader.domain.model.ChapterProgressDisplayMode
import com.retro99.reader.domain.model.HighlightColor
import com.retro99.reader.domain.model.HighlightStyle
import com.retro99.reader.domain.model.ProgressBarPosition
import com.retro99.reader.domain.model.ProgressIndicatorMode

data class ReaderSettingsUiModel(
    val fontSize: Double = 1.0,
    val fontFamily: FontFamilyUiModel = FontFamilyUiModel.DEFAULT,
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
    // Highlight style for ReadAloud text highlighting
    val highlightStyle: HighlightStyle = HighlightStyle.HIGHLIGHT,
    // Progress bar visibility: true = always, null = on tap (with controls), false = never
    val showProgressBar: Boolean? = true,
    // Chapter progress display mode: NONE, PERCENTAGE, RELATIVE, or FIXED
    val chapterProgressDisplayMode: ChapterProgressDisplayMode = ChapterProgressDisplayMode.RELATIVE,
    // Whether to show total book progress
    val showTotalProgress: Boolean = true,
    // Progress indicator mode: NONE, CHAPTER, or BOOK
    val progressIndicatorMode: ProgressIndicatorMode = ProgressIndicatorMode.CHAPTER,
    // Progress bar position: TOP or BOTTOM
    val progressBarPosition: ProgressBarPosition = ProgressBarPosition.BOTTOM,
    // Whether to hide system bars (status bar, navigation bar) for immersive reading
    val fullscreenMode: Boolean = false,
    // Whether to show current time in the progress bar
    val showCurrentTime: Boolean = true,
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

/**
 * Font family options for the reader.
 * Based on Readium CSS supported fonts.
 * See https://readium.org/readium-css/docs/CSS10-libre_fonts
 */
enum class FontFamilyUiModel(val cssValue: String) {
    // Default - uses publisher's font or system default
    DEFAULT("default"),

    // Generic CSS font families
    SERIF("serif"),
    SANS_SERIF("sans-serif"),
    CURSIVE("cursive"),
    FANTASY("fantasy"),
    MONOSPACE("monospace"),

    // Accessibility fonts embedded with Readium
    ACCESSIBLE_DFA("AccessibleDfA"),
    IA_WRITER_DUOSPACE("IA Writer Duospace"),
    OPEN_DYSLEXIC("OpenDyslexic"),
}

