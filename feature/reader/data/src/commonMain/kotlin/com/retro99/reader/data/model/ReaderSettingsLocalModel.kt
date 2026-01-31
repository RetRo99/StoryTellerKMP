package com.retro99.reader.data.model

import com.retro99.reader.domain.model.ReaderSettingsDomainModel
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
    )
}

