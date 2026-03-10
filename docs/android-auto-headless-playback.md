# Android Auto Headless Playback Implementation Guide

## Overview

This document outlines the steps to implement **headless playback** for Android Auto - enabling users to select and play books directly from their car without interacting with their phone.

## Current State vs. Target

| Feature | Current | Target |
|---------|---------|--------|
| User selects book in Auto | Opens app on phone (deep link) | **Audio plays directly** |
| ExoPlayer ownership | ✅ Service-owned | ✅ No change needed |
| Content browsing | ✅ Works (AutoMediaBrowser) | ✅ No change needed |
| Position persistence | ❌ Not in service | ⚠️ Implement |
| Chapter navigation | ❌ Not in service | ⚠️ Implement |

## Architecture Diagram

```
┌──────────────────────────────────────────────────────────────────────┐
│                    MediaPlaybackService                               │
│                                                                       │
│  ┌─────────────────────────────────────────────────────────────────┐ │
│  │ HeadlessPlaybackSession (per book)                              │ │
│  │  ├── HeadlessMediaOverlayPlayer (plays audio from EPUB)        │ │
│  │  ├── SmilLoadingManager (chapter detection)                     │ │
│  │  ├── EpubPublication (opened headlessly)                        │ │
│  │  └── Position persistence (SaveReadingProgressUseCase)          │ │
│  └─────────────────────────────────────────────────────────────────┘ │
│                            ▲                                          │
│                            │ creates                                  │
│  ┌─────────────────────────┴───────────────────────────────────────┐ │
│  │ HeadlessSessionFactory (@Single)                                │ │
│  │  - Opens EPUB via EpubPublicationService                        │ │
│  │  - Loads saved position from repository                         │ │
│  │  - Creates SMIL components                                      │ │
│  │  - Returns ready-to-play session                                │ │
│  └─────────────────────────────────────────────────────────────────┘ │
│                            ▲                                          │
│                            │ uses                                     │
│  ┌─────────────────────────┴───────────────────────────────────────┐ │
│  │ onAddMediaItems() (in LibrarySessionCallback)                   │ │
│  │  - Parses mediaId → serverId, bookUuid                          │ │
│  │  - Creates HeadlessPlaybackSession                              │ │
│  │  - Calls session.play()                                         │ │
│  └─────────────────────────────────────────────────────────────────┘ │
└──────────────────────────────────────────────────────────────────────┘
```

## Implementation Steps

### Step 1: Create `HeadlessMediaOverlayPlayer`

**File:** `feature/reader/ui/src/androidMain/kotlin/com/retro99/reader/ui/media/HeadlessMediaOverlayPlayer.kt`

Core audio player without UI dependencies. Reuse from stash:
- Loads SMIL clips for chapters
- Creates ExoPlayer playlist from audio files
- Handles seek, pause, resume
- Does NOT emit locators for text highlighting (no UI)

**Key differences from MediaOverlayPlayer:**
- No `PlaybackStateTracker` → no locator emissions
- No `LocatorTracker` → no text sync
- Takes ExoPlayer as parameter (doesn't create its own)
- No clip scheduling (plays whole audio files)

### Step 2: Create `HeadlessPlaybackSession`

**File:** `feature/reader/ui/src/androidMain/kotlin/com/retro99/reader/ui/playback/auto/HeadlessPlaybackSession.kt`

Manages playback lifecycle for one book:
- `play()` / `pause()` / `resume()`
- `seekForward()` / `seekBackward()`
- `skipToNextChapter()` / `skipToPreviousChapter()`
- Auto-saves position every 30 seconds
- Implements `AutoCloseable` for cleanup

### Step 3: Create `HeadlessSessionFactory`

**File:** `feature/reader/ui/src/androidMain/kotlin/com/retro99/reader/ui/playback/auto/HeadlessSessionFactory.kt`

Factory to create sessions from mediaId:
```kotlin
@Single
class HeadlessSessionFactory(
    private val context: Context,
    private val publicationService: EpubPublicationService,
    private val ebookFileDownloader: EbookFileDownloader,
    private val saveReadingProgressUseCase: SaveReadingProgressUseCase,
    private val analytics: Analytics,
    private val smilParser: SmilParser,
    private val smilQuickScanner: SmilQuickScanner,
) {
    suspend fun createSession(
        serverId: String,
        bookUuid: String,
        exoPlayer: ExoPlayer,
    ): HeadlessPlaybackSession?
}
```

### Step 4: Create `DynamicPublicationDataSourceFactory`

**File:** `feature/reader/ui/src/androidMain/kotlin/com/retro99/reader/ui/media/DynamicPublicationDataSourceFactory.kt`

Allows ExoPlayer to read audio from different EPUBs without recreating player:
- `setPublication(publication)` - updates current book
- Creates `DataSource` instances that read from EPUB container

### Step 5: Modify `MediaPlaybackService`

**File:** `feature/reader/ui/src/androidMain/kotlin/com/retro99/reader/ui/playback/MediaPlaybackService.kt`

Changes needed:
1. Inject `HeadlessSessionFactory`
2. Add `activePlaybackSession: HeadlessPlaybackSession?` property
3. Modify `onAddMediaItems()` to start playback instead of returning deep link
4. Add `DynamicPublicationDataSourceFactory` to ExoPlayer configuration
5. Handle session cleanup on service destroy

### Step 6: Wire Media Button Actions

In `MediaPlaybackService.LibrarySessionCallback`:
- `onPlay` → `activePlaybackSession?.resume()`
- `onPause` → `activePlaybackSession?.pause()`
- `onSeekForward` → `activePlaybackSession?.seekForward()`
- `onSeekBack` → `activePlaybackSession?.seekBackward()`
- `onSkipToNext` → `activePlaybackSession?.skipToNextChapter()`
- `onSkipToPrevious` → `activePlaybackSession?.skipToPreviousChapter()`

---

## Detailed Code Changes

### Step 5a: `onAddMediaItems` Change

**Before (current - returns deep link):**
```kotlin
override fun onAddMediaItems(
    mediaSession: MediaSession,
    controller: ControllerInfo,
    mediaItems: MutableList<MediaItem>
): ListenableFuture<MutableList<MediaItem>> {
    val item = mediaItems.first()
    val deepLinkUri = DeepLinkUriBuilder.buildReaderUri(...)
    return Futures.immediateFuture(
        mutableListOf(item.buildUpon().setUri(deepLinkUri.toUri()).build())
    )
}
```

**After (headless playback):**
```kotlin
override fun onAddMediaItems(
    mediaSession: MediaSession,
    controller: ControllerInfo,
    mediaItems: MutableList<MediaItem>
): ListenableFuture<MutableList<MediaItem>> {
    val mediaId = mediaItems.firstOrNull()?.mediaId ?: return Futures.immediateFuture(mutableListOf())
    val (serverId, bookUuid) = AutoMediaIds.parseBookId(mediaId) ?: return Futures.immediateFuture(mutableListOf())

    serviceScope.launch {
        // Close previous session
        activePlaybackSession?.close()

        // Create new session and start playback
        activePlaybackSession = sessionFactory.createSession(
            serverId = serverId,
            bookUuid = bookUuid,
            exoPlayer = player,
            dynamicDataSourceFactory = dataSourceFactory,
        )
        activePlaybackSession?.play()
    }

    return Futures.immediateFuture(mediaItems)
}
```

---

## Files to Create

| # | File Path | Lines | Purpose |
|---|-----------|-------|---------|
| 1 | `feature/reader/ui/.../media/HeadlessMediaOverlayPlayer.kt` | ~400 | Core audio player for headless |
| 2 | `feature/reader/ui/.../playback/auto/HeadlessPlaybackSession.kt` | ~150 | Session lifecycle management |
| 3 | `feature/reader/ui/.../playback/auto/HeadlessSessionFactory.kt` | ~200 | Factory to create sessions |
| 4 | `feature/reader/ui/.../media/DynamicPublicationDataSourceFactory.kt` | ~130 | ExoPlayer data source switching |

## Files to Modify

| # | File Path | Changes |
|---|-----------|---------|
| 1 | `feature/reader/ui/.../playback/MediaPlaybackService.kt` | Add session factory, modify onAddMediaItems |
| 2 | `gradle/libs.versions.toml` | Add `kotlinx-coroutines-guava` |
| 3 | `feature/reader/ui/build.gradle.kts` | Add coroutines-guava dependency |

## Dependencies to Add

```toml
# In gradle/libs.versions.toml
[libraries]
kotlinx-coroutines-guava = { module = "org.jetbrains.kotlinx:kotlinx-coroutines-guava", version.ref = "coroutines" }
```

```kotlin
// In feature/reader/ui/build.gradle.kts
androidMain.dependencies {
    implementation(libs.kotlinx.coroutines.guava)
}
```

---

## Scope Analysis Summary

### Components That Stay @ReaderScope (UI-only)
- `AndroidBookController` - holds EpubNavigatorFragment
- `AndroidAudioController` - wraps MediaOverlayPlayer for UI
- `ReaderSyncCoordinator` - syncs text ↔ audio for highlighting
- `ReadingSpeedTracker` - per-book reading stats
- `PublicationSmilContentProvider` - **create directly in factory for headless**

### Components Already @Single (No changes needed)
- `EpubPublicationService` - opens EPUBs
- `MediaPlaybackController` - service bridge
- `SmilParser` - stateless parser
- `SmilQuickScanner` - stateless scanner

### New Components (@Single or @Factory)
- `HeadlessSessionFactory` → `@Single`
- `HeadlessPlaybackSession` → **Not in Koin** (created by factory)
- `HeadlessMediaOverlayPlayer` → **Not in Koin** (created by factory)


---

## Testing

### 1. Desktop Head Unit (DHU) Setup
```bash
# Install via Android Studio SDK Manager:
# SDK Manager → SDK Tools → Android Auto Desktop Head Unit Emulator

# Run DHU:
cd $ANDROID_HOME/extras/google/auto
./desktop-head-unit
```

### 2. Enable Developer Mode
1. Open Android Auto app on phone
2. Tap version number 10 times for developer mode
3. Settings → Developer settings → Enable "Unknown sources"

### 3. Test Scenarios
- [ ] Browse books in Android Auto
- [ ] Select book → audio starts playing
- [ ] Play/Pause controls work
- [ ] Seek forward/backward works
- [ ] Skip to next/previous chapter works
- [ ] Disconnect Auto → position saved
- [ ] Reconnect Auto → position restored
- [ ] Open book on phone → position from Auto restored

---

## Stash Reference

The implementation code is available in `git stash@{0}`. Key files:
- `auto.md` - Detailed implementation documentation
- `HeadlessPlaybackSession.kt` - Session management
- `HeadlessSessionFactory.kt` - Factory with full createSession() logic
- `HeadlessMediaOverlayPlayer.kt` - Audio player implementation
- `DynamicPublicationDataSourceFactory.kt` - DataSource switching

To extract specific code:
```bash
# View the entire stash diff
git stash show -p stash@{0}

# Extract a specific file
git stash show -p stash@{0} | grep -A 500 "HeadlessPlaybackSession.kt" > extracted.kt
```

---

## Dead Code to Remove

The following code in `PlatformReaderModule.android.kt` is no longer used (ExoPlayer now owned by service):

```kotlin
// DELETE THIS - Dead code
@Scope(ReaderScope::class)
@Scoped
fun provideExoPlayer(context: Context): ExoPlayer {
    return ExoPlayer.Builder(context)
        .setSeekBackIncrementMs(SEEK_INCREMENT_MS)
        .setSeekForwardIncrementMs(SEEK_INCREMENT_MS)
        .setHandleAudioBecomingNoisy(true)
        .build()
}
```

---

## Estimated Effort

| Task | Hours |
|------|-------|
| Create HeadlessMediaOverlayPlayer | 4 |
| Create HeadlessPlaybackSession | 3 |
| Create HeadlessSessionFactory | 4 |
| Create DynamicPublicationDataSourceFactory | 2 |
| Modify MediaPlaybackService | 3 |
| Wire media button callbacks | 2 |
| Testing with Android Auto emulator | 6 |
| Edge cases & polish | 4 |
| **Total** | **~28 hours** |

