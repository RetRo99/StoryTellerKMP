package com.retro99.server.storyteller

import com.retro99.server.api.ServerAuthenticator
import com.retro99.server.api.ServerAuthenticatorFactory
import com.retro99.server.api.ServerType
import org.koin.core.annotation.Provided
import org.koin.core.annotation.Single

/**
 * Factory for creating server-specific authenticators.
 * Currently only supports Storyteller servers.
 */
@Single(binds = [ServerAuthenticatorFactory::class])
class StorytellerAuthenticatorFactory(
    @Provided private val storytellerAuthenticator: StorytellerAuthenticator,
) : ServerAuthenticatorFactory {

    override fun create(serverType: ServerType): ServerAuthenticator {
        return when (serverType) {
            ServerType.Storyteller -> storytellerAuthenticator
            ServerType.Local -> throw IllegalArgumentException("Local server type does not require authentication")
            // Add other server types here as they are implemented
            // ServerType.Audiobookshelf -> audiobookshelfAuthenticator
            // ServerType.CalibreWeb -> calibreWebAuthenticator
        }
    }
}

