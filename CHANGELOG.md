# Changelog

All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [0.4.0] - 2026-03-11

### Added
- **Configurable Tap Navigation** - Enable/disable tap navigation and customize left/right tap actions (Next Page or Previous Page)
- **Configurable Double-Tap Timeout** - Adjust double-tap detection timing (200-800ms) in Settings under Read Aloud section
- **Audio Progress Bar Visibility Setting** - Choose when to show the audio progress bar: On Tap (with controls) or Never

### Changed
- Moved double-tap detection from JavaScript to native code for more consistent behavior across platforms
- Renamed `VolumeButtonAction` enum to `NavigationAction` for semantic clarity (now used for both volume buttons and tap zones)

## [0.3.0] - 2026-03-10

### Added
- **Android Auto Integration** - Full Android Auto support with headless playback for listening to audiobooks while driving
- **Background Audio Playback** - Audio continues playing when the app is in the background with proper notification controls
- **Mini Player** - New mini player component that shows current playback state across the app
- **Chapter Navigation in Media Controls** - Skip to next/previous chapter directly from notification or Android Auto
- **Volume Button Navigation** - Use hardware volume buttons to navigate pages while reading

### Fixed
- Playback speed not being persisted between sessions
- Seek bar not updating when navigating pages
- Incorrect ReadAloud starting position
- `VisibleSentenceDetector` returning `not_found` on chapter change
- Position preservation when changing font size
- Multi-audio file playback and chapter completion
- Slow audio loading by pre-scanning all SMIL files

### Changed
- Improved synchronization between media playback and reader UI
- Better notification metadata with chapter titles
- Refactored Android audio playback to a service-centric architecture for better reliability
- Improved audio sync on navigation
- Updated README with Parrot app description

## [0.2.0] - 2025-XX-XX

### Added
- Separate underline color option for ReadAloud
- DATE_ADDED sort option for books
- CACHED quick filter for books
- Publication date support for local books
- Auto-scroll to expanded sections in settings

### Fixed
- Series position not showing in UI
- Highlight color selection and persistence issues
- Highlight color alpha being ignored by Readium
- Race condition in LocalServerInitializer causing imported books not to appear
- Underline color in dark theme

### Changed
- Made font selection expandable in Reader settings
- Renamed BooksViewModel to BooksListViewModel
- Refactored highlight color to support custom colors

## [0.1.0] - 2025-XX-XX

### Added
- Initial release
- Storyteller server integration
- Local server support for importing books
- EPUB reading with Readium
- Audio playback with media overlays (Read Aloud)
- Reading progress tracking and sync
- Book filtering and sorting
- Series support
- Reading statistics
- Customizable reader settings (fonts, colors, themes)
- Book caching for offline reading
- Multi-user/server support
- iOS and Android support

