package com.retro99.reader.domain.model

/**
 * Represents reader settings/preferences.
 */
data class ReaderSettingsDomainModel(
    val fontSize: Double = 1.0,
    val fontFamily: String = "default",
    val theme: ReaderTheme = ReaderTheme.SYSTEM,
    val lineHeight: Float = 1.5f,
    val marginHorizontal: Int = 16,
    val marginVertical: Int = 16,
    val scrollMode: Boolean = true,
)

enum class ReaderTheme {
    LIGHT,
    DARK,
    SEPIA,
    SYSTEM,
}

