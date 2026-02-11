package com.retro99.reader.ui.model

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
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
