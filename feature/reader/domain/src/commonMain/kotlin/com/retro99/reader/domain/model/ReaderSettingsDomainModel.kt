package com.retro99.reader.domain.model

/**
 * Represents reader settings/preferences.
 */
data class ReaderSettingsDomainModel(
    val fontSize: Float = 1.0f,
    val fontFamily: String = "default",
    val theme: ReaderTheme = ReaderTheme.SYSTEM,
    val lineHeight: Float = 1.5f,
    val marginHorizontal: Int = 16,
    val marginVertical: Int = 16,
)

enum class ReaderTheme {
    LIGHT,
    DARK,
    SEPIA,
    SYSTEM,
}

