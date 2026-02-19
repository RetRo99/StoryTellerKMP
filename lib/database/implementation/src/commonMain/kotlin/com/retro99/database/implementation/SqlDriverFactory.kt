package com.retro99.database.implementation

import app.cash.sqldelight.db.SqlDriver

/**
 * Factory for creating SqlDriver instances for specific users.
 * Platform-specific implementations handle the actual driver creation.
 */
interface SqlDriverFactory {
    /**
     * Create a SqlDriver for the specified user.
     * @param userId The user profile ID
     * @return A new SqlDriver instance for the user's database
     */
    fun createDriver(userId: String): SqlDriver

    /**
     * Delete the database file for a specific user.
     * Called when a user profile is deleted.
     * @param userId The user profile ID
     * @return true if deletion was successful or file didn't exist
     */
    fun deleteUserDatabase(userId: String): Boolean
}

