package com.retro99.base.server

/**
 * Represents the type of media server.
 * Each type has different API endpoints, authentication methods, and capabilities.
 */
enum class ServerType(
    val identifier: String,
    val displayName: String,
) {
    Storyteller(
        identifier = "storyteller",
        displayName = "Storyteller",
    ),
    Local(
        identifier = "local",
        displayName = "Local",
    );

    companion object {
        fun fromIdentifier(identifier: String): ServerType? {
            return entries.find { it.identifier == identifier }
        }
    }
}

