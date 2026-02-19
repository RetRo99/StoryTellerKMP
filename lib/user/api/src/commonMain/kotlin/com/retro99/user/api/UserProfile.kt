package com.retro99.user.api

import kotlinx.serialization.Serializable

/**
 * Represents a user profile on the device.
 * Each profile has its own servers, credentials, books, reading progress, and statistics.
 * Similar to Netflix/Plex profiles.
 */
@Serializable
data class UserProfile(
    val id: String,                    // UUID
    val name: String,                  // Display name ("Dad", "Mom", "Kid")
    val avatarId: Int? = null,         // Predefined avatar index (or null for default)
    val createdAt: Long,               // Timestamp when profile was created
    val lastActiveAt: Long? = null,    // Last time this profile was used
)

