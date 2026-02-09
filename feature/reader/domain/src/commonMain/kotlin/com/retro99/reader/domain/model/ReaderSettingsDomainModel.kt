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
    val scrollMode: Boolean? = null,
    val textAlign: ReaderTextAlign = ReaderTextAlign.START,
    // Media playback settings for ReadAloud books
    val playbackSpeed: Float = 1.0f,
    val volume: Float = 1.0f,
)

enum class ReaderTheme {
    LIGHT,
    DARK,
    SEPIA,
    SYSTEM,
}

enum class ReaderTextAlign {
    START,
    END,
    CENTER,
    JUSTIFY,
}

