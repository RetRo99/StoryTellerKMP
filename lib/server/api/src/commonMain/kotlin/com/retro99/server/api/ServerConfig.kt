package com.retro99.server.api

import kotlinx.serialization.Serializable

/**
 * Configuration for a registered server instance.
 */
@Serializable
data class ServerConfig(
    val id: String,                    // Unique identifier (UUID)
    val name: String,                  // User-defined display name
    val type: ServerType,              // Type of server
    val baseUrl: String,               // Base URL (e.g., "https://books.example.com")
    val addedAt: Long,                 // Timestamp when server was added
    val lastConnectedAt: Long? = null, // Last successful connection
)

