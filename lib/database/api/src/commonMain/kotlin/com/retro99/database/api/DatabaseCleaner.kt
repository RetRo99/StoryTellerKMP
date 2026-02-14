package com.retro99.database.api

/**
 * Interface for clearing all data from the database.
 * Used during logout to ensure user data is removed.
 */
interface DatabaseCleaner {

    suspend fun clearAllData()
}

