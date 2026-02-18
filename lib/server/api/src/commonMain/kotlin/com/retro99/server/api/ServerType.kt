package com.retro99.server.api

import kotlinx.serialization.Serializable

/**
 * Represents the type of media server.
 * Each type has different API endpoints, authentication methods, and capabilities.
 */
@Serializable
sealed class ServerType {
    abstract val identifier: String
    abstract val displayName: String

    @Serializable
    data object Storyteller : ServerType() {
        override val identifier = "storyteller"
        override val displayName = "Storyteller"
    }

    @Serializable
    data object Local : ServerType() {
        override val identifier = "local"
        override val displayName = "Local"
    }

    // Future server types can be added here
}

