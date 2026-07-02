package com.retro99.user.implementation

import co.touchlab.kermit.Logger
import com.retro99.base.AppInitializer
import com.retro99.user.api.UserRegistry
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.koin.core.annotation.Provided
import org.koin.core.annotation.Single

/**
 * Default user profile name.
 * This is the name shown for the automatically created profile.
 */
private const val DEFAULT_USER_NAME = "Default"

/**
 * Initializer that ensures a default user profile exists and is active.
 *
 * On first app launch (when no profiles exist), this creates a "Default" profile
 * and sets it as the active profile. This ensures the app always has an active
 * user context for database and server operations.
 *
 * For existing users (migration scenario), if profiles exist but none is active,
 * the first profile will be set as active.
 */
@Single(binds = [AppInitializer::class])
class DefaultUserInitializer(
    @Provided private val userRegistry: UserRegistry,
) : AppInitializer {

    private val logger = Logger.withTag("DefaultUserInitializer")

    override fun initialize() {
        initScope.launch {
            ensureDefaultUserExists()
        }
    }

    private val initScope = CoroutineScope(Dispatchers.Default)

    private suspend fun ensureDefaultUserExists() {
        // Check if any profiles exist
        if (!userRegistry.hasProfiles()) {
            // First launch - create default profile with fixed ID
            logger.i { "No user profiles found, creating default profile" }
            val defaultProfile = userRegistry.createProfile(
                id = UserRegistry.DEFAULT_USER_ID,
                name = DEFAULT_USER_NAME,
            )
            userRegistry.setActiveProfile(defaultProfile.id)
            logger.i { "Created and activated default profile: ${defaultProfile.id}" }
            return
        }

        // Profiles exist - ensure one is active
        if (!userRegistry.isProfileActive()) {
            logger.i { "No active profile, activating first available profile" }
            val profiles = userRegistry.getAllProfiles()
            val firstProfile = profiles.firstOrNull()
            if (firstProfile != null) {
                userRegistry.setActiveProfile(firstProfile.id)
                logger.i { "Activated profile: ${firstProfile.name} (${firstProfile.id})" }
            } else {
                // Edge case: hasProfiles() returned true but getAllProfiles() is empty
                // This shouldn't happen, but handle it gracefully
                logger.w { "Inconsistent state: hasProfiles=true but no profiles found" }
                val defaultProfile = userRegistry.createProfile(
                    id = UserRegistry.DEFAULT_USER_ID,
                    name = DEFAULT_USER_NAME,
                )
                userRegistry.setActiveProfile(defaultProfile.id)
                logger.i { "Created and activated default profile: ${defaultProfile.id}" }
            }
        } else {
            logger.d { "Active profile already set: ${userRegistry.getActiveProfileId()}" }
        }
    }
}

