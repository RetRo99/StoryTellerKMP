package com.retro99.server.audiobookshelf.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class AudiobookshelfLoginResponse(
    @SerialName("user")
    val user: AudiobookshelfUserApiModel,

    @SerialName("userDefaultLibraryId")
    val userDefaultLibraryId: String? = null,

    @SerialName("serverSettings")
    val serverSettings: AudiobookshelfServerSettingsApiModel? = null,

    @SerialName("Source")
    val source: String? = null,
)

@Serializable
data class AudiobookshelfUserApiModel(
    @SerialName("id")
    val id: String,

    @SerialName("username")
    val username: String,

    @SerialName("token")
    val token: String,

    @SerialName("type")
    val type: String? = null,

    @SerialName("isActive")
    val isActive: Boolean = true,

    @SerialName("createdAt")
    val createdAt: Long? = null,
)

@Serializable
data class AudiobookshelfServerSettingsApiModel(
    @SerialName("version")
    val version: String? = null,
)
