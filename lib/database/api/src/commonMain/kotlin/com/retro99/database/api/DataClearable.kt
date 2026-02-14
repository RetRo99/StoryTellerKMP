package com.retro99.database.api

/**
 * Interface for databases that can be cleared.
 *
 * Implementations of this interface are automatically discovered by Koin
 * and cleared during logout via `getAll<DataClearable>()`.
 *
 * To make a database clearable, implement this interface and ensure the database
 * is registered with Koin using: `@Single(binds = [YourDatabase::class, DataClearable::class])`
 */
interface DataClearable {

    suspend fun clearAllData()
}

