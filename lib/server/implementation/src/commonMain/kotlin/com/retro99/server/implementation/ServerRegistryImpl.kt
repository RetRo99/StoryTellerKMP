package com.retro99.server.implementation

import co.touchlab.kermit.Logger
import com.retro99.preferences.api.Preferences
import com.retro99.preferences.api.PreferencesKey
import com.retro99.preferences.api.getObject
import com.retro99.preferences.api.putObject
import com.retro99.server.api.ServerAuthState
import com.retro99.server.api.ServerConfig
import com.retro99.server.api.ServerCredentials
import com.retro99.server.api.ServerRegistry
import com.retro99.server.api.ServerType
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlin.time.Clock
import org.koin.core.annotation.Single
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

@Single(binds = [ServerRegistry::class])
class ServerRegistryImpl(
    private val preferences: Preferences,
) : ServerRegistry {

    private val logger = Logger.withTag("čič-ServerRegistry")

    private val mutex = Mutex()

    // In-memory cache backed by preferences
    private val _servers = MutableStateFlow<Map<String, ServerConfig>>(emptyMap())
    private val _credentials = MutableStateFlow<Map<String, ServerCredentials>>(emptyMap())
    private val _activeServerId = MutableStateFlow<String?>(null)

    init {
        loadFromPreferences()
    }

    private fun loadFromPreferences() {
        // Load servers
        val servers = preferences.getObject<List<ServerConfig>>(PreferencesKey.RegisteredServers)
        if (servers != null) {
            _servers.value = servers.associateBy { it.id }
            logger.d { "Loaded ${servers.size} servers from preferences" }
        }

        // Load credentials
        val credentials = preferences.getObject<List<ServerCredentials>>(PreferencesKey.ServerCredentials)
        if (credentials != null) {
            _credentials.value = credentials.associateBy { it.serverId }
            logger.d { "Loaded ${credentials.size} credentials from preferences" }
        }

        // Load active server
        val activeId = preferences.getStringOrNull(PreferencesKey.ActiveServerId)
        _activeServerId.value = activeId
        logger.d { "Loaded active server: $activeId" }
    }

    // ==================== Server Management ====================

    override fun observeAllServers(): Flow<List<ServerConfig>> {
        return _servers.map { it.values.toList().sortedBy { server -> server.name } }
    }

    override suspend fun getAllServers(): List<ServerConfig> {
        return _servers.value.values.toList()
    }

    @OptIn(ExperimentalUuidApi::class)
    override suspend fun addServer(
        name: String,
        type: ServerType,
        baseUrl: String,
    ): ServerConfig = mutex.withLock {
        val config = ServerConfig(
            id = Uuid.random().toString(),
            name = name,
            type = type,
            baseUrl = baseUrl.trimEnd('/'),
            addedAt = Clock.System.now().toEpochMilliseconds(),
        )

        _servers.update { it + (config.id to config) }
        persistServers()

        config
    }

    override suspend fun updateServer(config: ServerConfig) = mutex.withLock {
        _servers.update { it + (config.id to config) }
        persistServers()
    }

    override suspend fun removeServer(serverId: String) = mutex.withLock {
        _servers.update { it - serverId }
        _credentials.update { it - serverId }

        if (_activeServerId.value == serverId) {
            _activeServerId.value = null
            persistActiveServer()
        }

        persistServers()
        persistCredentials()
    }

    override suspend fun getServer(serverId: String): ServerConfig? {
        return _servers.value[serverId]
    }

    // ==================== Authentication State ====================

    override fun observeAllAuthStates(): Flow<Map<String, ServerAuthState>> {
        return combine(_servers, _credentials) { servers, creds ->
            servers.keys.associateWith { serverId ->
                getAuthStateForServer(serverId, creds[serverId])
            }
        }
    }

    override fun observeAuthState(serverId: String): Flow<ServerAuthState> {
        return _credentials.map { creds ->
            getAuthStateForServer(serverId, creds[serverId])
        }
    }

    override fun observeAuthenticatedServers(): Flow<List<ServerConfig>> {
        return combine(_servers, _credentials) { servers, creds ->
            logger.d { "observeAuthenticatedServers: ${servers.size} servers, ${creds.size} credentials" }
            logger.d { "Servers: ${servers.values.map { it.name }}" }
            logger.d { "Credentials for: ${creds.keys}" }
            servers.values.filter { server ->
                creds.containsKey(server.id)
            }.toList().also {
                logger.d { "Authenticated servers: ${it.size} - ${it.map { s -> s.name }}" }
            }
        }
    }

    override suspend fun isAuthenticated(serverId: String): Boolean {
        return _credentials.value.containsKey(serverId)
    }

    override suspend fun getAuthenticatedServers(): List<ServerConfig> {
        val creds = _credentials.value
        return _servers.value.values.filter { creds.containsKey(it.id) }
    }

    private fun getAuthStateForServer(
        serverId: String,
        credential: ServerCredentials?
    ): ServerAuthState {
        if (credential == null) {
            return ServerAuthState.NotAuthenticated(serverId)
        }

        val expiresAt = credential.expiresAt
        val now = Clock.System.now().toEpochMilliseconds()
        if (expiresAt != null && expiresAt < now) {
            return ServerAuthState.TokenExpired(serverId, expiresAt)
        }

        return ServerAuthState.Authenticated(
            serverId = serverId,
            username = credential.username,
            authenticatedAt = now,
        )
    }

    // ==================== Credentials Management ====================

    override suspend fun saveCredentials(credentials: ServerCredentials) = mutex.withLock {
        _credentials.update { it + (credentials.serverId to credentials) }
        persistCredentials()
    }

    override suspend fun getCredentials(serverId: String): ServerCredentials? {
        return _credentials.value[serverId]
    }

    override suspend fun clearCredentials(serverId: String) = mutex.withLock {
        _credentials.update { it - serverId }
        persistCredentials()
    }

    override suspend fun clearAllCredentials() = mutex.withLock {
        _credentials.value = emptyMap()
        persistCredentials()
    }

    // ==================== Active Server ====================

    override fun observeActiveServer(): Flow<ServerConfig?> {
        return combine(_activeServerId, _servers) { activeId, servers ->
            activeId?.let { servers[it] }
        }
    }

    override suspend fun getActiveServer(): ServerConfig? {
        val activeId = _activeServerId.value ?: return null
        return _servers.value[activeId]
    }

    override suspend fun setActiveServer(serverId: String) = mutex.withLock {
        _activeServerId.value = serverId
        persistActiveServer()
    }

    override suspend fun clearActiveServer() = mutex.withLock {
        _activeServerId.value = null
        persistActiveServer()
    }

    // ==================== Persistence ====================

    private fun persistServers() {
        val serversList = _servers.value.values.toList()
        preferences.putObject(PreferencesKey.RegisteredServers, serversList)
        logger.d { "Persisted ${serversList.size} servers" }
    }

    private fun persistCredentials() {
        val credentialsList = _credentials.value.values.toList()
        preferences.putObject(PreferencesKey.ServerCredentials, credentialsList)
        logger.d { "Persisted ${credentialsList.size} credentials" }
    }

    private fun persistActiveServer() {
        val activeId = _activeServerId.value
        if (activeId != null) {
            preferences.putString(PreferencesKey.ActiveServerId, activeId)
        } else {
            preferences.remove(PreferencesKey.ActiveServerId)
        }
        logger.d { "Persisted active server: $activeId" }
    }
}

