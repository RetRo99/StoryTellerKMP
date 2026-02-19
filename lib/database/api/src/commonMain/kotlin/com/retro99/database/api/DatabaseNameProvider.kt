package com.retro99.database.api

/**
 * Provides the database name based on the active user profile.
 * Each user gets their own isolated database.
 */
interface DatabaseNameProvider {
    /**
     * Get the database name for the current active user.
     * @return Database filename (e.g., "parrot_user_abc123.db")
     * @throws IllegalStateException if no user is active
     */
    fun getDatabaseName(): String

    /**
     * Get the database name for a specific user.
     * @param userId The user profile ID
     * @return Database filename (e.g., "parrot_user_abc123.db")
     */
    fun getDatabaseNameForUser(userId: String): String

    /**
     * Check if a user profile is currently active.
     */
    fun hasActiveUser(): Boolean

    /**
     * Get the active user ID, or null if no user is active.
     */
    fun getActiveUserId(): String?

    companion object {
        const val DATABASE_PREFIX = "parrot_user_"
        const val DATABASE_SUFFIX = ".db"

        fun buildDatabaseName(userId: String): String {
            return "$DATABASE_PREFIX$userId$DATABASE_SUFFIX"
        }
    }
}

