package com.retro99.reader.ui.model

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import com.retro99.reader.domain.model.ChapterProgressDisplayMode
import com.retro99.reader.domain.model.HighlightColor
import com.retro99.reader.domain.model.HighlightStyle
import com.retro99.reader.domain.model.ProgressIndicatorMode
import com.retro99.reader.domain.model.ReaderSettingsDomainModel
import com.retro99.reader.domain.model.ReaderTextAlign
import com.retro99.reader.domain.model.ReaderTheme

data class ReaderSettingsUiModel(
    val fontSize: Double = 1.0,
    val fontFamily: String = "default",
    val theme: ReaderThemeUi = ReaderThemeUi.SYSTEM,
    val lineHeight: Float = 1.5f,
    val marginHorizontal: Int = 16,
    val marginVertical: Int = 16,
    val scrollMode: Boolean? = null,
    val textAlign: ReaderTextAlignUi = ReaderTextAlignUi.START,
    // When true, uses publisher's CSS styles. When false, allows custom lineHeight, textAlign, etc.
    val publisherStyles: Boolean = true,
    // Media playback settings for ReadAloud books
    val playbackSpeed: Float = 1.0f,
    val volume: Float = 1.0f,
    // Highlight color for ReadAloud text highlighting
    val highlightColor: ReadAloudHighlightColor = ReadAloudHighlightColor.YELLOW,
    // Highlight style for ReadAloud text highlighting
    val highlightStyle: ReadAloudHighlightStyle = ReadAloudHighlightStyle.HIGHLIGHT,
    // Progress bar visibility: true = always, null = on tap (with controls), false = never
    val showProgressBar: Boolean? = true,
    // Chapter progress display mode: NONE, PERCENTAGE, RELATIVE, or FIXED
    val chapterProgressDisplayMode: ChapterProgressDisplayMode = ChapterProgressDisplayMode.RELATIVE,
    // Whether to show total book progress
    val showTotalProgress: Boolean = true,
    // Progress indicator mode: NONE, CHAPTER, or BOOK
    val progressIndicatorMode: ProgressIndicatorMode = ProgressIndicatorMode.CHAPTER,
)

enum class ReaderThemeUi {
    LIGHT,
    DARK,
    SEPIA,
    SYSTEM,
}

enum class ReaderTextAlignUi {
    START,
    END,
    CENTER,
    JUSTIFY,
}

/**
 * Available highlight colors for ReadAloud text highlighting.
 * Each color has an associated ARGB value for rendering.
 */
enum class ReadAloudHighlightColor(val argb: Int) {
    YELLOW(0x80FFEB3B.toInt()),
    GREEN(0x8081C784.toInt()),
    BLUE(0x8064B5F6.toInt()),
    PINK(0x80F48FB1.toInt()),
    ORANGE(0x80FFB74D.toInt()),
}

/**
 * Available highlight styles for ReadAloud text highlighting.
 */
enum class ReadAloudHighlightStyle {
    /** Background highlight only */
    HIGHLIGHT,

    /** Background highlight with underline */
    HIGHLIGHT_UNDERLINE,

    /** Underline only (no background highlight) */
    UNDERLINE,
}

fun ReaderSettingsDomainModel.toUiModel(): ReaderSettingsUiModel = ReaderSettingsUiModel(
    fontSize = fontSize,
    fontFamily = fontFamily,
    theme = theme.toUiTheme(),
    lineHeight = lineHeight,
    marginHorizontal = marginHorizontal,
    marginVertical = marginVertical,
    scrollMode = scrollMode,
    textAlign = textAlign.toUiTextAlign(),
    publisherStyles = publisherStyles,
    playbackSpeed = playbackSpeed,
    volume = volume,
    highlightColor = highlightColor.toUiHighlightColor(),
    highlightStyle = highlightStyle.toUiHighlightStyle(),
    showProgressBar = showProgressBar,
    chapterProgressDisplayMode = chapterProgressDisplayMode,
    showTotalProgress = showTotalProgress,
    progressIndicatorMode = progressIndicatorMode,
)

fun ReaderSettingsUiModel.toDomainModel(): ReaderSettingsDomainModel = ReaderSettingsDomainModel(
    fontSize = fontSize,
    fontFamily = fontFamily,
    theme = theme.toDomainTheme(),
    lineHeight = lineHeight,
    marginHorizontal = marginHorizontal,
    marginVertical = marginVertical,
    scrollMode = scrollMode,
    textAlign = textAlign.toDomainTextAlign(),
    publisherStyles = publisherStyles,
    playbackSpeed = playbackSpeed,
    volume = volume,
    highlightColor = highlightColor.toDomainHighlightColor(),
    highlightStyle = highlightStyle.toDomainHighlightStyle(),
    showProgressBar = showProgressBar,
    chapterProgressDisplayMode = chapterProgressDisplayMode,
    showTotalProgress = showTotalProgress,
    progressIndicatorMode = progressIndicatorMode,
)

private fun ReaderTheme.toUiTheme(): ReaderThemeUi = when (this) {
    ReaderTheme.LIGHT -> ReaderThemeUi.LIGHT
    ReaderTheme.DARK -> ReaderThemeUi.DARK
    ReaderTheme.SEPIA -> ReaderThemeUi.SEPIA
    ReaderTheme.SYSTEM -> ReaderThemeUi.SYSTEM
}

private fun ReaderThemeUi.toDomainTheme(): ReaderTheme = when (this) {
    ReaderThemeUi.LIGHT -> ReaderTheme.LIGHT
    ReaderThemeUi.DARK -> ReaderTheme.DARK
    ReaderThemeUi.SEPIA -> ReaderTheme.SEPIA
    ReaderThemeUi.SYSTEM -> ReaderTheme.SYSTEM
}

private fun ReaderTextAlign.toUiTextAlign(): ReaderTextAlignUi = when (this) {
    ReaderTextAlign.START -> ReaderTextAlignUi.START
    ReaderTextAlign.END -> ReaderTextAlignUi.END
    ReaderTextAlign.CENTER -> ReaderTextAlignUi.CENTER
    ReaderTextAlign.JUSTIFY -> ReaderTextAlignUi.JUSTIFY
}

private fun ReaderTextAlignUi.toDomainTextAlign(): ReaderTextAlign = when (this) {
    ReaderTextAlignUi.START -> ReaderTextAlign.START
    ReaderTextAlignUi.END -> ReaderTextAlign.END
    ReaderTextAlignUi.CENTER -> ReaderTextAlign.CENTER
    ReaderTextAlignUi.JUSTIFY -> ReaderTextAlign.JUSTIFY
}

private fun HighlightColor.toUiHighlightColor(): ReadAloudHighlightColor = when (this) {
    HighlightColor.YELLOW -> ReadAloudHighlightColor.YELLOW
    HighlightColor.GREEN -> ReadAloudHighlightColor.GREEN
    HighlightColor.BLUE -> ReadAloudHighlightColor.BLUE
    HighlightColor.PINK -> ReadAloudHighlightColor.PINK
    HighlightColor.ORANGE -> ReadAloudHighlightColor.ORANGE
}

private fun ReadAloudHighlightColor.toDomainHighlightColor(): HighlightColor = when (this) {
    ReadAloudHighlightColor.YELLOW -> HighlightColor.YELLOW
    ReadAloudHighlightColor.GREEN -> HighlightColor.GREEN
    ReadAloudHighlightColor.BLUE -> HighlightColor.BLUE
    ReadAloudHighlightColor.PINK -> HighlightColor.PINK
    ReadAloudHighlightColor.ORANGE -> HighlightColor.ORANGE
}

private fun HighlightStyle.toUiHighlightStyle(): ReadAloudHighlightStyle = when (this) {
    HighlightStyle.HIGHLIGHT -> ReadAloudHighlightStyle.HIGHLIGHT
    HighlightStyle.HIGHLIGHT_UNDERLINE -> ReadAloudHighlightStyle.HIGHLIGHT_UNDERLINE
    HighlightStyle.UNDERLINE -> ReadAloudHighlightStyle.UNDERLINE
}

private fun ReadAloudHighlightStyle.toDomainHighlightStyle(): HighlightStyle = when (this) {
    ReadAloudHighlightStyle.HIGHLIGHT -> HighlightStyle.HIGHLIGHT
    ReadAloudHighlightStyle.HIGHLIGHT_UNDERLINE -> HighlightStyle.HIGHLIGHT_UNDERLINE
    ReadAloudHighlightStyle.UNDERLINE -> HighlightStyle.UNDERLINE
}

// Theme background colors matching Readium CSS
// https://github.com/readium/readium-css/blob/master/css/src/modules/ReadiumCSS-day_mode.css
private val LightBackgroundColor = Color(0xFFFFFFFF)

// https://github.com/readium/readium-css/blob/master/css/src/modules/ReadiumCSS-night_mode.css
private val DarkBackgroundColor = Color(0xFF000000)

// https://github.com/readium/readium-css/blob/master/css/src/modules/ReadiumCSS-sepia_mode.css
private val SepiaBackgroundColor = Color(0xFFFAF4E8)

/**
 * Returns the background color for the reader based on the current theme.
 * For SYSTEM theme, it uses the system's dark/light mode setting.
 */
@Composable
fun ReaderThemeUi.backgroundColor(): Color {
    val isSystemDark = isSystemInDarkTheme()
    return when (this) {
        ReaderThemeUi.LIGHT -> LightBackgroundColor
        ReaderThemeUi.DARK -> DarkBackgroundColor
        ReaderThemeUi.SEPIA -> SepiaBackgroundColor
        ReaderThemeUi.SYSTEM -> if (isSystemDark) DarkBackgroundColor else LightBackgroundColor
    }
}
