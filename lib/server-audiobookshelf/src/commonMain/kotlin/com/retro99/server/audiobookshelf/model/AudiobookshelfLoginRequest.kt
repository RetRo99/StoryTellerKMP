package com.retro99.server.audiobookshelf.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class AudiobookshelfLoginRequest(
    @SerialName("username")
    val username: String,

    @SerialName("password")
    val password: String,
)
