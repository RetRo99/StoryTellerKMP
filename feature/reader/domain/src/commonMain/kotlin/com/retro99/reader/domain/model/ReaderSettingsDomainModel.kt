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
    // When true, uses publisher's CSS styles. When false, allows custom lineHeight, textAlign, etc.
    val publisherStyles: Boolean = true,
    // Media playback settings for ReadAloud books
    val playbackSpeed: Float = 1.0f,
    val volume: Float = 1.0f,
    // Highlight color for ReadAloud text highlighting
    val highlightColor: HighlightColor = HighlightColor.YELLOW,
    // Highlight style for ReadAloud text highlighting
    val highlightStyle: HighlightStyle = HighlightStyle.HIGHLIGHT,
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

/**
 * Available highlight colors for ReadAloud text highlighting.
 */
enum class HighlightColor {
    YELLOW,
    GREEN,
    BLUE,
    PINK,
    ORANGE,
}

/**
 * Available highlight styles for ReadAloud text highlighting.
 */
enum class HighlightStyle {
    /** Background highlight only */
    HIGHLIGHT,

    /** Underline only (no background highlight) */
    UNDERLINE,

    /** Background highlight with underline */
    HIGHLIGHT_UNDERLINE,
}

