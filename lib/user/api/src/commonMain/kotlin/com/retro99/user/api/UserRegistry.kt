package com.retro99.user.api

import kotlinx.coroutines.flow.Flow

/**
 * Central registry for managing user profiles on the device.
 * Each profile is completely isolated with their own servers, credentials, and data.
 */
interface UserRegistry {

    // ==================== Profile Management ====================

    /**
     * Observe all user profiles on the device.
     */
    fun observeAllProfiles(): Flow<List<UserProfile>>

    /**
     * Get all user profiles (suspend version).
     */
    suspend fun getAllProfiles(): List<UserProfile>

    /**
     * Create a new user profile.
     * @return The created UserProfile with generated ID
     */
    suspend fun createProfile(name: String, avatarId: Int? = null): UserProfile

    /**
     * Update an existing profile.
     */
    suspend fun updateProfile(profile: UserProfile)

    /**
     * Delete a profile and all associated data (servers, credentials, database).
     */
    suspend fun deleteProfile(profileId: String)

    /**
     * Get a specific profile by ID.
     */
    suspend fun getProfile(profileId: String): UserProfile?

    // ==================== Active Profile ====================

    /**
     * Observe the currently active profile.
     */
    fun observeActiveProfile(): Flow<UserProfile?>

    /**
     * Get the currently active profile (suspend version).
     */
    suspend fun getActiveProfile(): UserProfile?

    /**
     * Get the currently active profile ID.
     * @return The active profile ID or null if no profile is active
     */
    fun getActiveProfileId(): String?

    /**
     * Set the active profile.
     * This will trigger reloading of user-scoped data (servers, database, etc.)
     */
    suspend fun setActiveProfile(profileId: String)

    /**
     * Clear the active profile (logout from profile).
     */
    suspend fun clearActiveProfile()

    // ==================== Convenience ====================

    /**
     * Check if any profiles exist on the device.
     */
    suspend fun hasProfiles(): Boolean

    /**
     * Check if a profile is currently active.
     */
    fun isProfileActive(): Boolean
}

