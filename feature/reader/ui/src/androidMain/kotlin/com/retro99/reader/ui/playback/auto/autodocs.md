# Android Media Playback Architecture Refactoring

## Executive Summary

This document outlines the plan to refactor the Android media playback architecture from a **UI-dependent model** (ReaderScope owns playback state) to a **service-centric model** (MediaPlaybackService owns everything), enabling:
- Background playback when leaving the reader
- Android Auto support
- Proper reconnection when re-entering a book

---

## Current Architecture Problems

### Problem 1: ExoPlayer Ownership Confusion
- `MediaPlaybackService` creates ExoPlayer in `onCreate()`
- BUT `MediaOverlayPlayer.release()` calls `exoPlayer.release()` on the service's player
- When ReaderScope closes, it destroys the service's player

### Problem 2: SMIL Clips Scoped to ReaderScope
- `SmilClipCache` is `@Scope(ReaderScope::class)`
- Clips are lost when leaving the reader screen
- Service cannot access clips for position tracking or Android Auto

### Problem 3: LocatorTracker Constructor Injection
- `LocatorTracker` takes `ExoPlayer` as constructor parameter
- Tightly coupled to ReaderScope lifecycle
- Cannot survive reader screen exit

### Problem 4: MediaSession in Wrong Place
- `MediaSessionManager` (ReaderScope) creates MediaSession
- Session is released when leaving reader
- Notification disappears, playback stops

### Problem 5: Service Not Android Auto Compatible
- Uses `MediaSessionService` (not `MediaLibraryService`)
- `exported="false"` in manifest
- No browsing support for Android Auto

---

## Target Architecture

### Storyteller's Approach (Reference Implementation)

| Component | Storyteller | Purpose |
|-----------|-------------|---------|
| `PlaybackService` | `MediaLibraryService` | Owns ExoPlayer, MediaSession, clip scheduling |
| `BookService` | Singleton (`object`) | Global clip storage, accessible anywhere |
| `AudiobookPlayer` | `MediaController` client | Connects to service, receives broadcasts |
| Clip Sync | `PlayerMessage` scheduling | Service schedules callbacks at clip boundaries |
| UI Notification | `CLIP_CHANGED` broadcast | Custom session command to all clients |

### Key Design Principles

1. **Service owns everything playback-related** - ExoPlayer, MediaSession, position tracking
2. **Clips stored globally** - Singleton repository, not ReaderScope
3. **UI is just a client** - Connects via MediaController, receives broadcasts
4. **Reconnection is trivial** - Just connect new MediaController to running service

---

## Component Migration Plan

### Components Moving TO Service

| Component | Current Location | New Location | Reason |
|-----------|-----------------|--------------|--------|
| ExoPlayer | Service (correct) | Service | Already correct |
| MediaSession | MediaSessionManager (ReaderScope) | Service | Must survive UI |
| Position Tracking | LocatorTracker (ReaderScope) | Service | Clip scheduling |
| Playback State | PlaybackStateTracker (ReaderScope) | Service | Background playback |
| Audio Focus | AudioFocusManager (ReaderScope) | Service | System integration |

### Components Moving TO Singleton

| Component | Current Location | New Location | Reason |
|-----------|-----------------|--------------|--------|
| SMIL Clips | SmilClipCache (ReaderScope) | SmilClipRepository (@Single) | Global access |
| Book Metadata | Scattered | PlayingBookInfo (@Single) | Reconnection |

### Components STAYING in ReaderScope

| Component | Reason |
|-----------|--------|
| `MediaOverlayPlayer` | Becomes thin MediaController client |
| `HighlightController` (new) | Receives CLIP_CHANGED, updates UI |
| `ReaderSyncCoordinator` | Coordinates book ↔ audio in UI |
| `BookController` | UI navigation only |

---

## Phase 1: Create SmilClipRepository (Singleton)

### Purpose
Global storage for parsed SMIL clips, accessible by both ReaderScope and Service.

### New File: `SmilClipRepository.kt`

```kotlin
@Single
class SmilClipRepository {
    private val lock = Any()
    
    // Map of bookUuid -> Map of chapterHref -> List<MediaOverlayClip>
    private val clipsByBook = mutableMapOf<String, MutableMap<String, List<MediaOverlayClip>>>()
    
    // Currently playing book info
    private var _currentBook: PlayingBookInfo? = null
    val currentBook: PlayingBookInfo? get() = synchronized(lock) { _currentBook }
    
    fun setCurrentBook(info: PlayingBookInfo) {
        synchronized(lock) { _currentBook = info }
    }
    
    fun storeClips(bookUuid: String, chapterHref: String, clips: List<MediaOverlayClip>) {
        synchronized(lock) {
            clipsByBook.getOrPut(bookUuid) { mutableMapOf() }[chapterHref] = clips
        }
    }
    
    fun getClips(bookUuid: String, chapterHref: String): List<MediaOverlayClip>? {
        return synchronized(lock) { clipsByBook[bookUuid]?.get(chapterHref) }
    }
    
    fun getAllClipsForBook(bookUuid: String): Map<String, List<MediaOverlayClip>>? {
        return synchronized(lock) { clipsByBook[bookUuid]?.toMap() }
    }
    
    fun clearBook(bookUuid: String) {
        synchronized(lock) { clipsByBook.remove(bookUuid) }
    }
}
```

### Data Class: `PlayingBookInfo.kt`

```kotlin
data class PlayingBookInfo(
    val bookUuid: String,
    val serverId: String,
    val bookType: BookType,
    val title: String,
    val currentChapterHref: String,
    val coverArtwork: ByteArray? = null,
)
```

### Migration Steps

1. Create `SmilClipRepository` as `@Single`
2. Modify `SmilLoadingManager` to store clips in repository after parsing
3. Keep `SmilClipCache` temporarily for backward compatibility
4. Update service to read clips from repository

---

## Phase 2: Upgrade MediaPlaybackService to MediaLibraryService

### Purpose
Full Android Auto support with browsing and self-sufficient playback.

### Key Changes

```kotlin
@OptIn(UnstableApi::class)
class MediaPlaybackService : MediaLibraryService() {

    // Service owns these
    private var player: ExoPlayer? = null
    private var mediaSession: MediaLibrarySession? = null

    // Injected singletons
    private val clipRepository: SmilClipRepository by inject()
    private val controller: MediaPlaybackController by inject()

    // Clip-to-position mapping for current playlist
    private var mediaIdToClips = mapOf<String, List<MediaOverlayClip>>()

    override fun onCreate() {
        super.onCreate()

        player = ExoPlayer.Builder(this)
            .setAudioAttributes(audioAttributes, true)
            .setHandleAudioBecomingNoisy(true)
            .setWakeMode(C.WAKE_MODE_LOCAL)
            .build()

        player?.addListener(PlaybackListener())

        mediaSession = MediaLibrarySession.Builder(this, player!!, LibraryCallback())
            .setSessionActivity(createSessionActivityIntent())
            .build()

        controller.onServiceCreated(this, player!!, mediaSession!!)
    }

    override fun onGetSession(controllerInfo: MediaSession.ControllerInfo): MediaLibrarySession? {
        return mediaSession
    }

    // ... browsing callbacks for Android Auto
}
```

### Clip Scheduling (Storyteller Pattern)

```kotlin
private fun scheduleClipMessages(mediaItem: MediaItem, trackIndex: Int) {
    val clips = mediaIdToClips[mediaItem.mediaId] ?: return

    clips.forEach { clip ->
        player?.createMessage { messageType, payload ->
            val clipPayload = payload as MediaOverlayClip
            notifyClipChanged(clipPayload)
        }?.apply {
            setPosition(trackIndex, (clip.startTime * 1000).toLong())
            setPayload(clip)
            setDeleteAfterDelivery(false)
            send()
        }
    }
}

private fun notifyClipChanged(clip: MediaOverlayClip) {
    val bundle = Bundle().apply {
        putString("fragmentId", clip.fragmentId)
        putString("textHref", clip.textHref.toString())
        putDouble("startTime", clip.startTime)
        putDouble("endTime", clip.endTime)
    }

    mediaSession?.connectedControllers?.forEach { controller ->
        mediaSession?.sendCustomCommand(
            controller,
            SessionCommand(COMMAND_CLIP_CHANGED, Bundle.EMPTY),
            bundle
        )
    }
}

companion object {
    const val COMMAND_CLIP_CHANGED = "com.retro99.CLIP_CHANGED"
    const val COMMAND_LOAD_BOOK = "com.retro99.LOAD_BOOK"
}
```

---

## Phase 3: Refactor MediaOverlayPlayer as MediaController Client

### Purpose
Remove direct ExoPlayer access; communicate only via MediaController.

### Current vs New

| Aspect | Current | New |
|--------|---------|-----|
| Player access | `controller.currentPlayer` (direct) | `MediaController` (IPC) |
| Position updates | Poll every 100ms | Receive `CLIP_CHANGED` broadcasts |
| Play/Pause | `exoPlayer.play()` | `mediaController.play()` |
| Seek | `exoPlayer.seekTo()` | `mediaController.seekTo()` |
| Release | `exoPlayer.release()` ❌ | `mediaController.release()` ✅ |

### New Structure

```kotlin
@Scope(ReaderScope::class)
@Scoped
class MediaOverlayPlayer(
    private val context: Context,
    private val epubPublication: EpubPublication,
    private val clipRepository: SmilClipRepository,
    private val smilLoadingManager: SmilLoadingManager,
    // ... other dependencies
) {
    private var mediaController: MediaController? = null
    private val controllerCallback = ControllerCallback()

    suspend fun connect() {
        val sessionToken = SessionToken(
            context,
            ComponentName(context, MediaPlaybackService::class.java)
        )

        mediaController = MediaController.Builder(context, sessionToken)
            .setListener(controllerCallback)
            .buildAsync()
            .await()
    }

    fun play() {
        mediaController?.play()
    }

    fun pause() {
        mediaController?.pause()
    }

    fun seekTo(positionMs: Long) {
        mediaController?.seekTo(positionMs)
    }

    /**
     * Release the MediaController connection.
     * Does NOT release ExoPlayer - that belongs to the service.
     */
    fun release() {
        mediaController?.release()
        mediaController = null
        // DO NOT call exoPlayer.release() - service owns it!
    }

    private inner class ControllerCallback : MediaController.Listener {
        override fun onCustomCommand(
            controller: MediaController,
            command: SessionCommand,
            args: Bundle
        ): ListenableFuture<SessionResult> {
            when (command.customAction) {
                MediaPlaybackService.COMMAND_CLIP_CHANGED -> {
                    val fragmentId = args.getString("fragmentId")
                    val textHref = args.getString("textHref")
                    // Update highlights in UI
                    onClipChanged(fragmentId, textHref)
                }
            }
            return Futures.immediateFuture(SessionResult(SessionResult.RESULT_SUCCESS))
        }
    }
}
```

---

## Phase 4: Remove LocatorTracker ExoPlayer Dependency

### Current Problem

```kotlin
@Scope(ReaderScope::class)
@Scoped
class LocatorTracker(
    private val player: ExoPlayer,  // ❌ Direct dependency
    private val initialAudioPosition: InitialAudioPosition,
)
```

### Solution Options

**Option A: Remove LocatorTracker entirely**
- Service handles all position tracking via PlayerMessage
- UI receives CLIP_CHANGED broadcasts
- Simplest approach, matches Storyteller

**Option B: Refactor to use MediaController**
- LocatorTracker connects via MediaController
- Still polls position but through IPC
- More complex, less efficient

**Recommended: Option A**

The service schedules `PlayerMessage` callbacks at clip boundaries. When a clip starts, the service broadcasts `CLIP_CHANGED` to all connected clients. The UI receives this and updates highlights.

### Migration

1. Move position tracking logic into `MediaPlaybackService`
2. Create `ClipScheduler` helper class in service
3. Remove `LocatorTracker` class entirely
4. Create `HighlightController` in ReaderScope that receives broadcasts

---

## Phase 5: Reconnection Support

### Purpose
When re-entering a book that's already playing, connect to existing playback.

### Flow

```
User opens Book A → ReaderScope created → MediaOverlayPlayer connects
User leaves reader → ReaderScope destroyed → MediaController released
                     Service continues playing → Notification visible

User opens Book A again → ReaderScope created
                        → Check MediaPlaybackController.currentPlayingBook
                        → Same book? Connect and restore UI state
                        → Different book? Load new book in service
```

### Implementation in MediaPlaybackController

```kotlin
@Single
class MediaPlaybackController {
    // Track what's currently playing
    private var _currentPlayingBook: PlayingBookInfo? = null
    val currentPlayingBook: PlayingBookInfo?
        get() = synchronized(lock) { _currentPlayingBook }

    fun isPlayingBook(bookUuid: String): Boolean {
        return synchronized(lock) {
            _currentPlayingBook?.bookUuid == bookUuid && _player?.isPlaying == true
        }
    }

    fun getCurrentPosition(): Long? {
        return synchronized(lock) { _player?.currentPosition }
    }

    fun getCurrentChapterHref(): String? {
        return synchronized(lock) { _currentPlayingBook?.currentChapterHref }
    }
}
```

### Implementation in MediaOverlayPlayer

```kotlin
suspend fun initialize(initialChapterHref: String? = null) {
    val controller = mediaPlaybackController

    // Check if this book is already playing
    if (controller.isPlayingBook(epubPublication.bookUuid)) {
        // Reconnect to existing playback
        connect()
        restoreUIState(
            currentPosition = controller.getCurrentPosition(),
            currentChapter = controller.getCurrentChapterHref()
        )
        return
    }

    // New book - load fresh
    smilLoadingManager.initialize(playerScope)
    connect()
    loadBook()
}
```

---

## Phase 6: Android Auto Integration

### Manifest Changes

```xml
<service
    android:name=".playback.MediaPlaybackService"
    android:exported="true"
    android:foregroundServiceType="mediaPlayback">

    <intent-filter>
        <action android:name="androidx.media3.session.MediaLibraryService" />
        <action android:name="android.media.browse.MediaBrowserService" />
    </intent-filter>
</service>

<meta-data
    android:name="com.google.android.gms.car.application"
    android:resource="@xml/automotive_app_desc" />
```

### Create `res/xml/automotive_app_desc.xml`

```xml
<?xml version="1.0" encoding="utf-8"?>
<automotiveApp>
    <uses name="media" />
</automotiveApp>
```

### MediaLibrarySession.Callback Implementation

```kotlin
private inner class LibraryCallback : MediaLibrarySession.Callback {

    override fun onGetLibraryRoot(
        session: MediaLibrarySession,
        browser: MediaSession.ControllerInfo,
        params: LibraryParams?
    ): ListenableFuture<LibraryResult<MediaItem>> {
        val rootItem = MediaItem.Builder()
            .setMediaId("root")
            .setMediaMetadata(
                MediaMetadata.Builder()
                    .setIsBrowsable(true)
                    .setIsPlayable(false)
                    .setTitle("Parrot")
                    .build()
            )
            .build()
        return Futures.immediateFuture(LibraryResult.ofItem(rootItem, params))
    }

    override fun onGetChildren(
        session: MediaLibrarySession,
        browser: MediaSession.ControllerInfo,
        parentId: String,
        page: Int,
        pageSize: Int,
        params: LibraryParams?
    ): ListenableFuture<LibraryResult<ImmutableList<MediaItem>>> {
        // Return list of books or chapters based on parentId
        return when (parentId) {
            "root" -> getBooksList()
            else -> getChaptersList(parentId)
        }
    }

    override fun onPlaybackResumption(
        session: MediaLibrarySession,
        controller: MediaSession.ControllerInfo
    ): ListenableFuture<MediaSession.MediaItemsWithStartPosition> {
        // Return last played book and position
        val lastPlayed = clipRepository.currentBook ?: return super.onPlaybackResumption(session, controller)
        // ... build media items and return
    }
}
```

---

## Migration Strategy

### Week 1: Foundation
- [ ] Create `SmilClipRepository` singleton
- [ ] Create `PlayingBookInfo` data class
- [ ] Update `SmilLoadingManager` to store clips in repository
- [ ] Add tests for repository

### Week 2: Service Upgrade
- [ ] Convert `MediaPlaybackService` to `MediaLibraryService`
- [ ] Move `MediaSession` creation into service
- [ ] Implement `PlayerMessage` scheduling for clips
- [ ] Add `CLIP_CHANGED` broadcast mechanism
- [ ] Update manifest (but keep `exported="false"` initially)

### Week 3: Client Refactoring
- [ ] Refactor `MediaOverlayPlayer` to use `MediaController`
- [ ] Remove `exoPlayer.release()` call
- [ ] Create `HighlightController` for UI updates
- [ ] Remove `LocatorTracker` dependency on ExoPlayer
- [ ] Update `MediaSessionManager` to delegate to service

### Week 4: Reconnection & Polish
- [ ] Implement reconnection detection in `MediaPlaybackController`
- [ ] Add UI state restoration in `MediaOverlayPlayer`
- [ ] Test background playback scenarios
- [ ] Test notification controls

### Week 5: Android Auto
- [ ] Set `exported="true"` in manifest
- [ ] Add `automotive_app_desc.xml`
- [ ] Implement `onGetLibraryRoot` and `onGetChildren`
- [ ] Implement `onPlaybackResumption`
- [ ] Test with Android Auto emulator

---

## Breaking Changes

### API Changes

| Component | Breaking Change | Migration |
|-----------|-----------------|-----------|
| `MediaOverlayPlayer` | No longer has `exoPlayer` property | Use `mediaController` methods |
| `LocatorTracker` | Constructor signature changes | Remove ExoPlayer param |
| `MediaSessionManager` | Delegates to service | Internal change only |
| `SmilClipCache` | Deprecated | Use `SmilClipRepository` |

### Behavioral Changes

| Behavior | Before | After |
|----------|--------|-------|
| Leave reader | Playback stops | Playback continues |
| Return to book | Fresh start | Reconnect to playback |
| Kill app | Playback stops | Playback continues (30min timeout) |
| Android Auto | Not supported | Full support |

---

## File Structure After Refactoring

```
feature/reader/ui/src/androidMain/kotlin/com/retro99/reader/ui/
├── playback/
│   ├── MediaPlaybackService.kt          # MediaLibraryService (owns ExoPlayer, MediaSession)
│   ├── MediaPlaybackController.kt       # Singleton bridge + reconnection
│   ├── ClipScheduler.kt                 # NEW: PlayerMessage scheduling
│   ├── PlaybackStateManager.kt          # NEW: Service-side state tracking
│   ├── ForegroundServiceController.kt   # Unchanged
│   └── auto/
│       ├── LibraryBrowser.kt            # NEW: Android Auto browsing
│       └── automotive_app_desc.xml      # NEW: Android Auto config
├── media/
│   ├── MediaOverlayPlayer.kt            # REFACTORED: MediaController client
│   ├── HighlightController.kt           # NEW: Receives CLIP_CHANGED, updates UI
│   ├── smil/
│   │   ├── SmilClipRepository.kt        # NEW: @Single, global clip storage
│   │   ├── SmilLoadingManager.kt        # MODIFIED: Stores to repository
│   │   └── SmilClipCache.kt             # DEPRECATED
│   └── MediaOverlayClip.kt              # Unchanged
└── di/
    └── ReaderModule.kt                   # Updated DI bindings
```

---

## Testing Strategy

### Unit Tests
- `SmilClipRepository` - thread safety, storage/retrieval
- `ClipScheduler` - correct message timing
- `MediaPlaybackController` - reconnection detection

### Integration Tests
- Service starts and creates ExoPlayer
- Clips loaded and scheduled correctly
- CLIP_CHANGED broadcasts received by clients
- Reconnection works when re-entering book

### Manual Tests
- [ ] Play audio, leave reader, verify notification stays
- [ ] Return to same book, verify UI syncs with playback
- [ ] Play audio, kill app, verify 30min timeout works
- [ ] Test with Android Auto Desktop Head Unit (DHU)
- [ ] Test Bluetooth controls (play/pause/next/prev)
- [ ] Test headphone disconnect pause

---

## Risks and Mitigations

| Risk | Likelihood | Impact | Mitigation |
|------|------------|--------|------------|
| Breaking existing playback | Medium | High | Comprehensive tests, phased rollout |
| Android Auto certification issues | Low | Medium | Follow Google's guidelines exactly |
| Memory leaks from singleton clips | Low | Medium | Clear clips when book closes, LRU cache |
| Race conditions in reconnection | Medium | Medium | Synchronization, atomic operations |
| Performance regression from IPC | Low | Low | MediaController is efficient |

---

## Success Criteria

1. **Background Playback**: Audio continues when leaving reader screen
2. **Reconnection**: UI reconnects seamlessly when re-entering book
3. **Notification**: Stays visible with working controls while playing
4. **Android Auto**: Books browsable and playable from car
5. **No Regressions**: All existing playback features work correctly

