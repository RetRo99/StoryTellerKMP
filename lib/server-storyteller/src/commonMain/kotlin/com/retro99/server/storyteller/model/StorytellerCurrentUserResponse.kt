package com.retro99.server.storyteller.model

import kotlinx.serialization.Serializable

@Serializable
data class StorytellerCurrentUserResponse(
    val username: String? = null,
)
