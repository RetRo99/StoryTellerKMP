package com.retro99.server.audiobookshelf.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class AudiobookshelfLibraryListApiModel(
    @SerialName("libraries")
    val libraries: List<AudiobookshelfLibraryApiModel> = emptyList(),
)

@Serializable
data class AudiobookshelfLibraryApiModel(
    @SerialName("id")
    val id: String,

    @SerialName("name")
    val name: String,

    @SerialName("mediaType")
    val mediaType: String? = null,
)
