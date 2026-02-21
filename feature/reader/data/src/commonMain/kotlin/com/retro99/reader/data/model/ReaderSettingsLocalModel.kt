package com.retro99.reader.data.model

import com.retro99.reader.domain.model.ChapterProgressDisplayMode
import com.retro99.reader.domain.model.HighlightStyle
import com.retro99.reader.domain.model.ProgressBarPosition
import com.retro99.reader.domain.model.ProgressIndicatorMode
import com.retro99.reader.domain.model.ReaderSettingsDomainModel
import com.retro99.reader.domain.model.ReaderTextAlign
import com.retro99.reader.domain.model.ReaderTheme
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ReaderSettingsLocalModel(
    @SerialName("font_size")
    val fontSize: Double = 1.0,
    @SerialName("font_family")
    val fontFamily: String = "default",
    @SerialName("theme")
    val theme: String = "SYSTEM",
    @SerialName("line_height")
    val lineHeight: Float = 1.5f,
    @SerialName("margin_horizontal")
    val marginHorizontal: Int = 16,
    @SerialName("margin_vertical")
    val marginVertical: Int = 16,
    @SerialName("scroll_mode")
    val scrollMode: Boolean? = null,
    @SerialName("text_align")
    val textAlign: String = "START",
    @SerialName("publisher_styles")
    val publisherStyles: Boolean = true,
    @SerialName("playback_speed")
    val playbackSpeed: Float = 1.0f,
    @SerialName("volume")
    val volume: Float = 1.0f,
    // Highlight color as ARGB Int value
    @SerialName("highlight_color_argb")
    val highlightColorArgb: Int = ReaderSettingsDomainModel.DEFAULT_HIGHLIGHT_COLOR,
    @SerialName("highlight_style")
    val highlightStyle: String = "HIGHLIGHT",
    // Progress bar visibility: true = always, null = on tap (with controls), false = never
    @SerialName("show_progress_bar")
    val showProgressBar: Boolean? = true,
    // Chapter progress display mode: NONE, PERCENTAGE, RELATIVE, or FIXED
    @SerialName("chapter_progress_display_mode")
    val chapterProgressDisplayMode: String = "RELATIVE",
    // Whether to show total book progress
    @SerialName("show_total_progress")
    val showTotalProgress: Boolean = true,
    // Progress indicator mode: NONE, CHAPTER, or BOOK
    @SerialName("progress_indicator_mode")
    val progressIndicatorMode: String = "CHAPTER",
    // Progress bar position: TOP or BOTTOM
    @SerialName("progress_bar_position")
    val progressBarPosition: String = "BOTTOM",
    // Whether to hide system bars (status bar, navigation bar) for immersive reading
    @SerialName("fullscreen_mode")
    val fullscreenMode: Boolean = false,
    // Whether to show current time in the progress bar
    @SerialName("show_current_time")
    val showCurrentTime: Boolean = true,
    // Whether to show estimated reading time for the current chapter
    @SerialName("show_reading_time")
    val showReadingTime: Boolean = true,
    // Reading speed in words per minute (used for reading time estimation)
    @SerialName("reading_speed_wpm")
    val readingSpeedWpm: Int = ReaderSettingsDomainModel.DEFAULT_READING_SPEED_WPM,
)

fun ReaderSettingsLocalModel.toDomain(): ReaderSettingsDomainModel {
    return ReaderSettingsDomainModel(
        fontSize = fontSize,
        fontFamily = fontFamily,
        theme = try {
            ReaderTheme.valueOf(theme)
        } catch (e: IllegalArgumentException) {
            ReaderTheme.SYSTEM
        },
        lineHeight = lineHeight,
        marginHorizontal = marginHorizontal,
        marginVertical = marginVertical,
        scrollMode = scrollMode,
        textAlign = try {
            ReaderTextAlign.valueOf(textAlign)
        } catch (e: IllegalArgumentException) {
            ReaderTextAlign.START
        },
        publisherStyles = publisherStyles,
        playbackSpeed = playbackSpeed,
        volume = volume,
        // Treat 0 (fully transparent black) as invalid and use default
        highlightColor = if (highlightColorArgb == 0) {
            ReaderSettingsDomainModel.DEFAULT_HIGHLIGHT_COLOR
        } else {
            highlightColorArgb
        },
        highlightStyle = try {
            HighlightStyle.valueOf(highlightStyle)
        } catch (e: IllegalArgumentException) {
            HighlightStyle.HIGHLIGHT
        },
        showProgressBar = showProgressBar,
        chapterProgressDisplayMode = try {
            ChapterProgressDisplayMode.valueOf(chapterProgressDisplayMode)
        } catch (e: IllegalArgumentException) {
            ChapterProgressDisplayMode.RELATIVE
        },
        showTotalProgress = showTotalProgress,
        progressIndicatorMode = try {
            ProgressIndicatorMode.valueOf(progressIndicatorMode)
        } catch (e: IllegalArgumentException) {
            ProgressIndicatorMode.CHAPTER
        },
        progressBarPosition = try {
            ProgressBarPosition.valueOf(progressBarPosition)
        } catch (e: IllegalArgumentException) {
            ProgressBarPosition.BOTTOM
        },
        fullscreenMode = fullscreenMode,
        showCurrentTime = showCurrentTime,
        showReadingTime = showReadingTime,
        readingSpeedWpm = readingSpeedWpm,
    )
}

fun ReaderSettingsDomainModel.toLocal(): ReaderSettingsLocalModel {
    return ReaderSettingsLocalModel(
        fontSize = fontSize,
        fontFamily = fontFamily,
        theme = theme.name,
        lineHeight = lineHeight,
        marginHorizontal = marginHorizontal,
        marginVertical = marginVertical,
        scrollMode = scrollMode,
        textAlign = textAlign.name,
        publisherStyles = publisherStyles,
        playbackSpeed = playbackSpeed,
        volume = volume,
        highlightColorArgb = highlightColor,
        highlightStyle = highlightStyle.name,
        showProgressBar = showProgressBar,
        chapterProgressDisplayMode = chapterProgressDisplayMode.name,
        showTotalProgress = showTotalProgress,
        progressIndicatorMode = progressIndicatorMode.name,
        progressBarPosition = progressBarPosition.name,
        fullscreenMode = fullscreenMode,
        showCurrentTime = showCurrentTime,
        showReadingTime = showReadingTime,
        readingSpeedWpm = readingSpeedWpm,
    )
}
