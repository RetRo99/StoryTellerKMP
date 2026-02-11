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
    // Progress bar visibility: true = always, null = on tap (with controls), false = never
    val showProgressBar: Boolean? = true,
    // Chapter progress display mode: NONE, PERCENTAGE, RELATIVE, or FIXED
    val chapterProgressDisplayMode: ChapterProgressDisplayMode = ChapterProgressDisplayMode.RELATIVE,
    // Whether to show total book progress
    val showTotalProgress: Boolean = true,
    // Progress indicator mode: NONE, CHAPTER, or BOOK
    val progressIndicatorMode: ProgressIndicatorMode = ProgressIndicatorMode.CHAPTER,
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

/**
 * Display mode for chapter progress in the progress bar.
 */
enum class ChapterProgressDisplayMode {
    /** No chapter progress info shown */
    NONE,

    /** Show chapter progress as percentage (e.g., "50%") */
    PERCENTAGE,

    /** Show page numbers based on current screen/viewport (changes with font size, margins, etc.) */
    RELATIVE,

    /** Show page numbers based on publication's fixed position (EPUB position based on 1024-char blocks) */
    FIXED,
}

/**
 * Progress indicator mode for the reader.
 */
enum class ProgressIndicatorMode {
    /** No progress indicator shown */
    NONE,

    /** Show chapter progress indicator */
    CHAPTER,

    /** Show book (total) progress indicator */
    BOOK,
}
