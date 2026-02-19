package com.retro99.database.implementation

import com.retro99.database.api.DatabaseNameProvider
import com.retro99.user.api.UserRegistry
import org.koin.core.annotation.Provided
import org.koin.core.annotation.Single

@Single(binds = [DatabaseNameProvider::class])
class DatabaseNameProviderImpl(
    @Provided private val userRegistry: UserRegistry,
) : DatabaseNameProvider {

    override fun getDatabaseName(): String {
        val userId = userRegistry.getActiveProfileId()
            ?: throw IllegalStateException("No active user profile. Cannot determine database name.")
        return getDatabaseNameForUser(userId)
    }

    override fun getDatabaseNameForUser(userId: String): String {
        return DatabaseNameProvider.buildDatabaseName(userId)
    }

    override fun hasActiveUser(): Boolean {
        return userRegistry.isProfileActive()
    }

    override fun getActiveUserId(): String? {
        return userRegistry.getActiveProfileId()
    }
}

