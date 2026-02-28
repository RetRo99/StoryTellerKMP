# Parrot

A cross-platform ebook and audiobook reader app built with Kotlin Multiplatform and Compose Multiplatform. Parrot is a client for [Storyteller](https://github.com/smoores-dev/storyteller) servers, allowing you to stream and read your personal book library on Android, iOS, and Desktop.

## Features

- **📚 Ebook Reader** - Read EPUB books with customizable fonts, themes, and margins
- **🎧 Audiobook Player** - Listen to audiobooks with playback controls
- **🗣️ Read Aloud** - Synchronized text highlighting with audio narration (for books with media overlays)
- **📖 Multiple Server Support** - Connect to multiple Storyteller servers and switch between them
- **📁 Local Books** - Import and read local EPUB files
- **📊 Reading Statistics** - Track reading time, streaks, sessions, and most-read books
- **🔖 Reading Progress Sync** - Sync reading position across devices via server
- **📑 Table of Contents** - Navigate chapters easily
- **🔍 Search & Filter** - Find books by title, author, series, or tags
- **⭐ Favorites** - Mark books as favorites for quick access
- **📱 E-Ink Support** - Optimized color scheme for e-ink devices
- **🌙 Dark Mode** - System-aware dark/light theme support

## Supported Platforms

| Platform | Status |
|----------|--------|
| Android  | ✅     |
| iOS      | ✅     |
| Desktop (JVM) | ✅ |

## Project Structure

```
├── composeApp/          # Main application module (shared UI & app entry points)
├── androidApp/          # Android-specific app wrapper
├── iosApp/              # iOS-specific app wrapper (Xcode project)
├── feature/             # Feature modules (Clean Architecture)
│   ├── auth/            # Authentication
│   ├── books/           # Book listing, details, series
│   ├── home/            # Home navigation & bottom tabs
│   ├── login/           # Login/onboarding flow
│   ├── reader/          # EPUB reader & audio player
│   ├── settings/        # Reader settings
│   └── statistics/      # Reading statistics & analytics
├── lib/                 # Shared libraries
│   ├── analytics/       # Analytics abstraction
│   ├── database/        # SQLDelight database
│   ├── network/         # Ktor networking
│   ├── preferences/     # DataStore preferences
│   ├── server/          # Server abstraction layer
│   ├── server-local/    # Local file server implementation
│   ├── server-storyteller/ # Storyteller API implementation
│   └── user/            # User & server management
├── base/                # Base classes & utilities
├── base-ui/             # Shared UI components
└── translations/        # Internationalization resources
```

## Tech Stack

- **Kotlin Multiplatform** - Share code across Android, iOS, and Desktop
- **Compose Multiplatform** - Declarative UI framework
- **Koin** - Dependency injection with annotations
- **Ktor** - HTTP client for API communication
- **SQLDelight** - Type-safe SQL database
- **DataStore** - Preferences storage
- **Readium** - EPUB rendering engine
- **Coil** - Image loading

## Building

### Prerequisites

- JDK 17+
- Android Studio or IntelliJ IDEA
- Xcode (for iOS builds)

### Android

```shell
./gradlew :composeApp:assembleDebug
```

### Desktop

```shell
./gradlew :composeApp:run
```

### iOS

Open `iosApp/iosApp.xcworkspace` in Xcode and run the project.

## Server Setup

Parrot requires a [Storyteller](https://github.com/smoores-dev/storyteller) server to stream books. You can also import local EPUB files directly into the app.

## License

This project is for personal use.

