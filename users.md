# Multi-User Profile Architecture Implementation Plan

## Overview

This document outlines the implementation plan for supporting multiple user profiles in StoryTellerKMP. Each user profile is completely isolated with their own servers, credentials, books, reading progress, and statistics - similar to Netflix/Plex profiles.

---

## Table of Contents

1. [Architecture Overview](#architecture-overview)
2. [Data Models](#data-models)
3. [Phase 1: User Profile Foundation](#phase-1-user-profile-foundation)
4. [Phase 2: Per-User Server Management](#phase-2-per-user-server-management)
5. [Phase 3: Per-User Database](#phase-3-per-user-database)
6. [Phase 4: UI Implementation](#phase-4-ui-implementation)
7. [Phase 5: Migration Strategy](#phase-5-migration-strategy)
8. [Storage Structure](#storage-structure)

---

## Architecture Overview

### Current Architecture
```
Device
└── ServerRegistry (shared)
    ├── Server A → 1 credential
    └── Server B → 1 credential
└── Single Database (all data)
```

### New Architecture
```
Device
└── UserRegistry (shared)
    ├── User Profile: Dad
    │   ├── ServerRegistry (Dad's servers)
    │   │   └── Storyteller → Dad's credentials
    │   └── Database: user_dad.db
    ├── User Profile: Mom
    │   ├── ServerRegistry (Mom's servers)
    │   │   ├── Storyteller → Mom's credentials
    │   │   └── Audiobookshelf → Mom's credentials
    │   └── Database: user_mom.db
    └── User Profile: Kid
        ├── ServerRegistry (Kid's servers)
        │   └── Storyteller → Kid's credentials
        └── Database: user_kid.db
```

---

## Data Models

### UserProfile

**File: `lib/user/api/src/commonMain/kotlin/com/retro99/user/api/UserProfile.kt`**

```kotlin
@Serializable
data class UserProfile(
    val id: String,                    // UUID
    val name: String,                  // Display name ("Dad", "Mom", "Kid")
    val avatarId: Int? = null,         // Predefined avatar index (or null for default)
    val createdAt: Long,               // Timestamp
    val lastActiveAt: Long? = null,    // Last time this profile was used
)
```

### UserRegistry Interface

**File: `lib/user/api/src/commonMain/kotlin/com/retro99/user/api/UserRegistry.kt`**

```kotlin
interface UserRegistry {
    // Profile Management
    fun observeAllProfiles(): Flow<List<UserProfile>>
    suspend fun getAllProfiles(): List<UserProfile>
    suspend fun createProfile(name: String, avatarId: Int? = null): UserProfile
    suspend fun updateProfile(profile: UserProfile)
    suspend fun deleteProfile(profileId: String)
    suspend fun getProfile(profileId: String): UserProfile?
    
    // Active Profile
    fun observeActiveProfile(): Flow<UserProfile?>
    suspend fun getActiveProfile(): UserProfile?
    suspend fun setActiveProfile(profileId: String)
    suspend fun clearActiveProfile()
    
    // Convenience
    suspend fun hasProfiles(): Boolean
    suspend fun isProfileActive(): Boolean
}
```

### Updated Preferences Keys

**File: `lib/preferences/api/.../PreferencesKey.kt`** (additions)

```kotlin
sealed class PreferencesKey(val name: String) {
    // Existing keys...
    
    // New user profile keys
    data object UserProfiles : PreferencesKey("UserProfiles")
    data object ActiveProfileId : PreferencesKey("ActiveProfileId")
    
    // Per-user keys will be prefixed with profile ID
    // e.g., "user_{profileId}_RegisteredServers"
}
```

---

## Phase 1: User Profile Foundation

### 1.1 Create User Module Structure

```
lib/user/
├── api/
│   ├── build.gradle.kts
│   └── src/commonMain/kotlin/com/retro99/user/api/
│       ├── UserProfile.kt
│       └── UserRegistry.kt
└── implementation/
    ├── build.gradle.kts
    └── src/commonMain/kotlin/com/retro99/user/implementation/
        ├── UserRegistryImpl.kt
        └── di/
            └── UserModule.kt
```

### 1.2 Implement UserRegistryImpl

```kotlin
@Single(binds = [UserRegistry::class])
class UserRegistryImpl(
    private val preferences: Preferences,
) : UserRegistry {

    private val _profiles = MutableStateFlow<Map<String, UserProfile>>(emptyMap())
    private val _activeProfileId = MutableStateFlow<String?>(null)
    
    init {
        loadFromPreferences()
    }
    
    // Implementation similar to ServerRegistryImpl pattern...
}
```

### 1.3 Add Profile-Scoped Preferences

Create a wrapper that automatically scopes preferences to the active user:

```kotlin
interface UserScopedPreferences : Preferences {
    val userId: String
}

@Factory
class UserScopedPreferencesImpl(
    private val preferences: Preferences,
    private val userRegistry: UserRegistry,
) : UserScopedPreferences {
    
    override val userId: String
        get() = userRegistry.getActiveProfileId() 
            ?: throw IllegalStateException("No active profile")
    
    private fun scopedKey(key: PreferencesKey): String {
        return "user_${userId}_${key.name}"
    }
    
    override fun getStringOrNull(key: PreferencesKey): String? {
        return preferences.getStringOrNull(scopedKey(key))
    }
    // ... other methods with scoping
}
```

---

## Phase 2: Per-User Server Management

### 2.1 Modify ServerRegistry to be User-Scoped

The key insight: `ServerRegistry` should operate on the **active user's** servers only.

**Option A: Inject user context into existing ServerRegistry**

```kotlin
@Single(binds = [ServerRegistry::class])
class ServerRegistryImpl(
    private val preferences: Preferences,
    private val userRegistry: UserRegistry,
) : ServerRegistry {

    private fun getPreferencesKeyForUser(baseKey: PreferencesKey): String {
        val userId = userRegistry.getActiveProfileIdOrThrow()
        return "user_${userId}_${baseKey.name}"
    }

    // All server/credential operations now scoped to active user
}
```

**Option B: Create ServerRegistry per user (Factory pattern)**

```kotlin
interface ServerRegistryFactory {
    fun getForUser(userId: String): ServerRegistry
    fun getForActiveUser(): ServerRegistry
}

@Single
class ServerRegistryFactoryImpl(
    private val preferences: Preferences,
    private val userRegistry: UserRegistry,
) : ServerRegistryFactory {

    private val registries = mutableMapOf<String, ServerRegistry>()

    override fun getForUser(userId: String): ServerRegistry {
        return registries.getOrPut(userId) {
            UserScopedServerRegistry(preferences, userId)
        }
    }

    override fun getForActiveUser(): ServerRegistry {
        val userId = userRegistry.getActiveProfileIdOrThrow()
        return getForUser(userId)
    }
}
```

**Recommendation:** Option A is simpler and requires fewer changes to existing code.

### 2.2 Update Credential Storage

Credentials are already per-server. With user-scoped preferences, they automatically become per-user-per-server:

```
Storage keys:
- user_dad123_RegisteredServers → [Storyteller]
- user_dad123_ServerCredentials → [{serverId: "st1", username: "john", ...}]
- user_mom456_RegisteredServers → [Storyteller, Audiobookshelf]
- user_mom456_ServerCredentials → [{serverId: "st1", ...}, {serverId: "abs1", ...}]
```

### 2.3 Handle User Switching

When active user changes, `ServerRegistry` needs to reload:

```kotlin
class ServerRegistryImpl(...) {

    init {
        // React to user changes
        userRegistry.observeActiveProfile()
            .onEach { profile ->
                if (profile != null) {
                    reloadForUser(profile.id)
                } else {
                    clearInMemoryState()
                }
            }
            .launchIn(scope)
    }

    private fun reloadForUser(userId: String) {
        // Clear current state
        _servers.value = emptyMap()
        _credentials.value = emptyMap()
        _activeServerId.value = null

        // Load this user's data
        loadFromPreferences(userId)
    }
}
```

---

## Phase 3: Per-User Database

### 3.1 Database Naming Strategy

Each user gets their own SQLite database file:

```kotlin
object DatabaseNaming {
    fun getDatabaseName(userId: String): String {
        return "storyteller_user_${userId}.db"
    }
}
```

### 3.2 Modify Database Provider

**Current:**
```kotlin
@Single
fun provideDatabase(): StorytellerDatabase {
    return createDatabase("storyteller.db")
}
```

**New:**
```kotlin
@Single
class DatabaseProvider(
    private val userRegistry: UserRegistry,
    private val databaseFactory: DatabaseFactory,
) {
    private var currentUserId: String? = null
    private var currentDatabase: StorytellerDatabase? = null

    fun getDatabase(): StorytellerDatabase {
        val activeUserId = userRegistry.getActiveProfileIdOrThrow()

        if (currentUserId != activeUserId) {
            currentDatabase?.close()
            currentDatabase = databaseFactory.create(
                DatabaseNaming.getDatabaseName(activeUserId)
            )
            currentUserId = activeUserId
        }

        return currentDatabase!!
    }
}
```

### 3.3 Database Cleanup on Profile Deletion

```kotlin
class UserRegistryImpl(...) {

    override suspend fun deleteProfile(profileId: String) {
        // 1. Clear preferences for this user
        clearUserPreferences(profileId)

        // 2. Delete user's database file
        val dbName = DatabaseNaming.getDatabaseName(profileId)
        databaseFileManager.deleteDatabase(dbName)

        // 3. Remove profile from registry
        _profiles.update { it - profileId }
        persistProfiles()

        // 4. If this was active profile, clear active
        if (_activeProfileId.value == profileId) {
            _activeProfileId.value = null
            persistActiveProfile()
        }
    }
}
```

---

## Phase 4: UI Implementation

### 4.1 Profile Selection Screen

**New screen shown on app launch (if multiple profiles exist):**

```
┌─────────────────────────────────────┐
│           Who's Reading?            │
│                                     │
│    ┌───┐    ┌───┐    ┌───┐         │
│    │ 👤│    │ 👤│    │ 👤│         │
│    └───┘    └───┘    └───┘         │
│     Dad      Mom      Kid          │
│                                     │
│          [+ Add Profile]            │
│                                     │
│    ☐ Remember my choice             │
└─────────────────────────────────────┘
```

**File: `feature/profile/ui/src/commonMain/kotlin/.../ProfileSelectionScreen.kt`**

### 4.2 Profile Management in Settings

Add to existing settings screen:

```
Settings
├── Profile: Dad [Switch]
│   ├── Edit Profile
│   └── Delete Profile
├── Servers (Dad's servers)
│   ├── Storyteller ✓
│   └── [+ Add Server]
├── Appearance
└── About
```

### 4.3 Navigation Flow

```kotlin
sealed class AppDestination {
    data object ProfileSelection : AppDestination()
    data object Home : AppDestination()
    // ... existing destinations
}

// In root navigation
@Composable
fun AppNavigation(userRegistry: UserRegistry) {
    val activeProfile by userRegistry.observeActiveProfile().collectAsState(null)
    val profiles by userRegistry.observeAllProfiles().collectAsState(emptyList())

    val startDestination = when {
        profiles.isEmpty() -> AppDestination.ProfileCreation
        activeProfile == null -> AppDestination.ProfileSelection
        else -> AppDestination.Home
    }

    NavHost(startDestination = startDestination) {
        // ...
    }
}
```

### 4.4 Profile Switcher Component

Quick-switch in app header or drawer:

```kotlin
@Composable
fun ProfileSwitcher(
    currentProfile: UserProfile,
    allProfiles: List<UserProfile>,
    onProfileSelected: (UserProfile) -> Unit,
) {
    // Dropdown or bottom sheet with profile avatars
}
```

---

## Phase 5: Migration Strategy

### 5.1 First Launch After Update

When user updates to the multi-profile version:

```kotlin
class MigrationManager(
    private val preferences: Preferences,
    private val userRegistry: UserRegistry,
) {
    suspend fun migrateIfNeeded() {
        val migrationVersion = preferences.getInt(PreferencesKey.MigrationVersion, 0)

        if (migrationVersion < 1) {
            migrateToUserProfiles()
            preferences.putInt(PreferencesKey.MigrationVersion, 1)
        }
    }

    private suspend fun migrateToUserProfiles() {
        // 1. Create default profile
        val defaultProfile = userRegistry.createProfile(
            name = "Default",  // Or prompt user for name
            avatarId = null,
        )

        // 2. Move existing server configs to this profile
        val existingServers = preferences.getObject<List<ServerConfig>>(
            PreferencesKey.RegisteredServers
        )
        val existingCredentials = preferences.getObject<List<ServerCredentials>>(
            PreferencesKey.ServerCredentials
        )

        // 3. Save under user-scoped keys
        val userKey = "user_${defaultProfile.id}_RegisteredServers"
        preferences.putObject(userKey, existingServers)
        // ... same for credentials

        // 4. Rename existing database
        databaseFileManager.renameDatabase(
            from = "storyteller.db",
            to = DatabaseNaming.getDatabaseName(defaultProfile.id)
        )

        // 5. Set as active profile
        userRegistry.setActiveProfile(defaultProfile.id)

        // 6. Clean up old keys
        preferences.remove(PreferencesKey.RegisteredServers)
        preferences.remove(PreferencesKey.ServerCredentials)
    }
}
```

### 5.2 Migration UI

Show a one-time welcome screen explaining the new feature:

```
┌─────────────────────────────────────┐
│        Welcome to Profiles!         │
│                                     │
│  We've created a profile for you    │
│  with all your existing data.       │
│                                     │
│  You can now add more profiles      │
│  for family members!                │
│                                     │
│  Profile Name: [_______________]    │
│                                     │
│           [Get Started]             │
└─────────────────────────────────────┘
```

---

## Storage Structure

### Final Storage Layout

```
/shared_preferences/
  └── SecureSettings
      ├── UserProfiles              → [{id: "abc", name: "Dad", ...}, ...]
      ├── ActiveProfileId           → "abc"
      ├── MigrationVersion          → 1
      │
      ├── user_abc_RegisteredServers    → [ServerConfig, ...]
      ├── user_abc_ServerCredentials    → [ServerCredentials, ...]
      ├── user_abc_ActiveServerId       → "server123"
      ├── user_abc_ReaderSettings       → {...}
      │
      ├── user_def_RegisteredServers    → [ServerConfig, ...]
      ├── user_def_ServerCredentials    → [ServerCredentials, ...]
      └── ...

/databases/
  ├── storyteller_user_abc.db      # Dad's data
  ├── storyteller_user_def.db      # Mom's data
  └── storyteller_user_ghi.db      # Kid's data
```

---

## Implementation Order

### Week 1: Foundation
1. [ ] Create `lib/user/api` module with `UserProfile` and `UserRegistry`
2. [ ] Create `lib/user/implementation` with `UserRegistryImpl`
3. [ ] Add new `PreferencesKey` entries
4. [ ] Write unit tests for `UserRegistry`

### Week 2: Server Integration
5. [ ] Modify `ServerRegistryImpl` to be user-scoped
6. [ ] Update `ServerTokenProvider` to work with active user
7. [ ] Test server switching between users

### Week 3: Database
8. [ ] Create `DatabaseProvider` with per-user database support
9. [ ] Implement database file management (create/delete)
10. [ ] Update all database consumers to use `DatabaseProvider`

### Week 4: UI
11. [ ] Create Profile Selection screen
12. [ ] Add Profile Management to Settings
13. [ ] Implement Profile Switcher component
14. [ ] Update navigation flow

### Week 5: Migration & Polish
15. [ ] Implement `MigrationManager`
16. [ ] Add migration welcome screen
17. [ ] End-to-end testing
18. [ ] Edge cases (delete active profile, etc.)

---

## Open Questions

1. **Profile PIN/Password?** - Should profiles be protected?
2. **Profile avatars** - Predefined set or custom images?
3. **Profile limits** - Maximum number of profiles?
4. **Sync profiles across devices?** - Or device-local only?
5. **Guest profile?** - Temporary profile that auto-deletes?

---

## Risk Assessment

| Risk | Impact | Mitigation |
|------|--------|------------|
| Data loss during migration | High | Backup before migration, thorough testing |
| Performance with many profiles | Medium | Lazy-load databases, limit profile count |
| Complexity increase | Medium | Good abstractions, comprehensive tests |
| User confusion | Low | Clear onboarding, simple UI |

