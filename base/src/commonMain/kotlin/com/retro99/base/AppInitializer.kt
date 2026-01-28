package com.retro99.base

/**
 * Interface for application initializers.
 *
 * Implementations of this interface are automatically discovered by Koin
 * and executed during application startup via `getAll<AppInitializer>()`.
 *
 * To create a new initializer, implement this interface and annotate the class with:
 * `@Single(binds = [AppInitializer::class])`
 */
interface AppInitializer {
    fun initialize()
}