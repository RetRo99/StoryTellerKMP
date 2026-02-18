package com.retro99.server.api

/**
 * Factory for creating server-specific authenticators.
 */
interface ServerAuthenticatorFactory {
    /**
     * Create an authenticator for the given server type.
     */
    fun create(serverType: ServerType): ServerAuthenticator
}

