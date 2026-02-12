package com.retro99.reader.data.model

import com.retro99.reader.domain.model.ChapterProgressDisplayMode
import com.retro99.reader.domain.model.HighlightColor
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
    @SerialName("highlight_color")
    val highlightColor: String = "YELLOW",
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
        highlightColor = try {
            HighlightColor.valueOf(highlightColor)
        } catch (e: IllegalArgumentException) {
            HighlightColor.YELLOW
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
        highlightColor = highlightColor.name,
        highlightStyle = highlightStyle.name,
        showProgressBar = showProgressBar,
        chapterProgressDisplayMode = chapterProgressDisplayMode.name,
        showTotalProgress = showTotalProgress,
        progressIndicatorMode = progressIndicatorMode.name,
        progressBarPosition = progressBarPosition.name,
    )
}
