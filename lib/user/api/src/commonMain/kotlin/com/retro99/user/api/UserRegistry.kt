package com.retro99.user.api

import kotlinx.coroutines.flow.Flow

/**
 * Central registry for managing user profiles on the device.
 * Each profile is completely isolated with their own servers, credentials, and data.
 */
interface UserRegistry {

    companion object {
        /**
         * The default user ID used when no profile is active.
         * This is also the ID of the default profile created on first launch.
         */
        const val DEFAULT_USER_ID = "default"
    }

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
     * @param id Optional ID for the profile. If null, a random UUID will be generated.
     * @param name The display name for the profile.
     * @param avatarId Optional avatar ID for the profile.
     * @return The created UserProfile
     */
    suspend fun createProfile(id: String? = null, name: String, avatarId: Int? = null): UserProfile

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
     * Get the currently active profile ID, or the default user ID if no profile is active.
     * This is useful for file paths and preferences that need a user ID even before login.
     * @return The active profile ID or [DEFAULT_USER_ID] if no profile is active
     */
    fun getActiveProfileIdOrDefault(): String = getActiveProfileId() ?: DEFAULT_USER_ID

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

