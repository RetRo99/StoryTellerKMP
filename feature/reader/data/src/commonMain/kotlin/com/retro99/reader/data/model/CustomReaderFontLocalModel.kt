package com.retro99.reader.data.model

import com.retro99.reader.domain.model.CustomReaderFontDomainModel
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class CustomReaderFontLocalModel(
    @SerialName("id")
    val id: String,
    @SerialName("display_name")
    val displayName: String,
    @SerialName("css_family")
    val cssFamily: String,
    @SerialName("file_path")
    val filePath: String,
    @SerialName("file_name")
    val fileName: String,
)

fun CustomReaderFontLocalModel.toDomain(): CustomReaderFontDomainModel =
    CustomReaderFontDomainModel(
        id = id,
        displayName = displayName,
        cssFamily = cssFamily,
        filePath = filePath,
        fileName = fileName,
    )

fun CustomReaderFontDomainModel.toLocal(): CustomReaderFontLocalModel =
    CustomReaderFontLocalModel(
        id = id,
        displayName = displayName,
        cssFamily = cssFamily,
        filePath = filePath,
        fileName = fileName,
    )
