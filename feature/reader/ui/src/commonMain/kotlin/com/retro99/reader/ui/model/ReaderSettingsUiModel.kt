package com.retro99.reader.ui.model

import com.retro99.reader.domain.model.ReaderSettingsDomainModel
import com.retro99.reader.domain.model.ReaderTheme

data class ReaderSettingsUiModel(
    val fontSize: Double = 1.0,
    val fontFamily: String = "default",
    val theme: ReaderThemeUi = ReaderThemeUi.SYSTEM,
    val lineHeight: Float = 1.5f,
    val marginHorizontal: Int = 16,
    val marginVertical: Int = 16,
    val scrollMode: Boolean? = null,
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

fun ReaderSettingsDomainModel.toUiModel(): ReaderSettingsUiModel = ReaderSettingsUiModel(
    fontSize = fontSize,
    fontFamily = fontFamily,
    theme = theme.toUiTheme(),
    lineHeight = lineHeight,
    marginHorizontal = marginHorizontal,
    marginVertical = marginVertical,
    scrollMode = scrollMode,
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

