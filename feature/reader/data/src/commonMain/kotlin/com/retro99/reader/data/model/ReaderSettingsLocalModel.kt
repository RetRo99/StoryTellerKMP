package com.retro99.reader.data.model

import com.retro99.reader.domain.model.HighlightColor
import com.retro99.reader.domain.model.HighlightStyle
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
    )
}

