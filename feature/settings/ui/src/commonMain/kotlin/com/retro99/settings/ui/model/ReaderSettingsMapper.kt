package com.retro99.settings.ui.model

import com.retro99.reader.domain.model.ReaderSettingsDomainModel
import com.retro99.reader.domain.model.ReaderTextAlign
import com.retro99.reader.domain.model.ReaderTheme

fun ReaderSettingsDomainModel.toUiModel(): ReaderSettingsUiModel = ReaderSettingsUiModel(
    fontSize = fontSize,
    fontFamily = fontFamily,
    theme = theme.toUiModel(),
    lineHeight = lineHeight,
    marginHorizontal = marginHorizontal,
    marginVertical = marginVertical,
    scrollMode = scrollMode,
    textAlign = textAlign.toUiModel(),
)

fun ReaderSettingsUiModel.toDomainModel(): ReaderSettingsDomainModel = ReaderSettingsDomainModel(
    fontSize = fontSize,
    fontFamily = fontFamily,
    theme = theme.toDomainModel(),
    lineHeight = lineHeight,
    marginHorizontal = marginHorizontal,
    marginVertical = marginVertical,
    scrollMode = scrollMode,
    textAlign = textAlign.toDomainModel(),
)

fun ReaderTheme.toUiModel(): ReaderThemeUiModel = when (this) {
    ReaderTheme.LIGHT -> ReaderThemeUiModel.LIGHT
    ReaderTheme.DARK -> ReaderThemeUiModel.DARK
    ReaderTheme.SEPIA -> ReaderThemeUiModel.SEPIA
    ReaderTheme.SYSTEM -> ReaderThemeUiModel.SYSTEM
}

fun ReaderThemeUiModel.toDomainModel(): ReaderTheme = when (this) {
    ReaderThemeUiModel.LIGHT -> ReaderTheme.LIGHT
    ReaderThemeUiModel.DARK -> ReaderTheme.DARK
    ReaderThemeUiModel.SEPIA -> ReaderTheme.SEPIA
    ReaderThemeUiModel.SYSTEM -> ReaderTheme.SYSTEM
}

fun ReaderTextAlign.toUiModel(): ReaderTextAlignUiModel = when (this) {
    ReaderTextAlign.START -> ReaderTextAlignUiModel.START
    ReaderTextAlign.END -> ReaderTextAlignUiModel.END
    ReaderTextAlign.CENTER -> ReaderTextAlignUiModel.CENTER
    ReaderTextAlign.JUSTIFY -> ReaderTextAlignUiModel.JUSTIFY
}

fun ReaderTextAlignUiModel.toDomainModel(): ReaderTextAlign = when (this) {
    ReaderTextAlignUiModel.START -> ReaderTextAlign.START
    ReaderTextAlignUiModel.END -> ReaderTextAlign.END
    ReaderTextAlignUiModel.CENTER -> ReaderTextAlign.CENTER
    ReaderTextAlignUiModel.JUSTIFY -> ReaderTextAlign.JUSTIFY
}

